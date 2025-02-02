# AntiFraud Application

An enterprise-ready anti-fraud application built using Spring Boot. This application validates transactions, manages suspicious IPs and stolen cards, and provides robust user management with security controls. It’s designed with best practices in mind and includes comprehensive tests to ensure reliability and security.

## Overview

The AntiFraud Application processes transactions by applying business rules, cross-checking against lists of suspicious IP addresses and stolen cards, and enforcing user access controls. The application is built to be secure, scalable, and maintainable—qualities that make it ideal for enterprise use.

## Features

- **Transaction Validation:**  
  Evaluate transactions based on amount thresholds, IP/card blacklists, and historical correlations to decide if a transaction is allowed, requires manual review, or must be prohibited.

- **Suspicious IP & Stolen Card Management:**  
  Add, list, and remove suspicious IP addresses and stolen card numbers to protect against fraudulent activities.

- **User Management & Security:**  
  Support for user registration, role management (MERCHANT, SUPPORT, ADMINISTRATOR), and account locking/unlocking, all secured using Spring Security.

- **Dynamic Limit Adjustment:**  
  Feedback on transactions automatically adjusts internal limits, ensuring that the system adapts over time.

- **Comprehensive Testing:**  
  The project includes unit tests, integration tests, and end-to-end tests (using MockMvc and an in-memory H2 database) to ensure the application’s stability and security.

## Technologies

- **Java 17** (or later)
- **Spring Boot 3.x**
- **Spring Security**
- **Spring Data JPA**
- **H2 Database** (for development and testing)
- **Gradle** (build automation)
- **JUnit 5 / Mockito** (testing framework)
- **Jackson** (JSON serialization/deserialization)
