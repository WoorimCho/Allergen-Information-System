# 🔧 MySQL Connection Troubleshooter

## Quick Diagnosis

Run these commands to identify your issue:

### Step 1: Check MySQL Container
```powershell
docker-compose ps
```

**Expected:** `allergen-mysql` shows as `Up (healthy)`

**If not:**
```powershell
docker-compose up mysql -d
```

### Step 2: Test MySQL Connection
```powershell
docker-compose exec mysql mysql -u myuser -psecret -e "SHOW DATABASES;"
```

**Expected:** Should show `mydatabase`

**If error:**
```powershell
# Reset MySQL
docker-compose down -v
docker-compose up mysql -d
```

### Step 3: Check App Startup Method

**Are you running:**
- ✅ `docker-compose up --build` → Use Solution A
- ❌ `mvn spring-boot:run` or IDE → Use Solution B
- ❌ Just `mvn spring-boot:run` without Docker MySQL → Use Solution C

---

## Solution A: Docker Compose (RECOMMENDED)

```powershell
# Stop everything
docker-compose down

# Start everything
docker-compose up --build

# Wait for "Started AllergenInformationSystem"
# Then open: http://localhost:8080
```

✅ **This is the easiest and most reliable method!**

---

## Solution B: Local App + Docker MySQL

```powershell
# Terminal 1: Start MySQL only
docker-compose up mysql

# Terminal 2: Run your app
mvn spring-boot:run

# Or run from your IDE (IntelliJ, Eclipse, VS Code)
```

✅ **Good for development with IDE debugging**

---

## Solution C: Everything Local (No Docker)

If you want to run without Docker:

```powershell
# 1. Install MySQL locally (if not already)
# 2. Start MySQL service
Start-Service MySQL80

# 3. Create database
mysql -u root -p
```

```sql
CREATE DATABASE mydatabase;
CREATE USER 'myuser'@'localhost' IDENTIFIED BY 'secret';
GRANT ALL PRIVILEGES ON mydatabase.* TO 'myuser'@'localhost';
EXIT;
```

```powershell
# 4. Run app
mvn spring-boot:run
```

---

## Common Errors & Instant Fixes

### Error: "Communications link failure"
```powershell
# MySQL not running. Start it:
docker-compose up mysql -d
```

### Error: "Access denied for user"
```powershell
# Check credentials in compose.yaml match application.properties
docker-compose down -v
docker-compose up --build
```

### Error: "Unknown database"
```powershell
# Database not created. Reset:
docker-compose down -v
docker-compose up mysql -d
```

### Error: Port 3306 already in use
```powershell
# Another MySQL running. Stop it:
Stop-Service MySQL80

# Or check what's using it:
netstat -ano | findstr :3306
```

---

## Quick Status Check

Run this to see everything:

```powershell
# Check containers
docker-compose ps

# Check MySQL health
docker-compose exec mysql mysqladmin ping -u myuser -psecret

# Check logs
docker-compose logs --tail=20 app
```

---

## 🎯 TL;DR - Just Make It Work!

**Run this:**
```powershell
docker-compose down
docker-compose up --build
```

**Wait for:**
```
✔ Container allergen-mysql  Healthy
✔ Container allergen-app    Started
```

**Open:**
```
http://localhost:8080
```

**Done!** 🌸

---

**If still having issues, see MYSQL_CONNECTION_FIX.md for complete details**

