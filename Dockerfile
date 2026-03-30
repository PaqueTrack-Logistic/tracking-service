# Java 22 + Maven ready to use
FROM maven:3.9-eclipse-temurin-22

# Optional utilities for development
RUN apt-get update && apt-get install -y curl git unzip \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace
COPY . /workspace

CMD ["sleep", "infinity"]
