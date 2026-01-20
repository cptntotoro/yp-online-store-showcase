# 🛍️ Online Store Showcase | Reactive Spring Boot + Java 21

[![Java CI with Maven](https://github.com/cptntotoro/yp-online-store-showcase/actions/workflows/maven.yml/badge.svg)](https://github.com/cptntotoro/yp-online-store-showcase/actions/workflows/maven.yml) [![Coverage Status](https://coveralls.io/repos/github/cptntotoro/yp-online-store-showcase/badge.svg)](https://coveralls.io/github/cptntotoro/yp-online-store-showcase)

[![Static Badge](https://img.shields.io/badge/%F0%9F%8C%90_Language-Русский-blue)](README-ru.md)
[![Static Badge](https://img.shields.io/badge/%F0%9F%8C%90_Language-English-blue)](README.md)

A modern, reactive web application demonstrating an online product catalog with full e-commerce functionality built with
Java 21 and Spring Boot 3.

## 📋 Table of Contents

- [Live Demo](#-live-demo--quick-overview)
- [Technology Stack](#-technology-stack)
- [About The Project](#-about-the-project)
    - [User Roles & Features](#-user-roles--features)
    - [Authentication & Security](#-authentication--security)
    - [Payment & Balance System](#-payment--balance-system)
    - [Technical Highlights](#-technical-highlights)
    - [UI/UX Features](#-uiux-features)
- [How to Run](#-how-to-run)
    - [Using Docker (Recommended)](#using-docker-recommended)
    - [Local Development](#local-development)
- [Testing](#-testing)

## 🚀 Live Demo & Quick Overview

![Application Demo](demo.gif)

## 🛠️ Technology Stack

| Category           | Technologies                                              |
|--------------------|-----------------------------------------------------------|
| **Backend**        | Java 21, Spring Boot 3 (WebFlux, R2DBC, Security, OAuth2) |
| **Databases**      | PostgreSQL, Redis (caching)                               |
| **Infrastructure** | Docker, Testcontainers                                    |
| **UI**             | Thymeleaf, HTML, CSS, JavaScript                          |
| **Tools**          | Lombok, MapStruct, OpenAPI Generator                      |

## 📋 About The Project

This web application serves as a fully-functional product showcase with e-commerce capabilities, built using reactive
programming principles for better scalability and performance.

### 👤 User Roles & Features

#### **Unauthenticated User**

- Browse product catalog
- View product details

#### **Authenticated User** (registration/login required)

- Full cart management (add/remove items)
- Order placement, payment, and cancellation
- Product creation (stock replenishment)
- Session management with "Remember Me" functionality

### 🔐 Authentication & Security

- **Two login modes:**
    - *Temporary session*: Session expires when browser closes (default)
    - *Persistent session*: Session restored after browser restart ("Remember Me" enabled)
- OAuth 2.0 Authorization Server using Client Credentials Flow for payment service authorization
- "Remember Me" authentication uses Base64-encoded tokens to reconstruct full Authentication objects with UserDetails

### 💳 Payment & Balance System

- Default user balance: 15,000 RUB (configurable via `payment.default-balance`)
- Successful order cancellations refund the amount to user's balance
- Insufficient balance prevents payment with appropriate user notification

### ⚙️ Technical Highlights

#### **Architecture & Performance**

- Redis caching for user carts and product lists to reduce application load
- Reactive programming model with Spring WebFlux for better scalability
- `@ControllerAdvice` implementation for real-time cart total updates and dynamic UI state management

#### **API & Integration**

- Auto-generated client (showcase-service) and controller (payment-service) from OpenAPI specifications
- Service-to-service communication between showcase and payment services

#### **Testing**

- Comprehensive integration tests using Testcontainers
- Docker-in-Docker setup for executing Testcontainers tests within Docker containers

### 🎨 UI/UX Features

- Clear error messaging during registration and authentication
- Graceful degradation when payment service is unavailable:
    - Payment and cancellation buttons are disabled
    - Informative messages displayed to users
- Real-time balance validation with user feedback

## 🚀 How to Run

### Using Docker (Recommended)

#### Run Only Tests (Showcase & Payment Services)

```
docker-compose --profile test up --build --abort-on-container-exit
```

Exit code 0 indicates all tests passed successfully.

#### Run Full Application (All 3 Services)

```
docker-compose --profile prod up -d
```

Application will be available at: http://localhost:8080/products

#### Stop Containers

To stop all running containers:

```
docker-compose down
```

To remove volumes:

```
docker-compose down -v
```

### Local Development

#### Prerequisites:

- PostgreSQL running on port 5432
- Redis running on port 6379
- Docker Desktop (for Testcontainers)
- Maven 3.6+
- Java 21

#### Steps:

Build all services (including tests):

```
mvn clean package
```

(Optional) Build authorization service:

```
mvn clean install -pl auth-service
```

#### Run the showcase service:

```
java -jar showcase-service/target/showcase-service.jar --spring.profiles.active=prod
```

(Optional) Run payment service separately:

```
java -jar payment-service/target/payment-service.jar --spring.profiles.active=prod
```

Access the application at: http://localhost:8080/products

## 🧪 Testing

The project includes comprehensive testing:

- Unit tests with JUnit 5 and Mockito
- Integration tests with Testcontainers
- End-to-end Docker-based testing
- Code coverage tracking via Coveralls

## 🤝 Contributing
Feel free to fork the repository and submit pull requests for any improvements.

## 📄 License
This project is for demonstration purposes as part of a learning portfolio.

- - -

Built with ❤️ using Spring Boot, Java 21, and reactive programming principles
