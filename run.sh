#!/bin/bash
# Starts the Spring Boot app (http://localhost:8081) against your local MySQL.
#
# Credentials come from your environment, never from this file:
#   export DB_USER=root
#   export DB_PASSWORD='your-mysql-password'
#   ./run.sh
#
# If DB_PASSWORD is not set, the script asks for it (input hidden).

export DB_HOST="${DB_HOST:-localhost}"
export DB_PORT="${DB_PORT:-3306}"
export DB_NAME="${DB_NAME:-batch_lot_calc}"
export DB_USER="${DB_USER:-root}"

if [ -z "${DB_PASSWORD+x}" ]; then
  read -rsp "MySQL password for ${DB_USER}: " DB_PASSWORD; echo
  export DB_PASSWORD
fi

mvn spring-boot:run
