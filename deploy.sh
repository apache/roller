#!/bin/bash

# Ensure Docker Compose is installed
if ! command -v docker-compose &> /dev/null
then
    echo "docker-compose could not be found. Installing..."
    sudo curl -L "https://github.com/docker/compose/releases/download/v2.17.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
fi

# Navigate to project directory
cd /vagrant

# Build and start the Docker containers
docker-compose up --build -d

# Check container status
docker-compose ps
