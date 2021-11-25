#!/bin/bash
INSTANCE=$1

sqlplus system@$INSTANCE @geomark-dba-all.sql
