FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy all project files
COPY . .

# Compile Java files
RUN cd backend && javac *.java

# Expose the port (Railway sets PORT env var)
EXPOSE ${PORT:-8080}

# Run from the backend directory so staticDir resolves to /app (parent of /app/backend)
WORKDIR /app/backend
CMD ["java", "GradeCalculatorServer"]
