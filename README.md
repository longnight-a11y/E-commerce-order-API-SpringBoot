![CI](https://github.com/longnight-a11y/E-commerce-order-API-SpringBoot/actions/workflows/ci.yml/badge.svg)

# E-commerce Order API

## Spring Security

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