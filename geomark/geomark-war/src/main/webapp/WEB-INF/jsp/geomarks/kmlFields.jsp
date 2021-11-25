<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<div class="form-group">
  <label class="col-sm-2 control-label"><a href="<c:url value="${requestScope.geomarkConfig.getProperty('glossaryUrl')}#geometryType" />">Geometry Type</a></label>
  <div class="col-sm-10">
    <div>
      <label class="radio-inline">
        <input type="radio" name="geometryType" value="Polygon" required<c:if
          test="${param.formName != formName || param.geometryType != 'LineString' && param.geometryType != 'Point' && param.geometryType != 'Any'}"> checked</c:if> /> 
        Polygon
      </label>
        
      <label class="radio-inline">
        <input type="radio" name="geometryType" value="LineString" required<c:if
          test="${param.formName == formName && param.geometryType == 'LineString'}"> checked</c:if> />
        Line
      </label>
          
      <label class="radio-inline">
        <input type="radio" name="geometryType" value="Point" required<c:if
          test="${param.formName == formName && param.geometryType == 'Point'}"> checked</c:if> />
        Point
      </label>
        
      <label class="radio-inline">
        <input type="radio" name="geometryType" value="Any" required<c:if
        test="${param.formName == formName && param.geometryType == 'Any'}"> checked</c:if> />
        Any (Polygon, Line and Point)
      </label>
    </div>
    <p class="help-block">Select the types of geometry to read from
      the input data. This is will be the type of geometry created for the
      geomark unless a buffer is specified, in which case the geomark will
      have a Polygon geometry. The Any type will read all Polygon, Line and
      Point geometries and Construct a new buffered polygon from them, a buffer must
      be specified for this option. The input geometries or buffered polygon
      must not contain any overlapping geometries.</p>
  </div>
</div>

<div class="form-group">
  <label for="multiple" class="col-sm-2 control-label"><a href="<c:url value="${requestScope.geomarkConfig.getProperty('glossaryUrl')}#geometryCount" />">Geometry Count</a></label>
  <div class="col-sm-10">
    <div>
      <label class="radio-inline">
        <input type="radio" name="multiple" value="false" required<c:if
          test="${param.formName != formName || param.multiple != 'true'}"> checked</c:if> />
        One
      </label>
      <label class="radio-inline">
        <input type="radio" name="multiple" value="true" required<c:if
          test="${param.formName == formName && param.multiple == 'true'}"> checked</c:if> />
        Many</label>
    </div>
    <p class="help-block">Select the number of geometries to read
      from the input data. One will read only the first matching geometry from
      a feature, feature collection, or geometry collection. Many will read
      all geometries of the selected geometry type from the input data.</p>
  </div>
</div>

<div class="form-group">
  <label for="allowOverlap" class="col-sm-2 control-label"><a href="<c:url value="${requestScope.geomarkConfig.getProperty('glossaryUrl')}#allowOverlap" />">Allow Overlap</a></label>
  <div class="col-sm-10">
    <div>
      <input name="allowOverlap" type="checkbox" class="input-sm" value="true"<c:if
        test="${param.formName != formName && param.allowOverlap == 'true'}"> checked</c:if> />
    </div>
    <p class="help-block with-errors"></p>
    <p class="help-block">For Geometry Count=Many select this option to allow overlapping geometries.</p>
  </div>
</div>

<div class="form-group has-feedback">
  <label for="bufferMetres" class="col-sm-2 control-label"><a href="<c:url value="${requestScope.geomarkConfig.getProperty('glossaryUrl')}#bufferMetres" />">Buffer Width (Metres)</a></label>
  <div class="col-sm-10">
    <input type="number" class="form-control input-sm" min="1" name="bufferMetres"<c:if test="${param.formName == formName && !empty(param.bufferMetres)}">value="<c:out value="${param.bufferMetres}" />"</c:if> />
    <div class="help-block with-errors"><c:if test="${param.formName == formName && !empty(param.bufferMetres_Error)}"><c:out value="${param.bufferMetres_Error}" /></c:if></div>
    <p class="help-block">The amount to buffer the geometry in metres, must only contain
      numerical digits (e.g 10). Leave blank and no buffer will be added to input geometries.</p>
    <button type="button" class="btn btn-info" data-toggle="collapse" data-target="#<c:out value="${formName}" />_advancedBuffer">Advanced buffer settings</button>
  </div>
