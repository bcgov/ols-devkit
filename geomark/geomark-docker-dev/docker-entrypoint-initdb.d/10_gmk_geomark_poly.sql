
CREATE TABLE "GEOMARK"."GMK_GEOMARK_POLY"
 ("GEOMARK_ID" VARCHAR(35) NOT NULL
 ,"MIN_EXPIRY_DATE" DATE NOT NULL
 ,"EXPIRY_DATE" DATE NOT NULL
 ,"WHEN_CREATED" TIMESTAMP NOT NULL
 ,"GEOMETRY" geometry(GEOMETRY,3005) NOT NULL
 )

;

COMMENT ON TABLE "GEOMARK"."GMK_GEOMARK_POLY" IS 'The oracle spatial layer GEOMARK POLY is non-multipart polygon feature that stores a GeoMark polygon registered with the GeoMark Web Service.'
;

COMMENT ON COLUMN "GEOMARK"."GMK_GEOMARK_POLY"."GEOMARK_ID" IS 'This is the unique key for the GEOMARK POLY. The unique key contains the gm- prefix with a GUID generated id, for example gm-8e4f5974c5b5484ab4e50bc42d4c085b.'
;

COMMENT ON COLUMN "GEOMARK"."GMK_GEOMARK_POLY"."EXPIRY_DATE" IS 'This is the date when the geomark will expire and be deleted if it is not part of a group.'
;

COMMENT ON COLUMN "GEOMARK"."GMK_GEOMARK_POLY"."WHEN_CREATED" IS 'This is the timestamp when the record was created.'
;

COMMENT ON COLUMN "GEOMARK"."GMK_GEOMARK_POLY"."GEOMETRY" IS 'This is the Oracle geometry POLYGON location of the "GEOMARK".'
;


ALTER TABLE "GEOMARK"."GMK_GEOMARK_POLY"
 ADD CONSTRAINT GMK_G_PK PRIMARY KEY
  ("GEOMARK_ID")



;

GRANT DELETE, INSERT, SELECT, UPDATE ON "GEOMARK"."GMK_GEOMARK_POLY" TO GEOMARK_USER
;


CREATE INDEX GMK_GP_SI ON "GEOMARK"."GMK_GEOMARK_POLY" USING GIST ("GEOMETRY");
