#!/bin/bash
INSTANCE=$1
PASSWORD=$2

sqlplus geomark/$PASSWORD@$INSTANCE @drop.sql

./ddl.sh $INSTANCE $PASSWORD