</div>
<div id="<c:out value="${formName}" />_advancedBuffer" class="collapse">
  <div class="form-group has-feedback">
    <label for="bufferSegments" class="col-sm-2 control-label"><a href="<c:url value="${requestScope.geomarkConfig.getProperty('glossaryUrl')}#bufferSegments" />">Buffer segments</a></label>
    <div class="col-sm-10">
      <input type="number" name="bufferSegments" min="1" max="20" required value="<c:choose
        ><c:when test="${param.formName == formName && !empty(param.bufferSegments)}"><c:out value="${param.bufferSegments}" /></c:when
        ><c:otherwise>8</c:otherwise></c:choose>" class="form-control input-sm" />
      <div class="help-block with-errors"><c:if test="${param.formName == formName && !empty(param.bufferSegments_Error)}"><c:out value="${param.bufferSegments_Error}" /></c:if></div>
      <p class="help-block">Number of line segments used in each quadrant to
        approximate the curve for round end-cap and join styles. Must be &gt; 0.</p>
    </div>
  </div>
  <div class="form-group has-feedback">
    <label for="bufferMitreLimit" class="col-sm-2 control-label"><a href="<c:url value="${requestScope.geomarkConfig.getProperty('glossaryUrl')}#bufferMitreLimit" />">Mitre Limit (Ratio)</a></label>
    <div class="col-sm-10">
      <input type="number" name="bufferMitreLimit" min="1" max="20" required value="<c:choose
        ><c:when test="${param.formName == formName && !empty(param.bufferMitreLimit)}"><c:out value="${param.bufferMitreLimit}" /></c:when
        ><c:otherwise>5</c:otherwise></c:choose>" class="form-control input-sm" />
      <div class="help-block with-errors"><c:if test="${param.formName == formName && !empty(param.bufferMitreLimit_Error)}"><c:out value="${param.bufferMitreLimit_Error}" /></c:if></div>
      <p class="help-block">The maximum ratio of distance from the original
        geometry to a mitre buffer point and the buffer amount. If the ratio is
        greater than this a bevel will be used instead. This prevents infinite
        distances when the angle between the two lines at a join is small. Must be
        &gt; 0.</p>
    </div>
  </div>
  <div class="form-group has-feedback">
    <label for="bufferCap" class="col-sm-2 control-label"><a href="<c:url value="${requestScope.geomarkConfig.getProperty('glossaryUrl')}#bufferCap" />">End-cap Style</a></label>
    <div class="col-sm-10">
      <div>
        <label class="radio-inline">
          <input type="radio" name="bufferCap" value="ROUND" <c:if test="${param.formName != formName || param.bufferCap != 'SQUARE' && param.bufferCap != 'FLAT'}"> checked</c:if> />
            <img src="<c:url value="${imagePrefix}/images/end-cap-round.png" />" /> Round
        </label>
        <label class="radio-inline">
          <input type="radio" name="bufferCap" value="SQUARE" <c:if test="${param.formName == formName && param.bufferCap == 'SQUARE'}"> checked</c:if> />
           <img src="<c:url value="${imagePrefix}/images/end-cap-square.png" />" /> Square
        </label>
        <label class="radio-inline">
          <input type="radio" name="bufferCap" value="FLAT" <c:if test="${param.formName == formName && param.bufferCap == 'FLAT'}"> checked</c:if> />
          <img src="<c:url value="${imagePrefix}/images/end-cap-flat.png" />" /> Flat
        </label>
      </div>
      <p class="help-block">The style of buffer to use at the ends of a buffered line.</p>
    </div>
  </div>
  <div class="form-group has-feedback">
    <label for="bufferJoin" class="col-sm-2 control-label"><a href="<c:url value="${requestScope.geomarkConfig.getProperty('glossaryUrl')}#bufferJoin" />">Join Style</a></label>
    <div class="col-sm-10">
      <div>
        <label class="radio-inline">
          <input type="radio" name="bufferJoin" value="ROUND" <c:if test="${param.formName != formName || param.bufferJoin != 'MITRE' && param.bufferJoin != 'BEVEL'}"> checked</c:if> />
          <img src="<c:url value="${imagePrefix}/images/join-round.png" />" /> Round
        </label>
        <label class="radio-inline">
          <input type="radio" name="bufferJoin" value="MITRE" <c:if test="${param.formName == formName && param.bufferJoin == 'MITRE'}"> checked</c:if> />
          <img src="<c:url value="${imagePrefix}/images/join-mitre.png" />" /> Mitre
        </label>
        <label class="radio-inline">
          <input type="radio" name="bufferJoin" value="BEVEL" <c:if test="${param.formName == formName && param.bufferJoin == 'BEVEL'}"> checked</c:if> />
            <img src="<c:url value="${imagePrefix}/images/join-bevel.png" />" /> Bevel
        </label>
        <p class="help-block">The style of buffer to use for joins between the line segments for lines and polygons.</p>
      </div>
    </div>
  </div>
</div>