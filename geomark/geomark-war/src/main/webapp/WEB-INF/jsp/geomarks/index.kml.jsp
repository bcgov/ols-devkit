<%@
  page contentType="application/vnd.google-earth.kml+xml; charset=UTF-8"
  pageEncoding="UTF-8"
  session="false"
%><%@
  taglib
  uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><?xml version="1.0"?>
<kml xmlns="http://www.opengis.net/kml/2.2"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="
    http://www.opengis.net/kml/2.2
    http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd
  "
>
<Document>
  <name><c:out
  value="<![CDATA[" escapeXml="false" /><a href="<c:out value="${requestScope.serverUrl}" />/pub/geomark">Geomark</a><c:out value="]]>"
  escapeXml="false"
/></name>
  <open>1</open>
  <LookAt>
    <longitude>-125.790485103603</longitude>
    <latitude>54.53529082074053</latitude>
    <altitude>0</altitude>
    <heading>0</heading>
    <tilt>0</tilt>
    <range>1826047.023906531</range>
    <altitudeMode>relativeToGround</altitudeMode>
  </LookAt>
<Folder>
<name>Create geomark</name>
<Snippet maxLines="3">Use this form to upload the KML of a placemark
point, line or polygon to the Geomark Web Service and Construct a new geomark.</Snippet>
<description>
<c:out
  value="<![CDATA[" escapeXml="false" />
<html>
  <head>
<%@ include file="cssJs.jsp" %>
  </head>
  <body>
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
  <input type="hidden" name="googleEarth" value="true" />
  <input type="hidden" name="formName" value="kml" />
  <input type="hidden" name="format" value="kml" />
  <div class="form-group">
    <label for="body" class="col-sm-2 control-label"><a href="<c:url value="${requestScope.geomarkConfig.getProperty('glossaryUrl')}#body" />">Placemark KML</a></label>
    <div class="col-sm-10">
      <textarea name="body" rows="5" required class="form-control input-sm" ></textarea>
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
      <c:out value="]]>"
  escapeXml="false"
/>
</description>
<Style>
        <BalloonStyle>
          <text><c:out value="<![CDATA[" escapeXml="false" /><h1>$[name]</h1>$[description]<c:out value="]]>" escapeXml="false" /></text>
        </BalloonStyle>
      </Style>
<LookAt>
<longitude>-125.508977</longitude>
<latitude>54.389505</latitude>
<altitude>2100000</altitude>
<tilt>0</tilt>
<heading>0</heading>
<altitudeMode>relativeToGround</altitudeMode>
</LookAt>
</Folder>
<Folder>
<name>
<c:out
  value="<![CDATA[" escapeXml="false" /><a href="<c:out value="${requestScope.serverUrl}/pub/geomark/create/file" />">Create geomark from file</a><c:out value="]]>"
  escapeXml="false"
/>
</name>
<description>Construct a new geomark by uploading a file in your web
browser.</description>
</Folder>
<Folder>
<name>
<c:out
  value="<![CDATA[" escapeXml="false" /><a href="<c:out value="${requestScope.geomarkConfig.getProperty('businessHomeUrl')}" />">Documentation</a><c:out value="]]>"
  escapeXml="false"
/>
</name>
<description>View move information about the geomark service
in your web browser.</description>
</Folder>
</Document>
</kml>

