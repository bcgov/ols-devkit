/**
 * @fileOverview <p>The Geomark Javascript client allows applications to use the
 * <a href="../rest-api/">Geomark Web Service REST API</a> to
 * create geomarks, get geomark info, and download geomarks.</p>
 * 
 * @name GeomarkClient
 */

/**
 * <p>Construct a new Geomark client that is connected to a specific Geomark
 * web service (e.g. https://apps.gov.bc.ca/pub/geomark).</p>
 * 
 * @param {string} url The url to the Geomark web service.
 * @constructor
 */
function GeomarkClient(url) {
  this.url = url;
};

/**
<p>Construct a new new geomark by copying the geometries from one or more existing
geomarks using the
<a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.createGeomarkCopy">Create Geomark Copy</a>
REST API.</p>

<p>The parameters to the method are passed in using a Javascript object (map). See the
<a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.createGeomarkCopy">Create Geomark Copy</a>
REST API for the list of supported parameters. </p>

<p>In addition the request must include the 'callback' parameter which must be
a Javascript function. This function will be called with a single argument
containing the <a href="../infoAttributes.html">geomark info</a> Javascript object if the geomark was created
successfully. If there was a data error a JavaScript object
with the error details will be returned. Applications may receive other
undocumented results if the wrong URLs are used or the server is not available.</p>

<p>The following code fragment shows an example of using the API.</p>

<pre class="prettyprint"><code class="language-html">
&lt;script type=&quot;text/javascript&quot; src=&quot;https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js&quot;&gt;&lt;/script&gt;
&lt;script type=&quot;text/javascript&quot; src=&quot;https://apps.gov.bc.ca/pub/geomark/js/geomark.js&quot; &gt;&lt;/script&gt;&gt;
&lt;script type=&quot;text/javascript&quot;&gt;
  function copyGeomarkExample() {
    var baseUrl = 'https://apps.gov.bc.ca/pub/geomark';
    var client = new GeomarkClient(baseUrl);
    var geomarks = [
      baseUrl + '/geomarks/gm-abcdefghijklmnopqrstuv0bcislands'
      // or 'gm-abcdefghijklmnopqrstuv0bcislands'
    ];
    client.copyGeomark({
      'geomarkUrl': geomarks,
      'callback': function(geomarkInfo) {
        var geomarkId = geomarkInfo.id;
        if (geomarkId) { 
          alert('Created geomark: ' + geomarkInfo.id);
        } else {
          alert('Error creating geomark:' + geomarkInfo.geomarkUrl_Error);
        }
      }
    });
  }
&lt;/script&gt;
</code></pre>

<script type="text/javascript">
  function copyGeomarkExample() {
    var baseUrl = 'https://' + location.hostname + '/pub/geomark';
    var client = new GeomarkClient(baseUrl);
    var geomarks = [
      baseUrl + '/geomarks/gm-abcdefghijklmnopqrstuv0bcislands'
    ];
    client.copyGeomark({
      'geomarkUrl': geomarks,
      'callback': function(geomarkInfo) {
        var geomarkId = geomarkInfo.id;
        if (geomarkId) { 
          alert('Created geomark: ' + geomarkInfo.id);
        } else {
          alert('Error creating geomark:' + geomarkInfo.geomarkUrl_Error);
        }
      }
    });
  }
</script>
<p><button class="btn btn-primary btn-sm" onclick="copyGeomarkExample()">Run Copy Geomark Example</button></p>

 * @param {object} parameters The parameters used to create the request.
 */
GeomarkClient.prototype.copyGeomark = function(parameters) {
  var url = this.url + '/geomarks/copy';

  var callback = parameters['callback'];
  delete parameters['callback'];

  parameters['resultFormat'] = 'json';
  $.ajax({
    url : url,
    data : parameters,
    type : "POST",
    dataType : 'json',
    traditional : true,
    crossDomain : true,
    success : function(geomarkInfo) {
      callback(geomarkInfo);
    }
  });
};

