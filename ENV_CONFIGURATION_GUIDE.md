# NaijaVote - Environment Configuration Guide

## Overview

The NaijaVote application uses `.env` files to manage environment-specific configuration. This approach is recommended for:
- Security (sensitive credentials not in code)
- Flexibility (different configs for dev/staging/prod)
- Ease of deployment (no code changes needed)
- Docker Compose integration (automatic variable substitution)

## Files

### `.env` (Production/Development)
- **Location**: Root directory of the project
- **Purpose**: Contains actual configuration values for the current environment
- **Security**: Should be added to `.gitignore` (not committed to version control)
- **Used by**: Docker Compose and application runtime

### `.env.example`
- **Location**: Root directory of the project
- **Purpose**: Template showing all available variables and their defaults
- **Security**: Safe to commit to version control
- **Use case**: New developers can copy this to `.env` and customize

## Quick Start

### 1. Setup Environment File

#### Option A: Using Default Settings
```bash
cp .env.example .env
```

#### Option B: Manual Setup
Create a `.env` file in the project root:
```bash
cat > .env << 'EOF'
# PostgreSQL Database Configuration
POSTGRES_DB=naijavote
POSTGRES_USER=naijavote_user
POSTGRES_PASSWORD=naijavote_password

# Spring Boot Application Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/naijavote
SPRING_DATASOURCE_USERNAME=naijavote_user
SPRING_DATASOURCE_PASSWORD=naijavote_password

# Other settings...
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_FLYWAY_ENABLED=true
LOGGING_LEVEL_COM_NAIJAVOTE=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=INFO
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=default
EOF
```

### 2. Deploy with Docker Compose

The Docker Compose automatically reads the `.env` file:

```bash
# Start the application
docker compose up -d

# Docker Compose will:
# 1. Read variables from .env
# 2. Substitute them in docker-compose.yml
# 3. Pass them to containers as environment variables
```

### 3. Verify Configuration

```bash
# Check that variables are loaded
docker compose config | grep -A 5 "environment"

# Check running container environment
docker exec naijavote-app env | grep SPRING_DATASOURCE
```

## Configuration Variables

### Database Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_DB` | Database name | naijavote |
| `POSTGRES_USER` | Database user | naijavote_user |
| `POSTGRES_PASSWORD` | Database password | naijavote_password |

### Spring Boot Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | Database JDBC URL | jdbc:postgresql://postgres:5432/naijavote |
| `SPRING_DATASOURCE_USERNAME` | Database username | naijavote_user |
| `SPRING_DATASOURCE_PASSWORD` | Database password | naijavote_password |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate DDL mode | validate |
| `SPRING_FLYWAY_ENABLED` | Enable Flyway migrations | true |

### Logging Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `LOGGING_LEVEL_COM_NAIJAVOTE` | App logging level | DEBUG |
| `LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY` | Security logging | INFO |

### Server Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Application port | 8080 |

### Application Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profiles | default |

## Environment-Specific Configurations

### Development Environment

```bash
# .env
POSTGRES_PASSWORD=dev_password_123
LOGGING_LEVEL_COM_NAIJAVOTE=DEBUG
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

### Staging Environment

```bash
# .env
POSTGRES_PASSWORD=staging_secure_password_xyz
LOGGING_LEVEL_COM_NAIJAVOTE=INFO
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=staging
```

### Production Environment

```bash
# .env
POSTGRES_PASSWORD=prod_super_secure_password_abc123!@#
LOGGING_LEVEL_COM_NAIJAVOTE=WARN
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

## Security Best Practices

### 1. **Never Commit `.env` to Git**

```bash
# Verify .gitignore includes .env
cat .gitignore | grep "^.env"

# If not tracked, it won't be committed
git status .env  # Should show "untracked" or nothing
```

### 2. **Protect Sensitive Credentials**

```bash
# Set restrictive file permissions
chmod 600 .env

# Only your user can read it
ls -la .env  # Should show: -rw------- or similar
```

### 3. **Use Strong Passwords for Production**

```bash
# Generate a strong password
openssl rand -base64 32

# Update .env
POSTGRES_PASSWORD=your_generated_strong_password_here
```

