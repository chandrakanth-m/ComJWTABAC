# Angular Spring Boot Application

This is a full-stack application with an Angular frontend and Spring Boot backend.

## Project Structure

- `frontend/`: Angular application
- `backend/`: Spring Boot application

## Prerequisites

- Node.js and npm (for Angular)
- Java 17 (for Spring Boot)
- Maven (for Spring Boot)

## Running the Backend

1. Navigate to the backend directory:
   ```
   cd backend
   ```

2. Build and run the Spring Boot application:
   ```
   mvn spring-boot:run
   ```

3. The backend will be available at http://localhost:8080

## Running the Frontend

1. Navigate to the frontend directory:
   ```
   cd frontend
   ```

2. Install dependencies:
   ```
   npm install
   ```

3. Start the Angular development server:
   ```
   npm start
   ```

4. The frontend will be available at http://localhost:4200

## Features

- RESTful API with Spring Boot
- Angular frontend with components for listing, viewing, creating, and editing tasks
- H2 in-memory database for data storage
- Bootstrap for responsive UI

## API Endpoints

- `GET /api/tasks`: Get all tasks
- `GET /api/tasks/{id}`: Get a specific task
- `POST /api/tasks`: Create a new task
- `PUT /api/tasks/{id}`: Update a task
- `DELETE /api/tasks/{id}`: Delete a task