/**
<p>Construct a new new geomark from a string containg a KML, GML, or WKT geometry using the
<a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.createGeomark">Create Geomark</a>
REST API.</p>

<p>The parameters to the method are passed in using a Javascript object (map). See the
<a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.createGeomark">Create Geomark</a>
REST API for the list of supported parameters. </p>

<p>In addition the request must include the 'callback' parameter which must be
a Javascript function. This function will be called with a single argument
containing the <a href="../infoAttributes.html">geomark info</a> Javascript object if the geomark was created
successfully. If there was a data error a JavaScript object
with the error details will be returned. Applications may receive other
undocumented results if the wrong URLs are used or the server is not available.</p>

<p>The following code fragment shows an example of using the API.</p>

<pre class="prettyprint"><code class="language-html">
&lt;script type=&quot;text/javascript&quot; src=&quot;https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js&quot;&gt;&lt;/script&gt;
&lt;script type=&quot;text/javascript&quot; src=&quot;https://apps.gov.bc.ca/pub/geomark/js/geomark.js&quot; &gt;&lt;/script&gt;&gt;
&lt;script type=&quot;text/javascript&quot;&gt;
  function createGeomarkExample() {
    var baseUrl = 'https://' + location.hostname + '/pub/geomark';
    var client = new GeomarkClient(baseUrl);
    var geomarks = [
      baseUrl + '/geomarks/gm-abcdefghijklmnopqrstuv0bcislands'
    ];
    client.createGeomark({
      'body': 'SRID=4326;POINT(-128 54)',
      'format': 'wkt',
      'callback': function(geomarkInfo) {
        var geomarkId = geomarkInfo.id;
        if (geomarkId) { 
          alert('Created geomark: ' + geomarkInfo.id);
        } else {
          alert('Error creating geomark:' + geomarkInfo.geomarkUrl_Error);
        }
      }
    });
  }
&lt;/script&gt;
</code></pre>

<script type="text/javascript">
  function createGeomarkExample() {
    var baseUrl = 'https://' + location.hostname + '/pub/geomark';
    var client = new GeomarkClient(baseUrl);
    var geomarks = [
      baseUrl + '/geomarks/gm-abcdefghijklmnopqrstuv0bcislands'
    ];
    client.createGeomark({
      'body': 'SRID=4326;POINT(-128 54)',
      'format': 'wkt',
      'callback': function(geomarkInfo) {
        var geomarkId = geomarkInfo.id;
        if (geomarkId) { 
          alert('Created geomark: ' + geomarkInfo.id);
        } else {
          alert('Error creating geomark:' + geomarkInfo.geomarkUrl_Error);
        }
      }
    });
  }
</script>
<p><button class="btn btn-primary btn-sm" onclick="createGeomarkExample()">Run Create Geomark Example</button></p>
 * 
 * @param {object} parameters The parameters used to create the request.
 */
GeomarkClient.prototype.createGeomark = function(parameters) {
  var url = this.url + '/geomarks/new.' + parameters['format'];
  delete parameters['format'];

  var callback = parameters['callback'];
  delete parameters['callback'];

  parameters['resultFormat'] = 'json';

  $.ajax({
    url : url,
    data : parameters,
    type : "POST",
    dataType : 'json',
    traditional : true,
    success : function(geomarkInfo) {
      callback(geomarkInfo);
    }
  });
};

/**
<p>Get the <a href="../infoAttributes.html">geomark info</a> using the
<a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkInfo">Get Geomark Info</a>
REST API.</p>

<p>The parameters to the method are passed in using a Javascript object (map). See the
<a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkInfo">Get Geomark Info</a>
REST API for the list of supported parameters.</p>

<p>In addition the request must include the 'callback' parameter which must be
a Javascript function. This function will be called with a single argument
containing the <a href="../infoAttributes.html">geomark info</a> Javascript object if the geomark was found
successfully. If there was an error this will be recorded in the Javascript error console.</p>

<p>The following code fragment shows an example of using the API.</p>

<pre class="prettyprint"><code class="language-html">
&lt;script type=&quot;text/javascript&quot; src=&quot;https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js&quot;&gt;&lt;/script&gt;
&lt;script type=&quot;text/javascript&quot; src=&quot;https://apps.gov.bc.ca/pub/geomark/js/geomark.js&quot; &gt;&lt;/script&gt;&gt;
&lt;script type=&quot;text/javascript&quot;&gt;
  function getGeomarkInfoExample() {
    var baseUrl = 'https://apps.gov.bc.ca/pub/geomark';
    var client = new GeomarkClient(baseUrl);
    client.getGeomarkInfo({
      'geomarkId': 'gm-abcdefghijklmnopqrstuv0bcislands',
      'callback': function(geomarkInfo) {
        alert('Found geomark: ' + geomarkInfo.id);
      }
    });
  }
&lt;/script&gt;
</code></pre>

<script type="text/javascript">
  function getGeomarkInfoExample() {
    var baseUrl = 'https://' + location.hostname + '/pub/geomark';
    var client = new GeomarkClient(baseUrl);
    client.getGeomarkInfo({
      'geomarkId': 'gm-abcdefghijklmnopqrstuv0bcislands',
      'callback': function(geomarkInfo) {
        alert('Found geomark: ' + geomarkInfo.id);
      }
    });
  }
</script>
<p><button class="btn btn-primary btn-sm" onclick="getGeomarkInfoExample()">Run Get Geomark Info Example</button></p>

 * @param {object} parameters The parameters used to create the request.
 */
