#!/bin/bash
# Exports the DB env vars and starts the Spring Boot app in one command,
# so you never hit "Access denied" from a forgotten export again.
#
# Usage:
#   chmod +x run.sh    (one-time)
#   ./run.sh

export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=batch_lot_calc
export DB_USER=root
export DB_PASSWORD='Admin123!'

mvn spring-boot:run
