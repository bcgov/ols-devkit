CREATE ROLE geomark_user;
CREATE USER geomark PASSWORD 'g30m4rk' LOGIN NOSUPERUSER inherit CREATEDB NOCREATEROLE NOREPLICATION  ;
CREATE USER proxy_geomark_web PASSWORD 'g30m4rk' LOGIN NOSUPERUSER inherit CREATEDB NOCREATEROLE NOREPLICATION ;

grant geomark_user to geomark;
grant geomark_user to proxy_geomark_web;

