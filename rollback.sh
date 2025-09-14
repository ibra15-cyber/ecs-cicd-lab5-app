#!/bin/bash
# Emergency rollback script
flyway -url="jdbc:postgresql://$DB_HOST:5432/$DB_NAME" \
       -user="$DB_USER" \
       -password="$DB_PASSWORD" \
       -locations="filesystem:/app/db/migration" \
       repair