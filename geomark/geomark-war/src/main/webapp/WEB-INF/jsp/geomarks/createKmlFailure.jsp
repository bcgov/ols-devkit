<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%><%@
  taglib
  uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><c:set var="binaryFormats" value="false" />
  
<html>
  <head>
<%@ include file="cssJs.jsp" %>
  </head>
  <body>
<h1>Create Geomark</h1>
<div>Construct a new geomark by copying the shape (KML) drawn in Google Earth and
pasting it into this form.
Click <a href="<c:out value="${requestScope.geomarkConfig.getProperty('googleEarthTurorialUrl')}" />">here</a> to access the tutorial.
<ul>
<li>If you haven't defined a placemark that represents your Area of Interest, close this balloon and do so.</li>
<li>In the Google Earth side-bar (aka layer list), right click on the name of the placemark or folder of placemarks you want to turn into a geomark, then select 'Copy'.</li>
<li>Click once in the text box labelled Placemark KML.</li>
<li>Paste the contents of clipboard into the text box by right-clicking and selecting 'Paste' OR holding down the Ctrl key and pressing the 'V' key. </li>
<li>If you have copied a folder of placemarks to the clipboard, check the tick box next to Create geomark from multiple geometries.</li>
<li>Enter a buffer width if required.</li>
<li>Press the 'Create Geomark' button.</li>
<li>Once the geomark is created, a web address will display. Save this address to your personal computer directory as this web address will allow you to access the geomark in the future or pass on to other parties for access to your area of interest. If the web address is lost before it is saved, you will need to Construct a newnother geomark as there is no 'search' capability for retrieval.</li>
</ul>

  <form id="createGeomark" role="form" data-toggle="validator" class="form-horizontal" action="<c:out value="${requestScope.serverUrl}/pub/geomark/api/geomarks/new.kml" />"  method="POST">
    <c:set var="formName" value="kml" />
    <input type="hidden" name="googleEarth" value="true" />
    <input type="hidden" name="formName" value="kml" />
    <input type="hidden" name="format" value="kml" />
    <div class="form-group has-feedback<c:if test="${!empty(param.body_Error)}"> has-error</c:if>">
      <label for="body" class="col-sm-2 control-label"<a href="<c:url value="${requestScope.geomarkConfig.getProperty('glossaryUrl')}#body" />">Placemark KML</a></label>
      <div class="col-sm-10">
        <textarea name="body" rows="5" required class="form-control input-sm" ><c:out value="${param.body}" /></textarea>
        <c:if test="${!empty(param.body_Error)}">
          <p class="help-block with-errors"><c:out value="${param.body_Error}" /></p>
        </c:if>
      </div>
    </div>
<c:set var="imagePrefix" value="${requestScope.serverUrl}/pub/geomark/assets" />
<%@ include file="kmlFields.jsp" %>
    <div class="btn-toolbar" role="toolbar">
      <button class="btn btn-primary btn-sm" type="submit">Create Geomark</button>
    </div>
  </form>
</div>
  </body>
</html>