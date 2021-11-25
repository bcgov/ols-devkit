CREATE TABLE gpkg_tile_matrix (
  table_name TEXT NOT NULL,
  zoom_level INTEGER NOT NULL,
  matrix_width INTEGER NOT NULL,
  matrix_height INTEGER NOT NULL,
  tile_width INTEGER NOT NULL,
  tile_height INTEGER NOT NULL,
  pixel_x_size DOUBLE NOT NULL,
  pixel_y_size DOUBLE NOT NULL,
  CONSTRAINT pk_ttm PRIMARY KEY (table_name, zoom_level),
  CONSTRAINT fk_tmm_table_name FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name)
)

-- END --

CREATE TRIGGER 'gpkg_tile_matrix_zoom_level_insert'
BEFORE INSERT ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'insert on table ''gpkg_tile_matrix'' violates constraint: zoom_level cannot be less than 0')
WHERE (NEW.zoom_level < 0);
END

-- END --

CREATE TRIGGER 'gpkg_tile_matrix_zoom_level_update'
BEFORE UPDATE of zoom_level ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'update on table ''gpkg_tile_matrix'' violates constraint: zoom_level cannot be less than 0')
WHERE (NEW.zoom_level < 0);
END

-- END --

CREATE TRIGGER 'gpkg_tile_matrix_matrix_width_insert'
BEFORE INSERT ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'insert on table ''gpkg_tile_matrix'' violates constraint: matrix_width cannot be less than 1')
WHERE (NEW.matrix_width < 1);
END

-- END --

CREATE TRIGGER 'gpkg_tile_matrix_matrix_width_update'
BEFORE UPDATE OF matrix_width ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'update on table ''gpkg_tile_matrix'' violates constraint: matrix_width cannot be less than 1')
WHERE (NEW.matrix_width < 1);
END

-- END --

CREATE TRIGGER 'gpkg_tile_matrix_matrix_height_insert'
BEFORE INSERT ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'insert on table ''gpkg_tile_matrix'' violates constraint: matrix_height cannot be less than 1')
WHERE (NEW.matrix_height < 1);
END

-- END --

CREATE TRIGGER 'gpkg_tile_matrix_matrix_height_update'
BEFORE UPDATE OF matrix_height ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'update on table ''gpkg_tile_matrix'' violates constraint: matrix_height cannot be less than 1')
WHERE (NEW.matrix_height < 1);
END

-- END --

CREATE TRIGGER 'gpkg_tile_matrix_pixel_x_size_insert'
BEFORE INSERT ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'insert on table ''gpkg_tile_matrix'' violates constraint: pixel_x_size must be greater than 0')
WHERE NOT (NEW.pixel_x_size > 0);
END

-- END --

CREATE TRIGGER 'gpkg_tile_matrix_pixel_x_size_update'
BEFORE UPDATE OF pixel_x_size ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'update on table ''gpkg_tile_matrix'' violates constraint: pixel_x_size must be greater than 0')
WHERE NOT (NEW.pixel_x_size > 0);
END

-- END --

CREATE TRIGGER 'gpkg_tile_matrix_pixel_y_size_insert'
BEFORE INSERT ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'insert on table ''gpkg_tile_matrix'' violates constraint: pixel_y_size must be greater than 0')
WHERE NOT (NEW.pixel_y_size > 0);
END

-- END --

CREATE TRIGGER 'gpkg_tile_matrix_pixel_y_size_update'
BEFORE UPDATE OF pixel_y_size ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'update on table ''gpkg_tile_matrix'' violates constraint: pixel_y_size must be greater than 0')
WHERE NOT (NEW.pixel_y_size > 0);
END
