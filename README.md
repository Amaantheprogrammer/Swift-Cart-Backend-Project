# 🛒 Swift Cart — E-Commerce Backend Platform

**A secure, modular, and performance-conscious e-commerce backend built with Java and Spring Boot.**

Swift Cart is a RESTful e-commerce backend designed to handle the core operations of an online shopping platform, including user authentication, product discovery, shopping carts, wishlists, order processing, and external service integration.

Built using **Spring Boot 3, Spring Security, MySQL, Redis, and JWT**, the project focuses on secure API development, role-based access control, maintainable architecture, and integration with external services.

The next phase of Swift Cart is focused on exploring **Generative AI and Retrieval-Augmented Generation (RAG)** to make product discovery and shopping assistance more intelligent.

🌐 **Live Application:** https://e-commercebackendproject.onrender.com

📚 **API Documentation:** [Explore Swagger UI](https://e-commercebackendproject.onrender.com/swagger-ui/index.html)

---

## ✨ Key Highlights

* 🔐 **Secure Authentication:** JWT-based authentication with stateless session management and BCrypt password hashing.
* 👥 **Role-Based Authorization:** Separate access permissions for administrators, customers, and sellers.
* 📦 **Product Discovery:** Product search, category-based filtering, price filtering, and pagination.
* 🛍️ **Shopping Experience:** Dedicated APIs for shopping carts, wishlists, and order placement.
* ⚡ **Redis Integration:** Configurable caching for frequently accessed application data.
* 💳 **External Service Integration:** REST client integration with a digital banking service for account and transaction operations.
* 🧩 **Modular Architecture:** Features organized into dedicated controllers, services, repositories, entities, and DTOs.
* 🛡️ **Centralized Error Handling:** Consistent exception handling and request validation.
* 📚 **API Documentation:** Interactive API documentation using OpenAPI and Swagger UI.
* ☁️ **Cloud Deployment:** Deployed application with environment-based configuration.
* 🤖 **AI Roadmap:** Planned exploration of LLMs, LangChain, and RAG for intelligent product discovery and shopping assistance.

---

## 🚀 Features

### 1. Authentication & Security

Swift Cart uses Spring Security and JSON Web Tokens to secure its REST APIs.

Implemented capabilities include:

* User registration and login.
* JWT generation and validation.
* Stateless authentication.
* Custom JWT authentication filter.
* BCrypt password hashing.
* Role-based access control.
* Retrieval of the currently authenticated user.
* Protected endpoints based on application permissions.

**Authentication endpoints**

| Method | Endpoint        | Description                          |
| ------ | --------------- | ------------------------------------ |
| POST   | `/auth/sign-up` | Register a new user                  |
| POST   | `/auth/sign-in` | Authenticate a user and obtain a JWT |

Authenticated requests use the following header:

```http
Authorization: Bearer <your-jwt-token>
```

### 2. User Management

User-related APIs support account management and authenticated user operations.

Capabilities include:

* Retrieve user information.
* Update user details.
* Delete user accounts.
* Retrieve user-specific information.
* Manage user roles and payment preferences through the application's user model.

### 3. Product Management & Discovery

Product APIs support product creation, maintenance, and discovery.

Capabilities include:

* Create, retrieve, update, and delete products.
* Retrieve products using pagination.
* Retrieve individual products by ID.
* Search products by name.
* Apply optional price filters.
* Browse products by category.
* Retrieve products belonging to the authenticated seller.
* Add, update, and delete product images.

**Example API requests**

Retrieve a paginated list of products:

```http
GET /products?page=0&size=10
```

Retrieve a product by ID:

```http
GET /products/1
```

Search for products by name:

```http
GET /products/search?name=phone
```

Search for products by name with a price filter:

```http
GET /products/search?name=phone&price=50000
```

Retrieve products belonging to a particular category:

```http
GET /products/category/1
```

*Note: These examples assume the endpoint paths and query parameters match the deployed application's controller mappings.*

### 4. Category Management

Category APIs organize products into meaningful groups.

Capabilities include:

* Create categories.
* Retrieve categories.
* Update categories.
* Delete categories.
* Associate products with categories.
* Retrieve products belonging to a category.

### 5. Shopping Cart

Swift Cart provides dedicated APIs for managing a customer's shopping cart.

Capabilities include:

* Retrieve the authenticated user's cart.
* Add products to the cart.
* Update product quantities.
* Remove individual products.
* Clear the entire cart.
* Place orders directly from the cart.

**Cart endpoints**

| Method | Endpoint                   | Description                          |
| ------ | -------------------------- | ------------------------------------ |
| GET    | `/carts/my-cart`           | Retrieve the current user's cart     |
| POST   | `/carts/items`             | Add a product or update its quantity |
| DELETE | `/carts/items/{productId}` | Remove a product from the cart       |
| DELETE | `/carts/clear`             | Clear the cart                       |

### 6. Wishlist Management

Customers can maintain a separate collection of products they want to revisit.

Capabilities include:

* Retrieve the current user's wishlist.
* Add products to the wishlist.
* Remove products from the wishlist.
* Clear the wishlist.

| Method | Endpoint                 | Description           |
| ------ | ------------------------ | --------------------- |
| GET    | `/wishlists`             | Retrieve the wishlist |
| POST   | `/wishlists/{productId}` | Add a product         |
| DELETE | `/wishlists/{productId}` | Remove a product      |
| DELETE | `/wishlists`             | Clear the wishlist    |

### 7. Order Management

The order module handles order placement and subsequent order operations.

Capabilities include:

* Place orders.
* Place orders using the shopping cart.
* Retrieve orders with pagination.
* Retrieve individual orders.
* Retrieve orders associated with a user.
* Retrieve the authenticated user's orders.
* Cancel orders.
* Update order statuses.
* Update payment statuses.

**Order endpoints**

| Method | Endpoint                           | Description                        |
| ------ | ---------------------------------- | ---------------------------------- |
| GET    | `/orders`                          | Retrieve orders with pagination    |
| GET    | `/orders/{orderId}`                | Retrieve an order by ID            |
| GET    | `/orders/user/{userId}`            | Retrieve orders for a user         |
| GET    | `/orders/me`                       | Retrieve the current user's orders |
| POST   | `/orders/place-order`              | Place an order directly            |
| POST   | `/orders/cart/place-order`         | Place an order using the cart      |
| PATCH  | `/orders/{orderId}/cancel`         | Cancel an order                    |
| PATCH  | `/orders/{orderId}/order-status`   | Update order status                |
| PATCH  | `/orders/{orderId}/payment-status` | Update payment status              |

Actual access permissions and business rules are enforced by the application's security configuration and service layer.

### 8. External Digital Banking Integration

Swift Cart includes a dedicated REST client that communicates with an external digital banking service.

The integration uses Spring's `RestClient` and forwards the authenticated user's bearer token when making supported requests.

Implemented operations include:

* Retrieve the user's accounts.
* Retrieve an account using its account number.
* Initiate withdrawal requests.
* Initiate deposit requests.

This module demonstrates how a Spring Boot application can communicate with another backend service while propagating authentication credentials.

**Important:** The external banking service must be configured and available for these operations to work. This integration should not be interpreted as a claim that a production payment gateway has been implemented.

### 9. Redis Caching

Swift Cart includes Redis-backed Spring caching configuration.

The configured cache regions cover frequently accessed data, including:

* Products.
* Products retrieved by ID.
* Product search results.
* Products filtered by category and price.
* Categories.
* Users.
* Users retrieved by ID.
* Users retrieved by email.

Configured cache expiration periods range from 10 minutes to 12 hours, depending on the cache region.

Redis is intended to reduce repeated database work for suitable read operations.

The actual performance benefit depends on which service methods use caching, cache invalidation behavior, and the deployed Redis configuration.

### 10. Validation & Exception Handling

The application uses Jakarta Bean Validation and centralized exception handling.

Capabilities include:

* Request payload validation.
* Resource-not-found handling.
* Bad-request handling.
* Duplicate-resource handling.
* Insufficient-resource handling.
* Consistent error response structures.

Centralized exception handling is implemented using Spring's `@RestControllerAdvice`.

---

## 🛠️ Technology Stack

| Technology               | Purpose                          |
| ------------------------ | -------------------------------- |
| Java 21                  | Core programming language        |
| Spring Boot 3            | Application framework            |
| Spring Web               | REST API development             |
| Spring Security 6        | Authentication and authorization |
| JWT                      | Stateless authentication         |
| BCrypt                   | Password hashing                 |
| Spring Data JPA          | Database persistence             |
| Hibernate ORM            | Object-relational mapping        |
| MySQL                    | Relational database              |
| Redis                    | Caching infrastructure           |
| Spring RestClient        | External service communication   |
| Spring Boot Actuator     | Application monitoring endpoints |
| Springdoc OpenAPI        | API documentation                |
| Maven                    | Dependency management and builds |
| Lombok                   | Boilerplate reduction            |
| ModelMapper              | Object mapping                   |
| JUnit & Spring Boot Test | Automated testing                |

---

## 🏗️ Architecture

Swift Cart follows a modular, layered backend structure.

```text
                         Client
                            |
                            v
                   Spring Boot REST API
                            |
                +-----------+-----------+
                |                       |
                v                       v
          Spring Security          Controllers
                |                       |
                v                       v
        JWT Authentication           Services
                |                       |
                |              +--------+--------+
                |              |                 |
                |              v                 v
                |        Repositories      External REST Client
                |              |                 |
                |              v                 v
                |            MySQL       Digital Banking Service
                |
                +---- Role-Based Authorization

                     Redis Cache
                          |
                          v
                  Cached Application Data
```

### Architectural Responsibilities

**Controllers**

* Expose HTTP endpoints.
* Accept requests and return HTTP responses.
* Delegate business operations to service classes.

**Services**

* Implement application business logic.
* Coordinate repositories and other services.
* Handle operations such as cart updates and order placement.

**Repositories**

* Provide database access through Spring Data JPA.

**DTOs**

* Define request and response structures.
* Separate API payloads from persistence entities.

**Security**

* Validate JWTs.
* Establish authenticated user context.
* Enforce configured authorization rules.

**External REST Client**

* Communicates with the digital banking service.
* Forwards authentication credentials for supported operations.

**Redis**

* Provides the configured caching infrastructure for suitable application data.

This separation helps keep API handling, business logic, persistence, and external integrations independently maintainable.

---

## 👥 User Roles

Swift Cart defines three application roles:

| Role          | Intended Responsibilities                                                 |
| ------------- | ------------------------------------------------------------------------- |
| `ROLE_ADMIN`  | Administrative operations and authorized user and order management        |
| `ROLE_USER`   | Product discovery, shopping cart, wishlist, and customer order operations |
| `ROLE_SELLER` | Product creation and management for sellers                               |

The exact permissions available to each role depend on the endpoint authorization rules implemented in the application.

---

## 📂 Project Structure

The following illustrates the main feature modules and architectural layers.

```text
src/
├── main/
│   ├── java/com/myProject/
│   │   ├── auth/
│   │   ├── cart/
│   │   ├── category/
│   │   ├── order/
│   │   ├── payment/
│   │   ├── product/
│   │   ├── user/
│   │   ├── wishlist/
│   │   ├── rest_client/
│   │   ├── security/
│   │   │   ├── jwt/
│   │   │   └── user/
│   │   ├── config/
│   │   ├── exception/
│   │   ├── admin_seeder/
│   │   ├── health/
│   │   └── ECommerceBackendProjectApplication.java
│   │
│   └── resources/
│       └── application.yml
│
└── test/
    └── java/com/myProject/
```

Feature modules may contain controllers, DTOs, entities, repositories, and services according to their implementation.

---

## 📚 API Documentation

Swift Cart uses Springdoc OpenAPI to provide interactive API documentation.

### Live API Documentation

**[Open Swagger UI](https://e-commercebackendproject.onrender.com/swagger-ui/index.html)**

### Local API Documentation

When running the application locally, open:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger UI allows you to:

* Explore documented endpoints.
* Inspect request and response schemas.
* Review HTTP methods and parameters.
* Execute API requests.
* Test authenticated endpoints using a JWT.

---

## ⚙️ Configuration

Swift Cart uses **`application.yml`** for application configuration and environment-specific settings.

The configuration supports environment variables for database credentials, Redis, JWT authentication, and the server port.

### Environment Variables

| Variable                     | Purpose                        | Default                                 |
| ---------------------------- | ------------------------------ | --------------------------------------- |
| `SPRING_DATASOURCE_URL`      | MySQL JDBC connection URL      | Local MySQL database URL                |
| `SPRING_DATASOURCE_USERNAME` | Database username              | `root`                                  |
| `SPRING_DATASOURCE_PASSWORD` | Database password              | `root`                                  |
| `REDIS_URL`                  | Redis connection URL           | `redis://localhost:6379`                |
| `JWT_SECRET`                 | JWT signing secret             | Configured fallback in the current YAML |
| `JWT_EXPIRATION`             | JWT expiration in milliseconds | `3600000`                               |
| `PORT`                       | HTTP server port               | `8080`                                  |

### Application YAML

The following excerpts illustrate the configuration used by the application.

**MySQL and JPA**

```yaml
spring:
  application:
    name: E-CommerceBackendProject

  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/ecommerceDb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}
    username: ${SPRING_DATASOURCE_USERNAME:root}
    password: ${SPRING_DATASOURCE_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    open-in-view: false
    properties:
      hibernate:
        format_sql: true
```

**Database schema validation**

The `ddl-auto: validate` setting instructs Hibernate to validate the database schema against the application's entity mappings rather than automatically creating or updating tables.

Ensure that the required schema exists before starting the application.

**Redis caching**

```yaml
spring:
  data:
    redis:
      url: ${REDIS_URL:redis://localhost:6379}

  cache:
    type: redis
```

Redis is configured as the application's cache provider. A reachable Redis instance is required for the configured caching infrastructure to operate.

**JWT authentication**

```yaml
jwt:
  secret: ${JWT_SECRET:your-development-secret}
  expiration: ${JWT_EXPIRATION:3600000}
```

The example above illustrates the configuration structure. The actual project currently contains a hardcoded fallback value for `JWT_SECRET`.

The default expiration of `3600000` milliseconds represents one hour.

**Server port**

```yaml
server:
  port: ${PORT:8080}
```

The application uses the port provided by the deployment environment when `PORT` is set, otherwise defaulting to port `8080`.

### Security Considerations

* Configure a strong, unique JWT signing secret through `JWT_SECRET` in deployed environments.
* Avoid using a hardcoded fallback secret in production.
* Never commit production credentials or secrets to version control.
* Use appropriate database credentials instead of development defaults.
* Consider disabling SQL statement logging in production unless it is needed for debugging.

**Important:** If a publicly deployed application has used the fallback JWT secret, configure a new secret and rotate it. Existing tokens signed with the previous secret will no longer validate after rotation.

---

## ▶️ Getting Started

### Prerequisites

Install the following:

* Java Development Kit (JDK) 21.
* MySQL.
* Redis.
* Git.

You will also need access to the external digital banking service to use its integration endpoints.

### 1. Clone the Repository

```bash
git clone <your-github-repository-url>
```

### 2. Navigate to the Project

```bash
cd Swift-Cart-Backend-Project-main
```

Use the actual directory name created when you clone your repository.

### 3. Configure MySQL

Create the MySQL database and ensure that the required tables are present.

The default local configuration expects:

```text
Database: ecommerceDb
Username: root
Password: root
```

These are development defaults from the YAML configuration, not recommended production credentials.

Alternatively, configure your own values through environment variables.

**Windows PowerShell:**

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/ecommerceDb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:SPRING_DATASOURCE_USERNAME="your-username"
$env:SPRING_DATASOURCE_PASSWORD="your-password"
```

### 4. Configure Redis

Ensure that Redis is running and accessible.

The default configuration expects Redis at:

```text
redis://localhost:6379
```

To use a different Redis instance, configure `REDIS_URL` accordingly.

### 5. Configure JWT

Set `JWT_SECRET` to a strong, unique secret appropriate for your environment.

For example, in Windows PowerShell:

```powershell
$env:JWT_SECRET="your-unique-development-secret"
$env:JWT_EXPIRATION="3600000"
```

Use a securely generated secret rather than copying a publicly visible example into a deployed environment.

### 6. Configure External Services

Configure any environment-specific settings required by the digital banking service.

Ensure the service is reachable and that the authentication credentials required by the integration are available.

### 7. Run the Application

**Windows:**

```bash
mvnw.cmd spring-boot:run
```

**Linux/macOS:**

```bash
./mvnw spring-boot:run
```

Alternatively, build the application:

```bash
./mvnw clean package
```

Then execute the generated JAR from the build output directory.

### 8. Explore the APIs

Open Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Register an account, authenticate to obtain a JWT, and use the token to access protected endpoints according to their configured permissions.

> **Configuration note:** The commands above illustrate a local development setup. The database schema, JWT settings, Redis instance, and any required external services must be configured appropriately before the application can run successfully.

---

## 🧪 Testing

The project includes a test structure using Spring Boot Test and service-level tests.

Run the test suite using:

**Windows:**

```bash
mvnw.cmd test
```

**Linux/macOS:**

```bash
./mvnw test
```

Testing should cover critical application workflows, including:

* Authentication and authorization.
* Product management.
* Shopping cart operations.
* Wishlist operations.
* Order placement and cancellation.
* Validation and exception handling.
* Cache behavior.
* External service integration.

Expanding automated test coverage for security rules, business logic, and failure scenarios is a valuable next step.

---

## 🤖 Future Roadmap: AI-Powered Shopping Assistant

The next phase of Swift Cart explores how Generative AI can improve product discovery and the overall shopping experience.

Rather than introducing a chatbot that simply generates generic responses, the objective is to build an assistant that retrieves relevant product information and uses it to answer customer questions.

**The following are planned enhancements, not currently implemented features.**

### Phase 1: Product Knowledge Base

Build a searchable knowledge base from Swift Cart's product catalogue.

Planned tasks:

* Ingest product descriptions, specifications, and category information.
* Clean and prepare product data.
* Chunk text where appropriate.
* Generate vector embeddings.
* Store embeddings in a vector database.
* Retrieve relevant product information through semantic similarity.

### Phase 2: Retrieval-Augmented Generation (RAG)

Develop a shopping assistant that retrieves relevant product information before generating a response.

Potential customer queries include:

* "Which laptop is suitable for programming under ₹60,000?"
* "Compare these two products."
* "Find products with features similar to this one."
* "What are the main differences between these products?"
* "Which products match my requirements?"

The system would retrieve relevant information from the product catalogue and provide it to the language model as context.

This approach aims to make responses more relevant and grounded in the application's actual product data.

### Phase 3: Spring Boot and AI Integration

Keep Swift Cart's existing Spring Boot backend as the core business application while introducing a dedicated AI service.

A possible architecture is:

```text
                    Customer
                       |
                       v
                Swift Cart API
                 Spring Boot
                       |
                       v
                AI Assistant API
                    Python
                       |
             +---------+---------+
             |                   |
             v                   v
       Retrieval Pipeline    Language Model
             |
             v
       Embedding Model
             |
             v
       Vector Database
             |
             v
      Relevant Product Data
             |
             v
      Contextual Generation
             |
             v
        Assistant Response
```

The AI service could use Python, LangChain, and a suitable embedding model while communicating with the Spring Boot backend through an internal REST API.

The final technology choices will depend on evaluation, cost, latency, and deployment requirements.

### Phase 4: Context-Aware Product Discovery

Potential improvements include:

* Natural-language product search.
* Semantic recommendations based on customer requirements.
* Product comparisons grounded in retrieved product data.
* Follow-up questions that preserve conversational context.
* Product references in generated responses.
* Better handling of ambiguous shopping requests.

### Phase 5: Reliable AI Workflows

Further development could explore:

* Retrieval evaluation.
* Hallucination reduction.
* Grounded answers with product references.
* Response quality and latency monitoring.
* Authentication and authorization for AI endpoints.
* Protection against unauthorized access to customer-specific information.
* Fallback behavior when relevant product information cannot be retrieved.

The objective is to integrate AI into the existing application without allowing an LLM to bypass authorization rules or independently perform sensitive business operations.

---

## 🎯 Engineering Concepts Demonstrated

Swift Cart brings together several backend engineering concepts:

* RESTful API design.
* Layered application architecture.
* Authentication and authorization.
* JWT-based stateless security.
* Role-based access control.
* Relational database modeling.
* Object-relational mapping.
* Pagination and filtering.
* DTO-based request and response handling.
* Centralized exception handling.
* Request validation.
* Redis caching configuration.
* External REST API integration.
* Environment-based configuration.
* Application deployment.
* Automated testing.
* API documentation.
* Generative AI and RAG integration as a planned extension.

---

## 🗺️ Future Backend Improvements

Beyond AI integration, potential improvements include:

* Refresh token workflows.
* Email verification and password recovery.
* Expanded unit and integration test coverage.
* CI/CD automation.
* Improved cache invalidation and performance measurement.
* More comprehensive API authorization tests.
* Production-oriented observability and structured logging.
* Improved inventory consistency and concurrency handling.
* Payment workflow hardening.
* Containerized development and deployment.
* Improved management of environment-specific configuration.

These improvements should be prioritized according to application requirements rather than treated as completed capabilities.

---

## 👨‍💻 Author

**Amaan Shakil Coatwala**

Backend development interests:

* Java
* Spring Boot
* Spring Security
* REST API Development
* MySQL and JPA
* Redis
* Backend Architecture
* Generative AI
* LangChain
* Retrieval-Augmented Generation

---

## ⭐ Support

If you find Swift Cart interesting, consider starring the repository.

Suggestions, issues, and contributions that improve the application's architecture, security, performance, and functionality are welcome.
