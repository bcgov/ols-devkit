CREATE TABLE gpkg_metadata_reference (
  reference_scope TEXT NOT NULL,
  table_name TEXT,
  column_name TEXT,
  row_id_value INTEGER,
  timestamp DATETIME NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')),
  md_file_id INTEGER NOT NULL,
  md_parent_id INTEGER,
  CONSTRAINT crmr_mfi_fk FOREIGN KEY (md_file_id) REFERENCES gpkg_metadata(id),
  CONSTRAINT crmr_mpi_fk FOREIGN KEY (md_parent_id) REFERENCES gpkg_metadata(id)
)

-- END --

CREATE TRIGGER 'gpkg_metadata_reference_reference_scope_insert'
BEFORE INSERT ON 'gpkg_metadata_reference'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'insert on table gpkg_metadata_reference violates constraint: reference_scope must be one of "geopackage",
table", "column", "row", "row/col"')
WHERE NOT NEW.reference_scope IN
('geopackage','table','column','row','row/col');
END

-- END --

CREATE TRIGGER 'gpkg_metadata_reference_reference_scope_update'
BEFORE UPDATE OF 'reference_scope' ON 'gpkg_metadata_reference'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'update on table gpkg_metadata_reference violates constraint: referrence_scope must be one of "geopackage",
"table", "column", "row", "row/col"')
WHERE NOT NEW.reference_scope IN
('geopackage','table','column','row','row/col');
END

-- END --

CREATE TRIGGER 'gpkg_metadata_reference_column_name_insert'
BEFORE INSERT ON 'gpkg_metadata_reference'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'insert on table gpkg_metadata_reference violates constraint: column name must be NULL when reference_scope is "geopackage", "table" or "row"')
WHERE (NEW.reference_scope IN ('geopackage','table','row')
AND NEW.column_name IS NOT NULL);
SELECT RAISE(ABORT, 'insert on table gpkg_metadata_reference violates constraint: column name must be defined for the specified table when reference_scope is "column" or "row/col"')
WHERE (NEW.reference_scope IN ('column','row/col')
AND NOT NEW.table_name IN (
SELECT name FROM SQLITE_MASTER WHERE type = 'table'
AND name = NEW.table_name
AND sql LIKE ('%' || NEW.column_name || '%')));
END

-- END --

CREATE TRIGGER 'gpkg_metadata_reference_column_name_update'
BEFORE UPDATE OF column_name ON 'gpkg_metadata_reference'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'update on table gpkg_metadata_reference violates constraint: column name must be NULL when reference_scope is "geopackage", "table" or "row"')
WHERE (NEW.reference_scope IN ('geopackage','table','row')
AND NEW.column_nameIS NOT NULL);
SELECT RAISE(ABORT, 'update on table gpkg_metadata_reference violates constraint: column name must be defined for the specified table when reference_scope is "column" or "row/col"')
WHERE (NEW.reference_scope IN ('column','row/col')
AND NOT NEW.table_name IN (
SELECT name FROM SQLITE_MASTER WHERE type = 'table'
AND name = NEW.table_name
AND sql LIKE ('%' || NEW.column_name || '%')));
END

-- END --

CREATE TRIGGER 'gpkg_metadata_reference_row_id_value_insert'
BEFORE INSERT ON 'gpkg_metadata_reference'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'insert on table gpkg_metadata_reference violates constraint: row_id_value must be NULL when reference_scope is "geopackage", "table" or "column"')
WHERE NEW.reference_scope IN ('geopackage','table','column')
AND NEW.row_id_value IS NOT NULL;
END

-- END --

CREATE TRIGGER 'gpkg_metadata_reference_row_id_value_update'
BEFORE UPDATE OF 'row_id_value' ON 'gpkg_metadata_reference'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'update on table gpkg_metadata_reference violates constraint: row_id_value must be NULL when reference_scope is "geopackage", "table" or "column"')
WHERE NEW.reference_scope IN ('geopackage','table','column')
AND NEW.row_id_value IS NOT NULL;
END

-- END --

CREATE TRIGGER 'gpkg_metadata_reference_timestamp_insert'
BEFORE INSERT ON 'gpkg_metadata_reference'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'insert on table gpkg_metadata_reference violates constraint: timestamp must be a valid time in ISO 8601 "yyyy-mm-ddThh:mm:ss.cccZ" form')
WHERE NOT (NEW.timestamp GLOB
'[1-2][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9][0-9][0-9]Z'
AND strftime('%s',NEW.timestamp) NOT NULL);
END

-- END --

CREATE TRIGGER 'gpkg_metadata_reference_timestamp_update'
BEFORE UPDATE OF 'timestamp' ON 'gpkg_metadata_reference'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'update on table gpkg_metadata_reference violates constraint: timestamp must be a valid time in ISO 8601 "yyyy-mm-ddThh:mm:ss.cccZ" form')
WHERE NOT (NEW.timestamp GLOB
'[1-2][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9][0-9][0-9]Z'
AND strftime('%s',NEW.timestamp) NOT NULL);
END
