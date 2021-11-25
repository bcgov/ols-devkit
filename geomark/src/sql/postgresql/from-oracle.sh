#!/bin/bash
cd ../oracle
for file in gmk_*.sql; do 
  echo $file
  oracleToPostgresql.sh $file ../postgresql GEOMARK
done

cd ../postgresql
sed -i "" "s/MDSYS.SDO_GEOMETRY/geometry(GEOMETRY,3005)/g" gmk_geomark_poly.sql

