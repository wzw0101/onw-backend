#!/usr/bin/env bash
mvn clean package -DskipTests
mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)
docker build --platform linux/amd64,linux/arm64 -t wzw9807/onw .