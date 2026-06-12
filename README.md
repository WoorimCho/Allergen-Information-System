# Allergen Information System

A web-based allergen management application built with Spring Boot and Thymeleaf that allows users to manage foods, ingredients, and recipes while tracking allergen information.

## Overview

The Allergen Information System provides a centralized platform for storing and managing food-related data. Users can create, view, modify, and delete foods, ingredients, and recipes while maintaining allergen information associated with each item.

The project follows the MVC (Model-View-Controller) architecture using Spring Boot and provides a user-friendly web interface for interacting with the system.

---

## Features

### Food Management
- View all foods
- Add new foods
- Modify existing foods
- Delete foods

### Ingredient Management
- View all ingredients
- Add new ingredients
- Modify ingredient information
- Delete ingredients

### Recipe Management
- View all recipes
- Add new recipes
- Modify recipe information
- Delete recipes

### Allergen Information
- Store allergen-related data
- Associate allergens with food items and ingredients
- Improve food safety awareness

### User Interface
- Responsive web-based interface
- Thymeleaf template rendering
- Clean navigation between system modules

---

## Tech Stack

### Backend
- Java
- Spring Boot
- Spring MVC
- Spring Data JPA

### Frontend
- HTML5
- CSS3
- Thymeleaf

### Database
- H2 Database / JPA Persistence

### DevOps
- Docker
- Docker Compose

### Build Tool
- Maven

### Testing
- JUnit
- Spring Boot Testing Framework

---

## Project Structure

```text
src
├── main
│   ├── java
│   │   └── ca
│   │       └── allergen_info_system
│   │           ├── Controllers
│   │           │   └── MainController.java
│   │           │
│   │           ├── Models
│   │           │   ├── Food.java
│   │           │   ├── Ingredient.java
│   │           │   ├── Recipe.java
│   │           │   └── RecipeKey.java
│   │           │
│   │           ├── Repositories
│   │           │   ├── FoodRepository.java
│   │           │   ├── IngredientRepository.java
│   │           │   └── RecipeRepository.java
│   │           │
│   │           └── Services
│   │               ├── BasicService.java
│   │               └── BasicServiceImpl.java
│   │
│   └── resources
│       ├── static
│       │   ├── css
│       │   │   └── mainmenu.css
│       │   └── index.html
│       │
│       ├── templates
│       │   ├── Error
│       │   ├── Food
│       │   ├── Ingredient
│       │   └── Recipe
│       │
│       └── application.properties
│
└── test
    └── java
        └── ca
            └── allergen_info_system
```

---

## Architecture

The application follows a layered architecture:

### Controller Layer
Handles incoming HTTP requests and routes user actions.

- `MainController`

### Service Layer
Contains business logic and acts as an intermediary between controllers and repositories.

- `BasicService`
- `BasicServiceImpl`

### Repository Layer
Provides database access using Spring Data JPA.

- `FoodRepository`
- `IngredientRepository`
- `RecipeRepository`

### Model Layer
Represents database entities and relationships.

- `Food`
- `Ingredient`
- `Recipe`
- `RecipeKey`

---

## Installation

### Prerequisites

- Java 17+
- Maven 3.8+
- Git

### Clone the Repository

```bash
git clone https://github.com/WoorimCho/Allergen-Information-System.git
cd Allergen-Information-System
```

### Build the Project

```bash
mvn clean install
```

### Run the Application

```bash
mvn spring-boot:run
```

Alternatively:

```bash
java -jar target/AllergenInformationSystem.jar
```

---

## Access the Application

After starting the application, open:

```text
http://localhost:8080
```

---

## Available Pages

### Food

- View all foods
- Create new food entries
- Modify food records
- Delete foods

### Ingredients

- View all ingredients
- Create ingredient entries
- Update ingredient information
- Delete ingredients

### Recipes

- View all recipes
- Create recipe records
- Update recipes
- Delete recipes

### Error Handling

Custom error pages are provided for invalid operations and missing records.

---

## Testing

Run all tests using:

```bash
mvn test
```

Test classes include:

- `TestAccessingDataMysqlApplication`
- `TestControllerConfiguration`

---

## Learning Outcomes

This project demonstrates:

- Java Application Development
- Spring Boot application development
- MVC architecture implementation
- CRUD operations
- Repository pattern
- Service-oriented design
- Thymeleaf templating
- Database integration using JPA
- Software testing with JUnit
- Docker Containerization
- Docker Compose Orchestration
- REST API endpoints
- RESTful Web Development
- Relational Database Design
- Git Version Control
- Software Testing with JUnit

---

## Future Improvements

- User authentication and authorization
- Advanced allergen filtering
- Search functionality
- Nutritional information tracking
- Recipe recommendations

---

## Author

**Woorim Cho**

Computer Science Graduate

GitHub: https://github.com/WoorimCho

---

## License

This project is intended for educational purposes.
