#!/bin/bash
set -e

DB_HOST="$1"

echo "Waiting for PostgreSQL..."
until pg_isready -h "$DB_HOST" -p 5432 -U "$DB_USER" -d "$DB_NAME"; do
  >&2 echo "PostgreSQL is unavailable - sleeping"
  sleep 2
done
>&2 echo "PostgreSQL is up - starting migrations..."

# Download Flyway command-line tool (since it's not in the JAR)
echo "Downloading Flyway..."
curl -L https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/9.22.0/flyway-commandline-9.22.0-linux-x64.tar.gz -o flyway.tar.gz
tar -xzf flyway.tar.gz
rm flyway.tar.gz

# Run Flyway migrations
echo "Running database migrations..."
./flyway-9.22.0/flyway \
    -url="jdbc:postgresql://$DB_HOST:5432/$DB_NAME" \
    -user="$DB_USER" \
    -password="$DB_PASSWORD" \
    -locations="filesystem:/app/db/migration" \
    -baselineOnMigrate=true \
    -outOfOrder=true \
    migrate

echo "Database migrations completed successfully!"

# Clean up Flyway download
rm -rf flyway-9.22.0