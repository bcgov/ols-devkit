CREATE TABLE gpkg_data_column_constraints (
  constraint_name TEXT NOT NULL,
  constraint_type TEXT NOT NULL, -- 'range' | 'enum' | 'glob'
  value TEXT,
  min NUMERIC,
  min_is_inclusive BOOLEAN, -- 0 = false, 1 = true
  max NUMERIC,
  max_is_inclusive BOOLEAN, -- 0 = false, 1 = true
  description TEXT,
  CONSTRAINT gdcc_ntv UNIQUE (constraint_name, constraint_type, value)
)
