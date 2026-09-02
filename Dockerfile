FROM node:24-alpine AS frontend-build
WORKDIR /workspace/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-21-alpine AS backend-build
WORKDIR /workspace/backend
COPY backend/pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline
COPY backend/src ./src
COPY --from=frontend-build /workspace/frontend/dist ./src/main/resources/static
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S catmate && adduser -S catmate -G catmate
COPY --from=backend-build /workspace/backend/target/*.jar /app/app.jar
USER catmate
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
