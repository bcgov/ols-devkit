CREATE TABLE gpkg_metadata (
  id INTEGER CONSTRAINT m_pk PRIMARY KEY ASC NOT NULL,
  md_scope TEXT NOT NULL DEFAULT 'dataset',
  md_standard_uri TEXT NOT NULL,
  mime_type TEXT NOT NULL DEFAULT 'text/xml',
  metadata TEXT NOT NULL DEFAULT ''
)

-- END --

CREATE TRIGGER 'gpkg_metadata_md_scope_insert'
BEFORE INSERT ON 'gpkg_metadata'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'insert on table gpkg_metadata violates constraint: md_scope must be one of undefined | fieldSession | collectionSession | series | dataset | featureType | feature | attributeType | attribute | tile | model | catalog | schema | taxonomy software | service | collectionHardware | nonGeographicDataset | dimensionGroup')
WHERE NOT(NEW.md_scope IN
('undefined','fieldSession','collectionSession','series','dataset','featureType','feature','attributeType','attribute','tile','model','catalog','schema','taxonomy','software','service','collectionHardware','nonGeographicDataset','dimensionGroup'));
END

-- END --

CREATE TRIGGER 'gpkg_metadata_md_scope_update'
BEFORE UPDATE OF 'md_scope' ON 'gpkg_metadata'
FOR EACH ROW BEGIN
SELECT RAISE(ABORT, 'update on table gpkg_metadata violates constraint: md_scope must be one of undefined | fieldSession | collectionSession | series | dataset | featureType | feature | attributeType | attribute | tile | model | catalog | schema | taxonomy software | service | collectionHardware | nonGeographicDataset | dimensionGroup')
WHERE NOT(NEW.md_scope IN
('undefined','fieldSession','collectionSession','series','dataset','featureType','feature','attributeType','attribute','tile','model','catalog','schema','taxonomy','software','service','collectionHardware','nonGeographicDataset','dimensionGroup'));
END
