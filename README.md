# 🚀 Task Management Full Stack Application

> **Week 8 Project: Frontend-Backend Integration with React**

A complete full stack task management application built with **React TypeScript** frontend and **Spring Boot** backend. Features JWT authentication, real-time updates via WebSocket, drag-and-drop Kanban board, and responsive design with Tailwind CSS.

---

## 📋 Features

### Backend (Spring Boot)
- ✅ JWT authentication with refresh tokens
- ✅ REST API for task CRUD operations
- ✅ WebSocket for real-time updates
- ✅ H2 / PostgreSQL database support
- ✅ Spring Security with role-based access
- ✅ Comprehensive error handling
- ✅ API documentation with Swagger

### Frontend (React TypeScript)
- ✅ JWT authentication flow (login, register, auto-refresh)
- ✅ Drag-and-drop Kanban board
- ✅ Real-time updates via WebSocket
- ✅ Responsive design with Tailwind CSS
- ✅ Dark/light mode theme toggle
- ✅ Loading skeletons and error states
- ✅ Search and filter functionality

---

## 🛠️ Technology Stack

| Layer      | Technologies                                              |
|------------|-----------------------------------------------------------|
| **Frontend** | React 18, TypeScript, Vite, Tailwind CSS, Axios         |
| **Backend**  | Java 17, Spring Boot 3.x, Spring Security, Spring Data JPA |
| **Database** | H2 (dev), PostgreSQL (production)                       |
| **Real-time**| WebSocket (STOMP + SockJS)                              |
| **Auth**     | JWT (JSON Web Tokens) with refresh tokens               |
| **DevOps**   | Docker, Docker Compose, GitHub Actions                  |

---

## 🚀 Quick Start

### Prerequisites
- **Java 17+** (for backend)
- **Node.js 18+** and **npm** (for frontend)
- **Maven** (for backend build)

### Option 1: Manual Setup (Recommended for Development)

**Start the Backend:**
```bash
cd backend
mvn spring-boot:run
```
Backend runs at: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

**Start the Frontend:**
```bash
cd frontend
npm install
npm run dev
```
Frontend runs at: http://localhost:3000

### Option 2: Docker Compose
```bash
docker-compose up -d
```
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- PostgreSQL: localhost:5432

### Demo Credentials
| Role  | Email                    | Password |
|-------|--------------------------|----------|
| Admin | admin@taskmanager.com    | admin123 |
| User  | user@taskmanager.com     | user123  |

---

## 📁 Project Structure

```
week8-task-manager-fullstack/
├── backend/                          # Spring Boot Backend
│   ├── src/main/java/com/taskmanager/
│   │   ├── TaskManagerApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── WebSocketConfig.java
│   │   │   ├── CorsConfig.java
│   │   │   └── DataInitializer.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── TaskController.java
│   │   │   └── WebSocketController.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── TaskService.java
│   │   │   └── WebSocketService.java
│   │   ├── security/
│   │   │   ├── JwtTokenProvider.java
│   │   │   └── JwtAuthenticationFilter.java
│   │   ├── model/
│   │   │   ├── entity/ (User.java, Task.java)
│   │   │   ├── dto/ (TaskDto, AuthResponse, etc.)
│   │   │   └── enums/ (TaskStatus, TaskPriority, Role)
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   └── TaskRepository.java
│   │   └── exception/
│   │       └── GlobalExceptionHandler.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                         # React TypeScript Frontend
│   ├── src/
│   │   ├── components/
│   │   │   ├── TaskList.tsx
│   │   │   ├── TaskForm.tsx
│   │   │   └── Layout/Layout.tsx
│   │   ├── pages/
│   │   │   ├── Dashboard.tsx
│   │   │   ├── Login.tsx
│   │   │   └── Register.tsx
│   │   ├── services/
│   │   │   ├── api.ts
│   │   │   └── websocket.ts
│   │   ├── hooks/
│   │   │   ├── useTasks.ts
│   │   │   └── useAuth.ts
│   │   ├── context/
│   │   │   ├── AuthContext.tsx
│   │   │   └── TaskContext.tsx
│   │   ├── types/task.ts
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   └── Dockerfile
├── docker-compose.yml
├── DOCUMENTATION.md
├── README.md
└── .gitignore
```

---

## 📡 API Endpoints

### Authentication
| Method | Endpoint             | Description         |
|--------|----------------------|---------------------|
| POST   | `/api/auth/register` | Register new user   |
| POST   | `/api/auth/login`    | Login & get tokens  |
| POST   | `/api/auth/refresh`  | Refresh JWT token   |
| POST   | `/api/auth/logout`   | Invalidate token    |

### Tasks
| Method | Endpoint               | Description           |
|--------|------------------------|-----------------------|
| GET    | `/api/tasks`           | List tasks (paginated)|
| POST   | `/api/tasks`           | Create task           |
| GET    | `/api/tasks/{id}`      | Get task by ID        |
| PUT    | `/api/tasks/{id}`      | Update task           |
| DELETE | `/api/tasks/{id}`      | Delete task           |
| PUT    | `/api/tasks/{id}/status` | Update task status  |
| GET    | `/api/tasks/search?q=` | Search tasks          |

### WebSocket
| Endpoint    | Description                      |
|-------------|----------------------------------|
| `/ws`       | WebSocket connection (SockJS)    |
| `/topic/tasks` | Subscribe for task updates    |

---

## 👤 Author

Built as part of **TDA Week 8** assignment — Frontend-Backend Integration with React.
