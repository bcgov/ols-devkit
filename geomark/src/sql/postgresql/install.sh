#!/bin/bash

DB=geomark
host=localhost
port=5432
adminuser=postgres
admindb=postgres
geomark_pw=g30m4rk
proxy_geomark_web=g30m4rk

PSQL_ADMIN_CONNECT="-h $host -p $port -U $adminuser"

################ ROLE geomark_user
ERR_GEOMARK_USER=`createuser $PSQL_ADMIN_CONNECT --no-login geomark_user 2>&1`
if [[ "$?" != "0" && ! "$ERR_GEOMARK_USER" =~ "already exists" ]]; then
  echo $ERR_GEOMARK_USER
  exit 1
fi

################ USER geomark
ERR_GEOMARK=`echo "CREATE USER geomark PASSWORD '$geomark_pw' CREATEDB IN ROLE geomark_user" | psql -q $PSQL_ADMIN_CONNECT 2>&1`
if [[ "$?" != "0" && ! "$ERR_GEOMARK" =~ "already exists" ]]; then
  echo $ERR_GEOMARK
  exit 1
fi

################ USER proxy_geomark_web
ERR_PROXY_GEOMARK_WEB=`echo "CREATE USER proxy_geomark_web PASSWORD '$proxy_geomark_web_pw' IN ROLE geomark_user" | psql -q $PSQL_ADMIN_CONNECT 2>&1`
if [[ "$?" != "0" && ! "$ERR_PROXY_GEOMARK_WEB" =~ "already exists" ]]; then
  echo $ERR_PROXY_GEOMARK_WEB
  exit 1
fi

################ Create Database
RESULT=`psql $PSQL_ADMIN_CONNECT -d $admindb --tuples-only --command "SELECT datname FROM pg_database WHERE datname = '$DB';"`
if [ " $DB" == "$RESULT" ]; then
  echo "Database exists, are you sure you want to erase all the data (YES/NO)?"
  read DROP_DB
  if [ "$DROP_DB" == "YES" ]; then
    dropdb $PSQL_ADMIN_CONNECT $DB
    if [ "$?" != "0" ]; then
      echo ERROR: Cannot delete database
      exit
    fi
  else
    echo ERROR: Database deletion cancelled by user input
    exit
  fi
fi
createdb $PSQL_ADMIN_CONNECT --owner=geomark $DB
if [ "$?" != "0" ]; then
  echo ERROR: CANNOT CREATE THE DATABASE
  exit 1
fi
for extension in postgis uuid-ossp; do
  echo "CREATE EXTENSION \"$extension\"" | psql $PSQL_ADMIN_CONNECT -d geomark 2>&1 > /dev/null
  if [ "$?" != "0" ]; then
    echo ERROR: CANNOT CREATE THE $extension EXTENSION
    exit 1
  fi
done

################ Create tables
psql -h $host -p $port -U geomark geomark -f geomark-ddl-all.sql > ddl.log 2>&1;
DDL_ERR=`grep -e ERROR -e FATAL ddl.log`
if [ "$DDL_ERR" != "" ]; then
  cat ddl.log
  echo ERROR: CANNOT CREATE THE DATABASE
  exit 1
fi
