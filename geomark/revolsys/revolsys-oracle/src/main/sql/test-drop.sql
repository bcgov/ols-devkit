DEFINE SCHEMA=ORACLE_TEST
BEGIN
  FOR tab IN (SELECT table_name FROM all_tables where owner = '&SCHEMA' and not table_name like 'MDRT_%') LOOP
    EXECUTE IMMEDIATE 'DROP TABLE &SCHEMA..' || tab.table_name || ' CASCADE CONSTRAINTS PURGE';
  END LOOP;

  FOR seq IN (SELECT sequence_name FROM all_sequences where sequence_owner = '&SCHEMA') LOOP
    EXECUTE IMMEDIATE 'DROP SEQUENCE &SCHEMA..' || seq.sequence_name;
  END LOOP;
END;
/

DELETE FROM USER_SDO_GEOM_METADATA;
DELETE FROM OGIS_GEOMETRY_COLUMNS WHERE F_TABLE_SCHEMA = '&SCHEMA';

purge recyclebin;
exit
