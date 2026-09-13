#!/bin/bash
# Convenience build script for HealthFirst PIMS.
# Usage: ./build.sh   (compiles everything into ./bin)
set -e
mkdir -p bin
javac -d bin $(find src -name "*.java")
echo "Build complete -> bin/"
echo "Run with:"
echo "  java -cp bin:lib/mysql-connector-j-9.1.0.jar com.healthfirst.pims.Main"
