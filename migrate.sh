#!/bin/bash
set -e

DB_HOST="$1"

echo "Waiting for PostgreSQL..."
until pg_isready -h "$DB_HOST" -p 5432 -U "$DB_USER" -d "$DB_NAME"; do
  >&2 echo "PostgreSQL is unavailable - sleeping"
  sleep 2
done
>&2 echo "PostgreSQL is up - starting migrations..."

# Run Flyway migrations
echo "Running database migrations..."
/app/flyway-9.22.0/flyway \
    -url="jdbc:postgresql://$DB_HOST:5432/$DB_NAME" \
    -user="$DB_USER" \
    -password="$DB_PASSWORD" \
    -locations="filesystem:/app/db/migration" \
    -baselineOnMigrate=true \
    -outOfOrder=true \
    migrate

echo "Database migrations completed successfully!"