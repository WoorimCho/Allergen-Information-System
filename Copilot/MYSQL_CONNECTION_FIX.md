# 🔧 MySQL Connection Issue - Complete Fix

## 🎯 The Problem

Spring Boot starts but can't connect to MySQL. This happens because of different scenarios:

### Scenario 1: Running App Outside Docker ❌
```
You run: mvn spring-boot:run (or IDE)
         ↓
App tries: localhost:3306
         ↓
MySQL is: In Docker container OR not running
         ↓
Result: Connection Failed!
```

### Scenario 2: Running Everything in Docker ✅
```
You run: docker-compose up
         ↓
App uses: mysql:3306 (via environment variable)
         ↓
MySQL is: In same Docker network
         ↓
Result: Connection Success!
```

---

## ✅ SOLUTIONS (Choose Your Scenario)

### Solution 1: Run Everything with Docker (RECOMMENDED) ⭐

This is the easiest and most reliable:

```powershell
# Stop any running containers
docker-compose down

# Start everything fresh
docker-compose up --build

# Wait for:
# ✔ Container allergen-mysql  Healthy
# ✔ Container allergen-app    Started
```

**Why this works:**
- ✅ App and MySQL in same network
- ✅ Health checks ensure MySQL ready
- ✅ Environment variables set correctly
- ✅ No manual configuration needed

Then access: **http://localhost:8080**

---

### Solution 2: Run App Locally, MySQL in Docker

If you want to run Spring Boot from IDE/Maven but use Docker MySQL:

#### Step 1: Start Only MySQL
```powershell
docker-compose up mysql -d
```

#### Step 2: Wait for MySQL to be healthy
```powershell
docker-compose ps
# Should show: allergen-mysql (healthy)
```

#### Step 3: Run Spring Boot Locally
The app will use `localhost:3306` from application.properties default.

**Make sure MySQL port 3306 is exposed!** (Already done in compose.yaml)

---

### Solution 3: Run App Locally, MySQL Locally

If you have MySQL installed on Windows:

#### Step 1: Start MySQL Service
```powershell
# Check if MySQL is running
Get-Service -Name MySQL* | Select-Object Name, Status

# Start if needed
Start-Service -Name MySQL80  # or your MySQL service name
```

#### Step 2: Create Database
```sql
mysql -u root -p
CREATE DATABASE mydatabase;
CREATE USER 'myuser'@'localhost' IDENTIFIED BY 'secret';
GRANT ALL PRIVILEGES ON mydatabase.* TO 'myuser'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### Step 3: Run Spring Boot
```powershell
mvn spring-boot:run
```

---

## 🔍 Diagnostic Commands

### Check if MySQL Container is Running
```powershell
docker-compose ps

# Should show:
# NAME              STATUS
# allergen-mysql    Up (healthy)
# allergen-app      Up
```

### Check MySQL Container Logs
```powershell
docker-compose logs mysql

# Look for:
# "ready for connections"
# "MySQL init process done"
```

### Test MySQL Connection from Host
```powershell
# If you have MySQL client installed
mysql -h localhost -P 3306 -u myuser -psecret mydatabase

# Or using Docker
docker-compose exec mysql mysql -u myuser -psecret mydatabase
```

### Check Spring Boot Logs
```powershell
# If running in Docker
docker-compose logs app

# Look for errors like:
# "Communications link failure"
# "Access denied"
# "Unknown database"
```

---

## 🐛 Common Issues & Fixes

### Issue 1: "Communications link failure"

**Cause:** MySQL not running or wrong host  
**Fix:**
```powershell
# Check MySQL status
docker-compose ps

# Restart if needed
docker-compose restart mysql

# If running locally, use Docker MySQL
docker-compose up mysql -d
```

### Issue 2: "Access denied for user"

**Cause:** Wrong credentials  
**Fix:** Check that credentials match in:
- `application.properties` (defaults)
- `compose.yaml` (environment variables)

**Current credentials:**
- Username: `myuser`
- Password: `secret`
- Database: `mydatabase`

### Issue 3: "Unknown database 'mydatabase'"

**Cause:** Database not created  
**Fix:**
```powershell
# Connect to MySQL
docker-compose exec mysql mysql -u root -pverysecret

# Create database
CREATE DATABASE IF NOT EXISTS mydatabase;
EXIT;

# Restart app
docker-compose restart app
```

### Issue 4: Port 3306 already in use

**Cause:** Another MySQL instance running  
**Fix:**
```powershell
# Check what's using port 3306
netstat -ano | findstr :3306

# Stop local MySQL service
Stop-Service -Name MySQL80

# Or change Docker port in compose.yaml
ports:
  - '3307:3306'  # Use 3307 on host

# Then update application.properties
spring.datasource.url=jdbc:mysql://localhost:3307/mydatabase
```

### Issue 5: "Too many connections"

**Cause:** Connection pool exhausted  
**Fix:** Add to application.properties:
```properties
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=2
```

---

## 📊 Connection Flow Explained

### When Running with Docker Compose:

```
┌──────────────────────────────────────┐
│  Docker Network: allergen-network    │
│                                      │
│  ┌──────────────┐  ┌──────────────┐ │
│  │   MySQL      │  │     App      │ │
│  │              │◄─┤              │ │
│  │ Host: mysql  │  │ Connects to: │ │
│  │ Port: 3306   │  │ mysql:3306   │ │
│  └──────────────┘  └──────────────┘ │
│         │                 │          │
└─────────┼─────────────────┼──────────┘
          │                 │
    Host Port 3306    Host Port 8080
          │                 │
          └────── YOUR COMPUTER ───────┘
