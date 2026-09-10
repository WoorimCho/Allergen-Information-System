package com.allergen_info_service.bff;

import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * A small fixed pool for fanning out the independent downstream reads in
 * {@link RecipeCompositionService} (recipe + the account's restrictions +
 * favourite substitutions all at once instead of one after another).
 *
 * <p>Wrapped twice so a worker thread behaves like the request thread:
 * <ul>
 *   <li>{@link DelegatingSecurityContextExecutorService} copies the caller's
 *       {@code SecurityContext} across — the outbound HMAC signer reads the
 *       acting user from it, and the User service rejects an account-scoped call
 *       whose acting user doesn't match.</li>
 *   <li>{@link ContextExecutorService} carries the Micrometer trace context, so
 *       the parallel calls stay in the same Zipkin trace.</li>
 * </ul>
 */
@Configuration
class DownstreamExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    ExecutorService bffDownstreamExecutor() {
        ThreadFactory threads = new ThreadFactory() {
            private final AtomicInteger n = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "bff-downstream-" + n.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        };
        ExecutorService pool = Executors.newFixedThreadPool(6, threads);
        ExecutorService secure = new DelegatingSecurityContextExecutorService(pool);

        ContextSnapshotFactory snapshots = ContextSnapshotFactory.builder().build();
        Supplier<ContextSnapshot> capture = snapshots::captureAll;
        return ContextExecutorService.wrap(secure, capture);
    }
}
