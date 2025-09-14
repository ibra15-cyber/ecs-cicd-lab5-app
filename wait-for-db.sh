#!/bin/bash
set -e

host="$1"
shift
cmd="$@"

until pg_isready -h "$host" -p 5432 -U "$DB_USER" -d "$DB_NAME"; do
  >&2 echo "PostgreSQL is unavailable - sleeping"
  sleep 2
done

>&2 echo "PostgreSQL is up - executing command"
exec $cmd