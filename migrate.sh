#!/bin/bash
set -e

echo "Starting database migrations..."

# Use wait script to ensure database is ready
/app/wait-for-db.sh $DB_HOST echo "Database is ready. Starting migrations..."

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