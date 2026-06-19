# 🐳 Docker Quick Reference

## ✅ Problem Fixed

**Error:** `Communications link failure` - Could not connect to MySQL

**Solution:** 
- Fixed Dockerfile for proper multi-stage build
- Updated application.properties with environment variables
- Enhanced compose.yaml with MySQL + App services
- Added health checks and proper networking

---

## 🚀 Quick Start (Use This!)

```bash
# Build and run everything
docker-compose up --build

# Run in background
docker-compose up --build -d

# Stop everything
docker-compose down
```

Then open: **http://localhost:8080** 🌸

---

## 🔧 What Changed

| File | Change | Why |
|------|--------|-----|
| `Dockerfile` | Multi-stage build, layer caching | Faster builds, smaller image |
| `application.properties` | Environment variables | Works in Docker & locally |
| `compose.yaml` | Added app service + networking | MySQL & app can communicate |
| `.dockerignore` | Exclude unnecessary files | Faster builds |

---

## 📁 New File Structure

```
project/
├── Dockerfile              ✅ Fixed & optimized
├── compose.yaml            ✅ Complete with app + mysql
├── .dockerignore           ✅ New - faster builds
└── src/main/resources/
    └── application.properties  ✅ Updated with env vars
```

---

## 🎯 Common Commands

```bash
# Start
docker-compose up --build

# Stop
docker-compose down

# View logs
docker-compose logs -f app

# Clean restart (delete data)
docker-compose down -v && docker-compose up --build

# Check status
docker-compose ps
```

---

## 🔍 How It Works Now

```
Docker Network
    │
    ├── MySQL Container (mysql:3306)
    │   └── Database: mydatabase
    │
    └── App Container (app:8080)
        └── Connects to: mysql:3306 ✅
```

**Key:** Service name `mysql` in connection string, not `localhost`!

---

## 🧪 Test It

```bash
# 1. Start
docker-compose up --build

# 2. Wait for:
# ✅ "allergen-mysql Healthy"
# ✅ "Started AllergenInformationSystem"

# 3. Open browser
http://localhost:8080

# 4. Should see cherry blossom home page!
```

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| Port 8080 in use | Change port in compose.yaml |
| Build fails | `docker system prune -a` |
| Can't connect to DB | Check logs: `docker-compose logs mysql` |
| Slow builds | Use `DOCKER_BUILDKIT=1` |

---

## 📊 Environment Variables

Used in Docker (defined in compose.yaml):
- `SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/mydatabase`
- `SPRING_DATASOURCE_USERNAME=myuser`
- `SPRING_DATASOURCE_PASSWORD=secret`

Defaults for local (no Docker):
- `jdbc:mysql://localhost:3306/mydatabase`
- `myuser`
- `secret`

---

## ✅ Verification Checklist

After running `docker-compose up --build`:

- [ ] No "Communications link failure" error
- [ ] Logs show "Started AllergenInformationSystem"
- [ ] Can access http://localhost:8080
- [ ] Home page shows cherry blossom theme
- [ ] All buttons work
- [ ] Can create/view/edit/delete items

---

## 📖 Full Documentation

See `DOCKER_SETUP_GUIDE.md` for complete details!

---

**Status:** ✅ Working  
**Command:** `docker-compose up --build`  
**Access:** http://localhost:8080  
**Date:** June 19, 2026 🌸

