<%@ page
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  session="false"
%><%@
  taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
  <head>
    <title><c:out value="${param['geomarkId']}" /></title>
    <style>
body{ padding-top:0px; padding-bottom:0px;width: 700px}
    </style>
  </head>
  <body>
    <h1>Geomark <c:out value="${param['geomarkId']}" /> created</h1>
    
    <p>Save this URL to access this geomark in the future:</p>
    
    <p><a href="<c:out value="${param['geomarkUrl']}" />" target="_blank"><c:out value="${param['geomarkUrl']}" /></a></p>

    <ul>
      <li><a href="<c:out value="${param['geomarkUrl']}" />" target="_blank">View geomark in your web browser</a></li>
      <li><a href="<c:out value="${param['geomarkUrl']}/parts.kml" />" target="_blank">Open geomark in Google Earth</a></li>
    </ul>
  </body>
</html>
