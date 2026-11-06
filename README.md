![CI](https://github.com/longnight-a11y/E-commerce-order-API-SpringBoot/actions/workflows/ci.yml/badge.svg)

# E-commerce Order API

## Overview

---
## Features

### Authentication

### Product Management

### User Interface
- OpenAPI/SwaggerUI documentation

### Exception Handling

### Asynchronous Operation

### Technical Highlights
- Using pagination to display lists of
products, category, cart items, and orders for scalability
---
## Spring Security

Architecture: 

```
Client
  │
  │ Authorization: Bearer <JWT>
  ↓
Spring Security
  │
  ├─ BearerTokenAuthenticationFilter
  │
  ├─ JwtDecoder
  │
  ├─ JwtAuthenticationConverter
  │       ↓
  │    ROLE_CUSTOMER
  │    ROLE_SELLER
  │    ROLE_ADMIN
  │
  ↓
SecurityContextHolder
  │
  ↓
Authorization
  │
  ├─ CUSTOMER
  ├─ SELLER
  └─ ADMIN
  │
  ↓
Controller
  ↓
Service
```

---
## API Endpoints

### Authentication
- Login | POST /auth/login

### User
- Create User | POST /users
- Get My Information | GET /users/me

### Product
- Sell New Product (SELLER or ADMIN only) | POST /products
- Get All Product List | GET /products
- Get a Single Product | GET /products/{productId}
- Get Product List of a Specific Seller | GET /products/seller/{sellerId}
- Get Product List of a Specific Category | GET /products/category/{categoryId}
- Update Product | PATCH /products/{productsId}
- Delete Product | DELETE /products/{productId}

### Cart
- Add Item to My Cart | POST /cart
- Get My Cart Items | GET /cart
- Update Quantity of Cart Item | PUT /cart/{productId}
- Remove Cart Item | DELETE /cart/{productId}

### Category
- Crate New Category (SELLER or ADMIN only) | POST /category
- Get All Categories | GET /category

### Order
- Checkout All Cart Items | POST /order/checkout
- Buy a Product Right Now | POST /order/buy-now
- Get My Orders | GET /order
- Get an Order | GET /order/{orderId}
- Cancel an Order | POST /order/{orderId}/cancel

### Admin (ADMIN only)
- Update Category | PATCH /admin/category/{categoryId}

---
## API Examples

---
## Databases

---
## Future Improvements

---
## Author 
- Mirai (longnight-a11y)