GeomarkClient.prototype.getGeomarkInfo = function(parameters) {
  var geomarkId = parameters['geomarkId'];
  var callback = parameters['callback'];
  var url = this.url + "/geomarks/" + geomarkId + '.json';
  $.ajax({
    url : url,
    dataType : 'json',
    cache : false,
    traditional : true,
    success : function(geomarkInfo) {
      callback(geomarkInfo);
    }
  });
};

/**
<p>Get the <a href="../featureAttributes.html">geomark feature</a> using the
<a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkFeature">Get Geomark Feature</a>
REST API.</p>

<p>The parameters to the method are passed in using a Javascript object (map). See the
<a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkFeature">Get Geomark Feature</a>
REST API for the list of supported parameters.</p>

<p>In addition the request must include the 'callback' parameter which must be
a Javascript function. This function will be called with a single argument
containing the <a href="../featureAttributes.html">geomark feature</a> Javascript object if the geomark was found
successfully. If there was an error this will be recorded in the Javascript error console.</p>

<p>The following code fragment shows an example of using the API.</p>

<pre class="prettyprint"><code class="language-html">
&lt;script type=&quot;text/javascript&quot; src=&quot;https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js&quot;&gt;&lt;/script&gt;
&lt;script type=&quot;text/javascript&quot; src=&quot;https://apps.gov.bc.ca/pub/geomark/js/geomark.js&quot; &gt;&lt;/script&gt;&gt;
&lt;script type=&quot;text/javascript&quot;&gt;
  function getGeomarkFeatureExample() {
    var baseUrl = 'https://apps.gov.bc.ca/pub/geomark';
    var client = new GeomarkClient(baseUrl);
    client.getGeomarkFeature({
      'geomarkId': 'gm-abcdefghijklmnopqrstuv0bcislands',
      'srid': 4326,
      'callback': function(geomarkFeature) {
        alert('Found geomark: ' + geomarkFeature.geometry);
      }
    });
  }
&lt;/script&gt;
</code></pre>

<script type="text/javascript">
  function getGeomarkFeatureExample() {
    var baseUrl = 'https://' + location.hostname + '/pub/geomark';
    var client = new GeomarkClient(baseUrl);
    client.getGeomarkFeature({
      'geomarkId': 'gm-abcdefghijklmnopqrstuv0bcislands',
      'srid': 4326,
      'callback': function(geomarkFeature) {
        alert('Found geomark: ' + geomarkFeature.geometry);
      }
    });
  }
</script>
<p><button class="btn btn-primary btn-sm" onclick="getGeomarkFeatureExample()">Run Get Geomark Feature Example</button></p>

 * @param {object} parameters The parameters used to create the request.
 */
GeomarkClient.prototype.getGeomarkFeature = function(parameters) {
  var geomarkId = parameters['geomarkId'];
  var callback = parameters['callback'];
  var url = this.url + "/geomarks/" + geomarkId + '/feature.json';
  $.ajax({
    url : url,
    dataType : 'json',
    cache : false,
    traditional : true,
    success : function(geomarkFeature) {
      callback(geomarkFeature);
    }
  });
};

