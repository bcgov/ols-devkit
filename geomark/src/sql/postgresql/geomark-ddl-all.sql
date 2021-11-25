CREATE SCHEMA GEOMARK;
SET search_path TO GEOMARK,public;

GRANT USAGE ON SCHEMA GEOMARK TO geomark_user;

\i gmk_geomark_poly.sql
\i gmk_geomark_groups.sql
\i gmk_geomark_group_xref.sql
\i gmk_config_properties.sql

\i geomark-ddl-data.sql

\q
