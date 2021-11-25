#!/bin/bash
INSTANCE=$1
PASSWORD=$2
if [ ! -z $PASSWORD ]; then
  PASSWORD=/$PASSWORD
fi
sqlplus geomark$PASSWORD@$INSTANCE @geomark-ddl-all.sql