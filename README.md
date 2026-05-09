# 🎫 Ticket Management System

A full-stack **Ticket Management System** developed using **Spring Boot**, **Thymeleaf**, **Spring Security**, **JPA**, and **MySQL**.

The application is designed to streamline support ticket handling, department workflows, customer issue tracking, and internal communication within an organization.

---

# 🚀 Features

## 🔐 Authentication & Security
- User Login & Registration
- Role-Based Access Control (RBAC)
- Spring Security Authentication
- Secure Password Management
- Forgot & Reset Password Functionality

---

## 🎟️ Ticket Management
- Create Support Tickets
- Update Ticket Status
- Ticket Tracking System
- Ticket History Management
- Department-wise Ticket Allocation
- Ticket Detail View
- Ticket Status Monitoring

---

## 🏢 Department Modules
- HR Department
- Finance Department
- IT Support Department
- Operations Department
- Legal Department

---

## 👤 User Features
- User Dashboard
- Profile Management
- Customer Support Chat
- View Submitted Tickets
- Service Request Handling

---

## 👨‍💼 Admin Features
- Manage Departments
- Manage Technical Support Teams
- Monitor Ticket Activities
- User Management
- Ticket Workflow Monitoring

---

## 📧 Additional Functionalities
- Email Integration using Spring Mail
- Excel File Support using Apache POI
- Thymeleaf Layout Dialect Integration
- Responsive UI Design
- MVC Architecture
- Form Validation using Jakarta Validation

---

# 🛠️ Tech Stack

| Technology | Version / Usage |
|---|---|
| Java | 17 |
| Spring Boot | 3.5 |
| Spring Security | Authentication & Authorization |
| Spring Data JPA | Database Operations |
| Thymeleaf | Server Side Rendering |
| MySQL | Relational Database |
| Maven | Build Tool |
| Apache POI | Excel Processing |
| Lombok | Boilerplate Reduction |

---

# 📂 Project Structure

```text
TicketManagementSystem/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── railbit/
│   │   │           └── TicketManagementSystem/
│   │   │               ├── controller/
│   │   │               ├── service/
│   │   │               ├── repository/
│   │   │               ├── entity/
│   │   │               ├── config/
│   │   │               ├── security/
│   │   │               └── TicketManagementSystemApplication.java
│   │   │
│   │   ├── resources/
│   │   │   ├── static/
│   │   │   │   ├── css/
│   │   │   │   ├── js/
│   │   │   │   └── images/
│   │   │   │
│   │   │   ├── templates/
│   │   │   │   ├── customer/
│   │   │   │   ├── departments/
│   │   │   │   ├── layout/
│   │   │   │   └── error/
│   │   │   │
│   │   │   └── application.properties
│   │   │
│   │   └── uploads/
│   │
│   └── test/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