```

### When Running App Locally:

```
┌─── Docker ───┐         ┌─── Your Computer ───┐
│              │         │                      │
│   MySQL      │◄────────┤   Spring Boot App   │
│ localhost:   │         │   Connects to:      │
│   3306       │         │   localhost:3306    │
└──────────────┘         └─────────────────────┘
```

---

## 🧪 Quick Test Procedure

### Test 1: Docker Compose (Everything Together)

```powershell
# Clean start
docker-compose down -v
docker-compose up --build

# Expected output:
# ✔ allergen-mysql  Healthy
# ✔ allergen-app    Started
# Started AllergenInformationSystem

# Test in browser
http://localhost:8080
```

✅ **If this works:** Your Docker setup is perfect!

### Test 2: Local App + Docker MySQL

```powershell
# Start MySQL only
docker-compose up mysql -d

# Wait 10 seconds

# In another terminal
mvn spring-boot:run

# Expected: Should connect successfully
```

✅ **If this works:** MySQL Docker works, app works locally!

### Test 3: Direct MySQL Connection

```powershell
# Test MySQL is accessible
docker-compose exec mysql mysql -u myuser -psecret -e "SHOW DATABASES;"

# Should show 'mydatabase'
```

✅ **If this works:** MySQL is running and accessible!

---

## 🔧 Configuration Reference

### Current application.properties:
```properties
# Uses environment variable in Docker, localhost locally
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/mydatabase}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:myuser}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:secret}
```

### How it works:
- **In Docker:** Uses `SPRING_DATASOURCE_URL` from compose.yaml → `mysql:3306`
- **Locally:** Uses default → `localhost:3306`

### Current compose.yaml MySQL config:
```yaml
mysql:
  image: 'mysql:latest'
  environment:
    - 'MYSQL_DATABASE=mydatabase'
    - 'MYSQL_USER=myuser'
    - 'MYSQL_PASSWORD=secret'
    - 'MYSQL_ROOT_PASSWORD=verysecret'
  ports:
    - '3306:3306'  # Accessible on localhost:3306
```

---

## 🚀 Recommended Approach

### For Development:

**Option A: Everything in Docker** (Easiest)
```powershell
docker-compose up --build
```
- ✅ No local MySQL needed
- ✅ Consistent environment
- ✅ Easy to reset

**Option B: Local IDE + Docker MySQL** (For debugging)
```powershell
# Terminal 1: Start MySQL
docker-compose up mysql

# Terminal 2 or IDE: Run Spring Boot
mvn spring-boot:run
# Or click Run in IntelliJ/Eclipse
```
- ✅ Fast code refresh
- ✅ Better debugging
- ✅ Use IDE features

---

## 📝 Checklist for Connection Success

Run through this checklist:

- [ ] MySQL container is running: `docker-compose ps`
- [ ] MySQL is healthy: Shows "(healthy)" in status
- [ ] Port 3306 is exposed: Check compose.yaml
- [ ] Credentials match everywhere:
  - [ ] compose.yaml: `MYSQL_USER=myuser`
  - [ ] compose.yaml: `MYSQL_PASSWORD=secret`
  - [ ] compose.yaml: `MYSQL_DATABASE=mydatabase`
  - [ ] application.properties: same credentials
- [ ] Can connect to MySQL: `docker-compose exec mysql mysql -u myuser -psecret`
- [ ] Database exists: `SHOW DATABASES;` shows mydatabase
- [ ] App uses correct connection string:
  - [ ] Docker: `mysql:3306`
  - [ ] Local: `localhost:3306`

---

## 🎯 Quick Fix Commands

### Restart Everything
```powershell
docker-compose down
docker-compose up --build
```

### Reset Database (Clean Slate)
```powershell
docker-compose down -v  # Deletes volumes!
docker-compose up --build
```

### Check Connection
```powershell
# Test MySQL is accessible
docker exec allergen-mysql mysqladmin ping -h localhost -u myuser -psecret

# Should output: mysqld is alive
```

### View Detailed Logs
```powershell
# MySQL logs
docker-compose logs mysql | Select-String -Pattern "ready"

# App logs
docker-compose logs app | Select-String -Pattern "Started"
```

---

## ✅ Success Indicators

You know it's working when you see:

### In Docker Compose Logs:
```
allergen-mysql | MySQL init process done. Ready for start up.
allergen-mysql | ready for connections
allergen-app   | HikariPool-1 - Start completed
allergen-app   | Started AllergenInformationSystem in 5.234 seconds
```

### No Error Messages Like:
- ❌ Communications link failure
- ❌ Access denied
- ❌ Unknown database
- ❌ Connection refused

### Can Access:
- ✅ http://localhost:8080 loads
- ✅ Cherry blossom home page appears
- ✅ Can create/view data

---

## 🎉 Summary

**Problem:** Spring Boot can't connect to MySQL  

**Most Common Cause:** Running app and MySQL in different environments

**Best Solution:** Use `docker-compose up --build`

**Alternative:** Run `docker-compose up mysql -d` then `mvn spring-boot:run`

**Status:** Configuration is correct, just need to use the right startup method!

---

**Date:** June 19, 2026  
**Recommendation:** Use Docker Compose for easiest experience 🐳🌸

