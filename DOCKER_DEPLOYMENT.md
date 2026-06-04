# NaijaVote Docker Deployment Guide

This guide explains how to deploy the NaijaVote application using Docker Compose.

## Prerequisites

- Docker (v20.10+)
- Docker Compose (v2.0+)
- Git (to clone the repository)

## Quick Start

### 1. Build and Start Services

```bash
docker compose up -d
```

This command will:
- Build the NaijaVote Spring Boot application from source
- Start PostgreSQL database container
- Start the NaijaVote application container
- Create necessary volumes and networks

### 2. Verify Services Are Running

```bash
docker compose ps
```

You should see:
- `naijavote-postgres` - PostgreSQL database (port 5432)
- `naijavote-app` - Spring Boot application (port 8080)

### 3. Access the Application

- **Web Application**: http://localhost:8080
- **Admin Panel**: http://localhost:8080/admin/parties
- **Health Check**: http://localhost:8080/actuator/health

## Database Information

- **Host**: `postgres` (or `localhost` from host machine)
- **Port**: 5432
- **Database**: `naijavote`
- **Username**: `naijavote_user`
- **Password**: `naijavote_password`

## Default Admin Credentials

- **Username**: `admin`
- **Password**: `temporary`

> ⚠️ **Important**: Change these credentials in production!

## Common Commands

### View Logs

```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f naijavote-app
docker compose logs -f postgres
```

### Stop Services

```bash
docker compose down
```

### Stop and Remove All Data

```bash
docker compose down -v
```

### Rebuild Application

```bash
docker compose up -d --build
```

### Access Database

```bash
docker compose exec postgres psql -U naijavote_user -d naijavote
```

## Environment Variables

You can override environment variables by creating a `.env` file:

```env
POSTGRES_DB=naijavote
POSTGRES_USER=naijavote_user
POSTGRES_PASSWORD=your_secure_password
```

Then use:
```bash
docker compose --env-file .env up -d
```

## Health Checks

Both services include health checks:

- **PostgreSQL**: Checks if database is ready to accept connections
- **Spring Boot App**: Checks the `/actuator/health` endpoint

The application will automatically wait for the database to be healthy before starting.

## Troubleshooting

### Application fails to connect to database

1. Check if PostgreSQL is running:
   ```bash
   docker compose ps postgres
   ```

2. Check application logs:
   ```bash
   docker compose logs naijavote-app
   ```

3. Verify database credentials match in `application.yml`

### Port Already in Use

If ports 5432 or 8080 are already in use, modify `docker-compose.yml`:

```yaml
services:
  postgres:
    ports:
      - "5433:5432"  # Changed from 5432

  naijavote-app:
    ports:
      - "8081:8080"  # Changed from 8080
```

### Database Migrations Not Applied

1. Check Flyway migration files exist in `src/main/resources/db/migration/`
2. Review logs:
   ```bash
   docker compose logs naijavote-app | grep Flyway
   ```

### Clean Rebuild

Sometimes a fresh build is needed:

```bash
docker compose down -v
docker system prune -f
docker compose up -d --build
```

## Production Deployment

For production, consider:

1. **Use specific image tags** instead of `latest`
2. **Store secrets** in environment variables or Docker secrets
3. **Enable restart policies** (already configured with `restart: unless-stopped`)
4. **Configure resource limits** in docker-compose.yml:
   ```yaml
   services:
     naijavote-app:
       deploy:
         resources:
           limits:
             cpus: '1'
             memory: 512M
   ```
5. **Use a reverse proxy** (nginx/Traefik) for SSL/TLS
6. **Set up logging** with proper log drivers
7. **Configure backup strategy** for PostgreSQL volume

## Docker Compose File Structure

```yaml
version: '3.8'

services:
  postgres:          # PostgreSQL database service
  naijavote-app:     # Spring Boot application service

volumes:
  postgres_data:     # Persistent data storage for PostgreSQL

networks:
  naijavote_network: # Internal communication network
```

## File Descriptions

- `docker-compose.yml` - Defines all services, volumes, and networks
- `Dockerfile` - Multi-stage build for optimized application image
- `.dockerignore` - Files to exclude from Docker build context
- `application.yml` - Spring Boot configuration (with PostgreSQL connection)
- `pom.xml` - Maven dependencies and build configuration

## Support

For issues or questions, check the main README.md or project documentation.
