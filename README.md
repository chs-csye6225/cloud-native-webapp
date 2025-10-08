# Cloud-Native Web Application

A RESTful web application built with Spring Boot for managing users and products with secure authentication and authorization.

## Prerequisites

### Software Requirements
- **Java 21** - Latest LTS version
- **Maven 3.6+** - Build automation tool
- **Docker & Docker Compose** - For PostgreSQL database
- **PostgreSQL 16** - Database (via Docker)
- **Postman** - For API testing (recommended)

### Development Environment
- IntelliJ IDEA (recommended)
- Git for version control

## Build and Deploy Instructions

### 1. Clone and Setup Project
```bash
git clone <repository-url>
cd webapp
```

### 2. Start PostgreSQL Database
```bash
# Start PostgreSQL container
docker-compose up -d

# Verify container is running
docker-compose ps
```

### 3. Build Application
```bash
# Clean and compile
mvn clean compile

# Run tests (optional)
mvn test
```

### 4. Run Application
```bash
# Start the application
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

### 5. Verify Deployment
```bash
# Health check
curl http://localhost:8080/health

# Expected response:
# {"status": "OK", "message": "Application is running"}
```

## Database Configuration

The application uses PostgreSQL with the following default settings:
- **Host**: localhost:5432
- **Database**: webapp_db
- **Username**: webapp_user
- **Password**: webapp_password

Database schema is automatically created using Hibernate DDL.

## API Documentation

### Authentication
- **Type**: HTTP Basic Authentication (Token-based, not Session-based)
- **Format**: `Authorization: Basic base64(email:password)`
- **Security**: All passwords encrypted with BCrypt + salt

### User Management APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/v1/user` | Create new user account | No |
| GET | `/v1/user/{id}` | Get user information | Yes |
| PUT | `/v1/user/{id}` | Update user information | Yes |

#### User Registration
```bash
POST /v1/user
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
}
```

#### User Authentication Example
```bash
# Base64 encode: user@example.com:password123
Authorization: Basic dXNlckBleGFtcGxlLmNvbTpwYXNzd29yZDEyMw==
```

### Product Management APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/v1/product` | Create new product | Yes |
| GET | `/v1/product` | Get all products | No |
| GET | `/v1/product/{id}` | Get specific product | No |
| GET | `/v1/product/user` | Get user's products | Yes |
| PUT | `/v1/product/{id}` | Update product | Yes (Owner only) |
| DELETE | `/v1/product/{id}` | Delete product | Yes (Owner only) |

#### Product Creation Example
```bash
POST /v1/product
Authorization: Basic <credentials>
Content-Type: application/json

{
    "name": "iPhone 15",
    "description": "Latest iPhone model",
    "sku": "IPH-15-001",
    "manufacturer": "Apple",
    "quantity": 50
}
```

### System APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/health` | Application health check | No |

## Security Features

### Authentication & Authorization
- **Token-based Authentication** (not session-based)
- **BCrypt password hashing** with salt
- **User isolation** - users can only access/modify their own data
- **Product ownership** - only product creators can modify/delete their products

### Input Validation
- Email format validation
- Password minimum length (8 characters)
- Required field validation
- Quantity cannot be negative
- Duplicate email/SKU prevention

### HTTP Status Codes
- **200 OK** - Successful operations
- **201 Created** - Resource created successfully
- **204 No Content** - Successful deletion
- **400 Bad Request** - Validation errors
- **401 Unauthorized** - Authentication required
- **403 Forbidden** - Access denied
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Server errors

## Testing the Application

### Using Postman

1. **Create User**
   ```
   POST http://localhost:8080/v1/user
   Body: {"email": "test@example.com", "password": "password123", "firstName": "Test", "lastName": "User"}
   ```

2. **Login and Get User Info**
   ```
   GET http://localhost:8080/v1/user/{user-id}
   Authorization: Basic Auth (test@example.com / password123)
   ```

3. **Create Product**
   ```
   POST http://localhost:8080/v1/product
   Authorization: Basic Auth
   Body: {"name": "Test Product", "sku": "TEST-001", "manufacturer": "Test Inc", "quantity": 10}
   ```

### Sample Test Flow
1. Create a user account
2. Authenticate and get user information
3. Create products with authentication
4. View all products (no auth needed)
5. Update/delete own products only
6. Verify security restrictions

## Project Structure

```
src/main/java/com/chs/webapp/
├── config/          # Security and application configuration
├── controller/      # REST API endpoints
├── dto/            # Data Transfer Objects
├── entity/         # JPA entities
├── repository/     # Data access layer
└── service/        # Business logic layer
```

## Technology Stack

- **Spring Boot 3.5.5** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database abstraction
- **Hibernate** - ORM framework
- **PostgreSQL** - Database
- **Maven** - Build tool
- **Docker** - Containerization
- **Lombok** - Code generation

## Development Notes

- Application uses `create-drop` DDL mode for development
- Database schema is automatically generated
- Passwords are never returned in API responses
- All timestamps are automatically managed
- Global exception handling for consistent error responses

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   ```bash
   # Ensure PostgreSQL container is running
   docker-compose ps
   docker-compose up -d
   ```

2. **Port 8080 Already in Use**
   ```bash
   # Change port in application.properties
   server.port=8081
   ```

3. **Authentication Failed**
   - Verify email/password are correct
   - Check Basic Auth encoding
   - Ensure user account exists

## Configuration

The application supports environment variable configuration:

| Variable | Default | Description |
|----------|---------|-------------|
| DATABASE_URL | jdbc:postgresql://localhost:5432/webapp_db | Database connection URL |
| DATABASE_USERNAME | webapp_user | Database username |
| DATABASE_PASSWORD | webapp_password | Database password |
| DDL_AUTO | update | Hibernate DDL mode |
| SERVER_PORT | 8080 | Application port |

### Using Environment Variables
```bash
export DATABASE_PASSWORD=your_password
mvn spring-boot:run