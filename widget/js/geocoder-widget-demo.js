/*
 * Copyright 2008-2015, Province of British Columbia
 * All rights reserved.
 */
function showCoordsBasic (data) {
	$("#basic_output").fadeOut(function () {
		$(this).
		html("Latitude, Longitude: "+data.lat+","+data.lon).
		fadeIn();
	});
}
function showCoordsAdvanced (data) {
	var html = "<p class='coords'>BC Albers Easting: "+data.lon+", Northing: "+data.lat+"</p>"+
		"<p class='meta'>Locality type is "+data.localityType+"</p>"+
		"<p class='meta'>Location descriptor is "+data.locationDescriptor+"</p>"+
		"<p class='meta'>Positional Accuracy is "+data.locationPositionalAccuracy+"</p>";
	$("#advanced_output").fadeOut(function () {
		$(this).
		html(html).
		fadeIn();
	});
}
