# MCPTravel Project

## Overview
MCP-based platform for business discovery with real-time data. Allows AI agents to discover restaurants, cafes, and other businesses using natural language queries.

**Authors:** Revenco Victor (FAF-221), Popa Marius (FAF-222)
**University:** Technical University of Moldova
**Stack:** Java 17, Spring Boot 4.0.2, PostgreSQL, JWT Auth

## Architecture

```
src/main/java/com/example/MCPTravel/
├── entity/          # JPA entities (User, Company, MenuItem, Report)
├── repository/      # Spring Data JPA repositories
├── service/         # Business logic
├── controller/      # REST controllers
├── dto/             # Request/Response DTOs (auth, company, menu, report)
├── security/        # JWT authentication
└── config/          # Security, OpenAPI config
```

## Key Entities
- **User** - Users with roles: USER, BUSINESS_OWNER, ADMIN
- **Company** - Business locations with working hours, status, coordinates
- **MenuItem** - Menu items with name, description, price, category, availability
- **Report** - User reports against companies (warning system)

## Key Features
- **Auto-calculate open status**: `Company.isCurrentlyOpen()` checks working hours against current time
  - Working hours format: `{"MONDAY": "09:00-18:00", "TUESDAY": "09:00-18:00", ...}`
  - Supports overnight hours (e.g., `"22:00-02:00"`)
  - Returns false if status is CLOSED or TEMPORARILY_CLOSED

## API Endpoints

### Public (No Auth)
- `GET /api/discovery/tools` - MCP tool definitions for AI agents
- `GET /api/discovery/companies` - Search companies
- `GET /api/discovery/nearby` - Find nearby businesses
- `GET /api/discovery/categories` - List categories
- `GET /api/discovery/open-now` - Currently open businesses
- `POST /api/auth/register` - Register user
- `POST /api/auth/login` - Login, get JWT

### Protected (JWT Required)
- `POST /api/companies` - Create company (BUSINESS_OWNER)
- `PUT /api/companies/{id}` - Update company (owner only)
- `PATCH /api/companies/{id}/status` - Update status
- `POST /api/reports` - Create report (USER)
- `PATCH /api/reports/{id}/review` - Review report (ADMIN)

### Menu Management (JWT, owner only)
- `GET /api/companies/{id}/menu` - Get menu items (`?availableOnly=true` filter)
- `GET /api/companies/{id}/menu/items/{itemId}` - Get single item
- `POST /api/companies/{id}/menu` - Add menu item
- `POST /api/companies/{id}/menu/bulk` - Add multiple items
- `PUT /api/companies/{id}/menu/items/{itemId}` - Update item
- `DELETE /api/companies/{id}/menu/items/{itemId}` - Delete item
- `DELETE /api/companies/{id}/menu` - Clear entire menu

## Running the Project

### Option 1: Docker Compose (Recommended)
```bash
# Start everything (builds app + starts PostgreSQL)
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop everything
docker-compose down

# Stop and remove data
docker-compose down -v
```

### Option 2: Manual (for development)
```bash
# Start PostgreSQL
docker start mcptravel-db

# Start app with hot-reload
./mvnw spring-boot:run
```

### URLs
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- MCP Tools: http://localhost:8080/api/discovery/tools
- OpenAPI Spec: http://localhost:8080/api-docs

## Database
- **Host:** localhost:5432
- **Database:** mcptravel
- **User:** postgres
- **Password:** postgres

## Testing the API

```bash
# Register business owner
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"owner1","email":"owner@test.com","password":"pass123","role":"BUSINESS_OWNER"}'

# Create company with working hours (use token from register response)
curl -X POST http://localhost:8080/api/companies \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "name":"Cafe Central",
    "address":"Main St 1",
    "category":"cafe",
    "latitude":47.01,
    "longitude":28.86,
    "workingHours":{"MONDAY":"09:00-18:00","TUESDAY":"09:00-18:00"}
  }'

# Add menu item to company
curl -X POST http://localhost:8080/api/companies/1/menu \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"Espresso","description":"Strong coffee","price":25.00,"category":"drinks"}'

# Bulk add menu items
curl -X POST http://localhost:8080/api/companies/1/menu/bulk \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '[
    {"name":"Latte","price":35.00,"category":"drinks"},
    {"name":"Croissant","price":20.00,"category":"pastry"}
  ]'

# Check what's open now
curl http://localhost:8080/api/discovery/open-now
```

## Future Work
- Integrate local AI agent with /api/discovery/tools
- Add frontend application
- Implement geospatial queries with PostGIS
- Add image upload for menu items and company photos
- Add user favorites/bookmarks
- Add company reviews/ratings
