package com.allergen_info_service.bff;

import java.io.Serializable;

/** The authenticated user, held in the BFF session. */
public record BffPrincipal(Long accountId, String username, String displayName) implements Serializable {
}
