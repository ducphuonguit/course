#!/bin/bash

# Quick Start Script for Course Attendance System
# This script helps you get started quickly

echo "=========================================="
echo "Course Attendance System - Quick Start"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if MySQL is running
echo -e "${BLUE}Checking MySQL connection...${NC}"
mysql -u root -p -h localhost -P 3307 -e "SELECT 1" 2>/dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ MySQL is running${NC}"
else
    echo -e "${RED}✗ MySQL is not running or credentials are incorrect${NC}"
    echo "Please start MySQL on localhost:3307 with user 'root'"
    exit 1
fi

# Check if database exists
echo -e "${BLUE}Checking database...${NC}"
mysql -u root -p -h localhost -P 3307 -e "USE course" 2>/dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Database 'course' exists${NC}"
else
    echo -e "${BLUE}Creating database 'course'...${NC}"
    mysql -u root -p -h localhost -P 3307 -e "CREATE DATABASE course"
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Database 'course' created${NC}"
    else
        echo -e "${RED}✗ Failed to create database${NC}"
        exit 1
    fi
fi

# Load sample data
echo ""
read -p "Do you want to load sample data? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${BLUE}Loading sample data...${NC}"
    mysql -u root -p -h localhost -P 3307 course < sample-data.sql
    echo -e "${GREEN}✓ Sample data loaded${NC}"
fi

# Build the application
echo ""
echo -e "${BLUE}Building the application...${NC}"
./mvnw clean package -DskipTests
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Build successful${NC}"
else
    echo -e "${RED}✗ Build failed${NC}"
    exit 1
fi

# Run the application
echo ""
echo -e "${GREEN}=========================================="
echo "Starting the application..."
echo "==========================================${NC}"
echo ""
echo "The application will start on: http://localhost:8080"
echo ""
echo "Next steps:"
echo "1. Create admin user: POST http://localhost:8080/api/auth/signup"
echo "2. Login: POST http://localhost:8080/api/auth/login"
echo "3. See API_DOCUMENTATION.md for all endpoints"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

./mvnw spring-boot:run