### 4. **Manage Secrets Securely**

**For Production:**
- Use Docker secrets or Kubernetes secrets
- Use AWS Secrets Manager, HashiCorp Vault, etc.
- Never hardcode credentials in containers

**Option: Docker Secrets** (Swarm Mode)
```bash
echo "naijavote_password" | docker secret create db_password -
```

Then update docker-compose.yml:
```yaml
services:
  postgres:
    secrets:
      - db_password
    environment:
      POSTGRES_PASSWORD_FILE: /run/secrets/db_password
```

## Troubleshooting

### Variables Not Being Substituted

**Problem**: Docker Compose shows literal `${POSTGRES_USER}` instead of `naijavote_user`

**Solution**:
```bash
# Verify .env file exists
ls -la .env

# Check file format (no extra spaces)
cat .env | head -5

# Verify Docker Compose reads it
docker compose config | grep POSTGRES_USER
```

### Permission Denied on `.env`

**Problem**: `Permission denied: .env`

**Solution**:
```bash
# Check permissions
ls -la .env

# Fix permissions
chmod 644 .env  # Readable by Docker

# Or more restrictive
chmod 600 .env  # Only owner
```

### Wrong Database Connection

**Problem**: `Connection refused` or `Unknown database`

**Solution**:
1. Verify `SPRING_DATASOURCE_URL` format
2. Check database is running: `docker compose logs postgres`
3. Verify credentials match: `POSTGRES_USER` and `POSTGRES_PASSWORD`
4. Check database name: `POSTGRES_DB`

## Example Workflows

### Switching Between Environments

```bash
# Development
cp .env.dev .env
docker compose up -d

# Staging
cp .env.staging .env
docker compose down -v
docker compose up -d

# Production
cp .env.prod .env
docker compose down -v
docker compose up -d
```

### Adding New Configuration Variables

1. **Update `.env.example`** with new variable and description:
   ```bash
   # New Feature Configuration
   NEW_FEATURE_ENABLED=true
   NEW_FEATURE_TIMEOUT=30
   ```

2. **Update `docker-compose.yml`** to reference the variable:
   ```yaml
   environment:
     NEW_FEATURE_ENABLED: ${NEW_FEATURE_ENABLED}
     NEW_FEATURE_TIMEOUT: ${NEW_FEATURE_TIMEOUT}
   ```

3. **Update `.env`** with actual value for current environment

4. **Update `application.yml`** if it needs the configuration:
   ```yaml
   app:
     feature:
       enabled: ${NEW_FEATURE_ENABLED:false}
       timeout: ${NEW_FEATURE_TIMEOUT:30}
   ```

### Verifying Configuration at Runtime

```bash
# Check what variables are being used
docker exec naijavote-app printenv | sort

# Filter for specific variables
docker exec naijavote-app printenv | grep SPRING_

# Check database connection
docker exec naijavote-postgres psql -U ${POSTGRES_USER} -d ${POSTGRES_DB} -c "SELECT 1"
```

## Advanced: Docker Compose Override Files

For different environments without changing `.env`:

### Create `docker-compose.override.yml` (for local development)

```yaml
services:
  naijavote-app:
    ports:
      - "8080:8080"
    environment:
      LOGGING_LEVEL_COM_NAIJAVOTE: DEBUG
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
```

This overrides settings in main `docker-compose.yml` without modifying it.

## References

- [Docker Compose Environment Variables](https://docs.docker.com/compose/environment-variables/)
- [Docker Secrets Management](https://docs.docker.com/engine/swarm/secrets/)
- [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [12 Factor App - Config](https://12factor.net/config)

## Summary

✅ **Benefits of Using `.env`:**
- ✅ Security: Credentials not in code
- ✅ Flexibility: Easy environment switching
- ✅ Simplicity: Single source of truth
- ✅ Best Practice: Follows 12 Factor App principles
- ✅ Docker Integration: Native support in Compose

**Next Steps:**
1. Copy `.env.example` to `.env`
2. Customize values for your environment
3. Run `docker compose up -d`
4. Verify with `docker compose config`

---

**Last Updated**: 2026-01-13
**Version**: 1.0.0