/**
<p>Get the <a href="../featureAttributes.html">geomark point</a> using the
<a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkPoint">Get Geomark Point</a>
REST API.</p>

<p>The parameters to the method are passed in using a Javascript object (map). See the
<a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkPoint">Get Geomark Point</a>
REST API for the list of supported parameters.</p>

<p>In addition the request must include the 'callback' parameter which must be
a Javascript function. This function will be called with a single argument
containing the <a href="../featureAttributes.html">geomark point</a> Javascript object if the geomark was found
successfully. If there was an error this will be recorded in the Javascript error console.</p>

<p>The following code fragment shows an example of using the API.</p>

<pre class="prettyprint"><code class="language-html">
&lt;script type=&quot;text/javascript&quot; src=&quot;https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js&quot;&gt;&lt;/script&gt;
&lt;script type=&quot;text/javascript&quot; src=&quot;https://apps.gov.bc.ca/pub/geomark/js/geomark.js&quot; &gt;&lt;/script&gt;&gt;
&lt;script type=&quot;text/javascript&quot;&gt;
  function getGeomarkPointExample() {
    var baseUrl = 'https://apps.gov.bc.ca/pub/geomark';
    var client = new GeomarkClient(baseUrl);
    client.getGeomarkPoint({
      'geomarkId': 'gm-abcdefghijklmnopqrstuv0bcislands',
      'srid': 4326,
      'callback': function(geomarkPoint) {
        alert('Found geomark: ' + geomarkPoint.geometry);
      }
    });
  }
&lt;/script&gt;
</code></pre>

<script type="text/javascript">
  function getGeomarkPointExample() {
    var baseUrl = 'https://' + location.hostname + '/pub/geomark';
    var client = new GeomarkClient(baseUrl);
    client.getGeomarkPoint({
      'geomarkId': 'gm-abcdefghijklmnopqrstuv0bcislands',
      'srid': 4326,
      'callback': function(geomarkPoint) {
        alert('Found geomark: ' + geomarkPoint.geometry);
      }
    });
  }
</script>
<p><button class="btn btn-primary btn-sm" onclick="getGeomarkPointExample()">Run Get Geomark Point Example</button></p>

 * @param {object} parameters The parameters used to create the request.
 */
GeomarkClient.prototype.getGeomarkPoint = function(parameters) {
  var geomarkId = parameters['geomarkId'];
  var callback = parameters['callback'];
  var url = this.url + "/geomarks/" + geomarkId + '/point.json';
  $.ajax({
    url : url,
    dataType : 'json',
    cache : false,
    traditional : true,
    success : function(geomarkPoint) {
      callback(geomarkPoint);
    }
  });
};

/**
<p>Get the <a href="../featureAttributes.html">geomark bounding box</a> using the
<a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkBoundingBox">Get Geomark BoundingBox</a>
REST API.</p>

<p>The parameters to the method are passed in using a Javascript object (map). See the
<a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkBoundingBox">Get Geomark BoundingBox</a>
REST API for the list of supported parameters.</p>

<p>In addition the request must include the 'callback' parameter which must be
a Javascript function. This function will be called with a single argument
containing the <a href="../featureAttributes.html">geomark bounding box</a> Javascript object if the geomark was found
successfully. If there was an error this will be recorded in the Javascript error console.</p>

<p>The following code fragment shows an example of using the API.</p>

<pre class="prettyprint"><code class="language-html">
&lt;script type=&quot;text/javascript&quot; src=&quot;https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js&quot;&gt;&lt;/script&gt;
&lt;script type=&quot;text/javascript&quot; src=&quot;https://apps.gov.bc.ca/pub/geomark/js/geomark.js&quot; &gt;&lt;/script&gt;&gt;
&lt;script type=&quot;text/javascript&quot;&gt;
  function getGeomarkBoundingBoxExample() {
    var baseUrl = 'https://apps.gov.bc.ca/pub/geomark';
    var client = new GeomarkClient(baseUrl);
    client.getGeomarkBoundingBox({
      'geomarkId': 'gm-abcdefghijklmnopqrstuv0bcislands',
      'srid': 4326,
      'callback': function(geomarkBoundingBox) {
        alert('Found geomark: ' + geomarkBoundingBox.geometry);
      }
    });
  }
&lt;/script&gt;
</code></pre>

<script type="text/javascript">
  function getGeomarkBoundingBoxExample() {
    var baseUrl = 'https://' + location.hostname + '/pub/geomark';
    var client = new GeomarkClient(baseUrl);
    client.getGeomarkBoundingBox({
      'geomarkId': 'gm-abcdefghijklmnopqrstuv0bcislands',
      'srid': 4326,
      'callback': function(geomarkBoundingBox) {
        alert('Found geomark: ' + geomarkBoundingBox.geometry);
      }
    });
  }
</script>
<p><button class="btn btn-primary btn-sm" onclick="getGeomarkBoundingBoxExample()">Run Get Geomark BoundingBox Example</button></p>

 * @param {object} parameters The parameters used to create the request.
 */
