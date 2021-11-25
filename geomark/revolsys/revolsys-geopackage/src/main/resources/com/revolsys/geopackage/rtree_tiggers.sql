CREATE VIRTUAL TABLE rtree_<t>_<c> USING rtree(id, minx, maxx, miny, maxy)

-- END --

CREATE TRIGGER rtree_<t>_<c>_insert AFTER INSERT ON <t>
  WHEN (new.<c> NOT NULL AND NOT ST_IsEmpty(NEW.<c>))
BEGIN
  INSERT OR REPLACE INTO rtree_<t>_<c> VALUES (
    NEW.<i>,
    ST_MinX(NEW.<c>), ST_MaxX(NEW.<c>),
    ST_MinY(NEW.<c>), ST_MaxY(NEW.<c>)
  );
END;

-- END --

CREATE TRIGGER rtree_<t>_<c>_update1 AFTER UPDATE OF <c> ON <t>
  WHEN OLD.<i> = NEW.<i> AND
       (NEW.<c> NOTNULL AND NOT ST_IsEmpty(NEW.<c>))
BEGIN
  INSERT OR REPLACE INTO rtree_<t>_<c> VALUES (
    NEW.<i>,
    ST_MinX(NEW.<c>), ST_MaxX(NEW.<c>),
    ST_MinY(NEW.<c>), ST_MaxY(NEW.<c>)
  );
END;

-- END --

CREATE TRIGGER rtree_<t>_<c>_update2 AFTER UPDATE OF <c> ON <t>
  WHEN OLD.<i> = NEW.<i> AND
       (NEW.<c> ISNULL OR ST_IsEmpty(NEW.<c>))
BEGIN
  DELETE FROM rtree_<t>_<c> WHERE id = OLD.<i>;
END;

-- END --

CREATE TRIGGER rtree_<t>_<c>_update3 AFTER UPDATE OF <c> ON <t>
  WHEN OLD.<i> != NEW.<i> AND
       (NEW.<c> NOTNULL AND NOT ST_IsEmpty(NEW.<c>))
BEGIN
  DELETE FROM rtree_<t>_<c> WHERE id = OLD.<i>;
  INSERT OR REPLACE INTO rtree_<t>_<c> VALUES (
    NEW.<i>,
    ST_MinX(NEW.<c>), ST_MaxX(NEW.<c>),
    ST_MinY(NEW.<c>), ST_MaxY(NEW.<c>)
  );
END;

-- END --

CREATE TRIGGER rtree_<t>_<c>_update4 AFTER UPDATE ON <t>
  WHEN OLD.<i> != NEW.<i> AND
       (NEW.<c> ISNULL OR ST_IsEmpty(NEW.<c>))
BEGIN
  DELETE FROM rtree_<t>_<c> WHERE id IN (OLD.<i>, NEW.<i>);
END;

-- END --

CREATE TRIGGER rtree_<t>_<c>_delete AFTER DELETE ON <t>
  WHEN old.<c> NOT NULL
BEGIN
  DELETE FROM rtree_<t>_<c> WHERE id = OLD.<i>;
END;