GeomarkClient.prototype.getGeomarkBoundingBox = function(parameters) {
  var geomarkId = parameters['geomarkId'];
  var callback = parameters['callback'];
  var url = this.url + "/geomarks/" + geomarkId + '/boundingBox.json';
  $.ajax({
    url : url,
    dataType : 'json',
    cache : false,
    traditional : true,
    success : function(geomarkBoundingBox) {
      callback(geomarkBoundingBox);
    }
  });
};

/**
<p>Get the <a href="../featureAttributes.html">geomark parts</a> using the
<a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkParts">Get Geomark Parts</a>
REST API.</p>

<p>The parameters to the method are passed in using a Javascript object (map). See the
<a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkParts">Get Geomark Parts</a>
REST API for the list of supported parameters.</p>

<p>In addition the request must include the 'callback' parameter which must be
a Javascript function. This function will be called with a single argument
containing either a single <a href="../featureAttributes.html">geomark parts</a> Javascript object or a Javascript object with the "items" property containing an array of geomark part Javascript objects if the geomark was found
successfully. If there was an error this will be recorded in the Javascript error console.</p>

<p>The following code fragment shows an example of using the API.</p>

<pre class="prettyprint"><code class="language-html">
&lt;script type=&quot;text/javascript&quot; src=&quot;https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js&quot;&gt;&lt;/script&gt;
&lt;script type=&quot;text/javascript&quot; src=&quot;https://apps.gov.bc.ca/pub/geomark/js/geomark.js&quot; &gt;&lt;/script&gt;&gt;
&lt;script type=&quot;text/javascript&quot;&gt;
  function getGeomarkPartsExample() {
    var baseUrl = 'https://apps.gov.bc.ca/pub/geomark';
    var client = new GeomarkClient(baseUrl);
    client.getGeomarkParts({
      'geomarkId': 'gm-abcdefghijklmnopqrstuv0bcislands',
      'srid': 4326,
      'callback': function(geomarkParts) {
        if (geomarkParts.id) {
          alert('Found geomark: ' + geomarkParts.geometry);
        } else {
          alert('Found geomark: ' + geomarkParts.items[0].geometry);
        }
      }
    });
  }
&lt;/script&gt;
</code></pre>

<script type="text/javascript">
  function getGeomarkPartsExample() {
    var baseUrl = 'https://' + location.hostname + '/pub/geomark';
    var client = new GeomarkClient(baseUrl);
    client.getGeomarkParts({
      'geomarkId': 'gm-abcdefghijklmnopqrstuv0bcislands',
      'srid': 4326,
      'callback': function(geomarkParts) {
        if (geomarkParts.id) {
          alert('Found geomark: ' + geomarkParts.geometry);
        } else {
          alert('Found geomark: ' + geomarkParts.items[0].geometry);
        }
      }
    });
  }
</script>
<p><button class="btn btn-primary btn-sm" onclick="getGeomarkPartsExample()">Run Get Geomark Parts Example</button></p>

 * @param {object} parameters The parameters used to create the request.
 */
GeomarkClient.prototype.getGeomarkParts = function(parameters) {
  var geomarkId = parameters['geomarkId'];
  var callback = parameters['callback'];
  var url = this.url + "/geomarks/" + geomarkId + '/parts.json';
  $.ajax({
    url : url,
    dataType : 'json',
    cache : false,
    traditional : true,
    success : function(geomarkParts) {
      callback(geomarkParts);
    }
  });
};

if (window.XDomainRequest) {
  jQuery.ajaxTransport(function(s) {
    if (s.crossDomain && s.async) {
      if (s.timeout) {
        s.xdrTimeout = s.timeout;
        delete s.timeout;
      }
      var xdr;
      return {
        send : function(_, complete) {
          function callback(status, statusText, responses, responseHeaders) {
            xdr.onload = xdr.onerror = xdr.ontimeout = jQuery.noop;
            xdr = undefined;
            complete(status, statusText, responses, responseHeaders);
          }
          xdr = new XDomainRequest();
          xdr.onload = function() {
            callback(200, "OK", {
              text : xdr.responseText
            }, "Content-Type: " + xdr.contentType);
          };
          xdr.onerror = function() {
            callback(404, "Not Found");
          };
          xdr.onprogress = jQuery.noop;
          xdr.ontimeout = function() {
            callback(0, "timeout");
          };
          xdr.timeout = s.xdrTimeout || Number.MAX_VALUE;
          xdr.open(s.type, s.url);
          xdr.send((s.hasContent && s.data) || null);
        },
        abort : function() {
          if (xdr) {
            xdr.onerror = jQuery.noop;
            xdr.abort();
          }
        }
      };
    }
  });
}
