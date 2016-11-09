/*
 * Copyright 2008-2015, Province of British Columbia
 * All rights reserved.
 */
/* --------- GEOCODER -------- */

/**
 * Construct a new GeocodeRequest with the base URL for the
 * Geocoder Web Service (eg. http://apps.gov.bc.ca/pub/Geocoder).
 * 
 * This GeocodeRequest object provides setter functions for all of the
 * various geocode parameters, as well as a way to load the parameters
 * from a form, and a method to get the URL of the request defined by
 * the parameters set. This URL can then be used to navigate the browser
 * to, or in an AJAX request. 
 *  
 * @param baseUrl The url to the Geocoder Web Service
 */
function GeocodeRequest(baseUrl) {
	this.baseUrl = baseUrl;
}

/*
 * Setters for all of the parameters of the geocode Request.
 */
GeocodeRequest.prototype.setTags = function(tags) {
	this.tags = encodeURIComponent(tags);
};

GeocodeRequest.prototype.setOutputFormat = function(outputFormat) {
	this.outputFormat = encodeURIComponent(outputFormat);
};

GeocodeRequest.prototype.setSetBack = function(setBack) {
	this.setBack = encodeURIComponent(setBack);
};

GeocodeRequest.prototype.setMinScore = function(minScore) {
	this.minScore = encodeURIComponent(minScore);
};

GeocodeRequest.prototype.setMatchPrecision = function(matchPrecision) {
	this.matchPrecision = encodeURIComponent(matchPrecision);
};

GeocodeRequest.prototype.setMatchPrecisionNot = function(matchPrecisionNot) {
	this.matchPrecisionNot = encodeURIComponent(matchPrecisionNot);
};

GeocodeRequest.prototype.setLocalities = function(localities) {
	this.localities = encodeURIComponent(localities);
};

GeocodeRequest.prototype.setNotLocalities = function(notLocalities) {
	this.notLocalities = encodeURIComponent(notLocalities);
};

GeocodeRequest.prototype.setCentre = function(centre) {
	this.centre = encodeURIComponent(centre);
};

GeocodeRequest.prototype.setMaxDistance = function(maxDistance) {
	this.maxDistance = encodeURIComponent(maxDistance);
};

GeocodeRequest.prototype.setBbox = function(bbox) {
	this.bbox = encodeURIComponent(bbox);
};

GeocodeRequest.prototype.setParcelPoint = function(parcelPoint) {
	this.bbox = encodeURIComponent(parcelPoint);
};

GeocodeRequest.prototype.setExtrapolate = function(extrapolate) {
	this.extrapolate = encodeURIComponent(extrapolate);
};

GeocodeRequest.prototype.setMaxResults = function(maxResults) {
	this.maxResults = encodeURIComponent(maxResults);
};

GeocodeRequest.prototype.setQuickMatch = function(quickMatch) {
	this.quickMatch = encodeURIComponent(quickMatch);
};

GeocodeRequest.prototype.setIgnoreSites = function(ignoreSites) {
	this.ignoreSites = encodeURIComponent(ignoreSites);
};

GeocodeRequest.prototype.setSitesOnly = function(sitesOnly) {
	this.sitesOnly = encodeURIComponent(sitesOnly);
};

GeocodeRequest.prototype.setInterpolation = function(interpolation) {
	this.interpolation = encodeURIComponent(interpolation);
};

GeocodeRequest.prototype.setEcho = function(echo) {
	this.echo = encodeURIComponent(echo);
};

GeocodeRequest.prototype.setOutputSRS = function(outputSRS) {
	this.outputSRS = encodeURIComponent(outputSRS);
};

GeocodeRequest.prototype.setAddress = function(address) {
	this.address = encodeURIComponent(address);
};

GeocodeRequest.prototype.setSiteName = function(siteName) {
	this.siteName = encodeURIComponent(siteName);
};

GeocodeRequest.prototype.setUnitDesignator = function(unitDesignator) {
	this.unitDesignator = encodeURIComponent(unitDesignator);
};

GeocodeRequest.prototype.setUnitNumber = function(unitNumber) {
	this.unitNumber = encodeURIComponent(unitNumber);
};

GeocodeRequest.prototype.setUnitNumberSuffix = function(unitNumberSuffix) {
	this.unitNumberSuffix = encodeURIComponent(unitNumberSuffix);
};

GeocodeRequest.prototype.setCivicNumber = function(civicNumber) {
	this.civicNumber = encodeURIComponent(civicNumber);
};

GeocodeRequest.prototype.setCivicNumberSuffix = function(civicNumberSuffix) {
	this.civicNumberSuffix = encodeURIComponent(civicNumberSuffix);
};

GeocodeRequest.prototype.setapLocation = function(apLocation) {
	this.apLocation = encodeURIComponent(apLocation);
};

GeocodeRequest.prototype.setStreetName = function(streetName) {
	this.streetName = encodeURIComponent(streetName);
};

GeocodeRequest.prototype.setStreetType = function(streetType) {
	this.streetType = encodeURIComponent(streetType);
};

GeocodeRequest.prototype.setStreetDirection = function(streetDirection) {
	this.streetDirection = encodeURIComponent(streetDirection);
};

GeocodeRequest.prototype.setStreetQualifier = function(streetQualifier) {
	this.streetQualifier = encodeURIComponent(streetQualifier);
};

GeocodeRequest.prototype.setLocality = function(locality) {
	this.locality = encodeURIComponent(locality);
};

GeocodeRequest.prototype.setProvince = function(province) {
	this.province = encodeURIComponent(province);
};

GeocodeRequest.prototype.setLocationDescriptor  = function(locationDescriptor) {
	this.locationDescriptor = encodeURIComponent(locationDescriptor);
};

/**
 * Combines all of the request parameters into a single request URL.
 * This url can then be used, for example, to navigate to or in an AJAX request.
 * @returns the URL used to get the results of the geocode request
 */
GeocodeRequest.prototype.getURL = function() {
	url = this.baseUrl + "/addresses";
	if(typeof this.sitesOnly != undefined && this.sitesOnly) {
		url += "/sites";
	}
	if (this.outputFormat) {
		url += "." + this.outputFormat;
	}
	url += "?";
	if (this.setBack) {
		url += "setBack=" + this.setBack + "&";
	}
	if (this.tags) {
		url += "tags=" + this.tags + "&";
	}
	if (this.minScore) {
		url += "minScore=" + this.minScore + "&";
	}
	if (this.maxResults) {
		url += "maxResults=" + this.maxResults + "&";
	}
	if (this.matchPrecision) {
		url += "matchPrecision=" + this.matchPrecision + "&";
	}
	if (this.matchPrecisionNot) {
		url += "matchPrecisionNot=" + this.matchPrecisionNot + "&";
	}
	if (this.localities) {
		url += "localities=" + this.localities + "&";
	}
	if (this.notLocalities) {
		url += "notLocalities=" + this.notLocalities + "&";
	}
	if (this.centre) {
		url += "centre=" + this.centre + "&";
	}
	if (this.maxDistance) {
		url += "maxDistance=" + this.maxDistance + "&";
	}
	if (this.bbox) {
		url += "bbox=" + this.bbox + "&";
	}
	if (this.parcelPoint) {
		url += "parcelPoint=" + this.parcelPoint + "&";
	}
	if (this.extrapolate) {
		url += "extrapolate=" + this.extrapolate + "&";
	}
	if (typeof this.quickMatch != "undefined") {
		url += "quickMatch=" + this.quickMatch + "&";
	}
	if (typeof this.ignoreSites != "undefined") {
		url += "ignoreSites=" + this.ignoreSites + "&";
	}
	if (typeof this.sitesOnly != "undefined") {
		url += "sitesOnly=" + this.sitesOnly + "&";
	}
	if (typeof this.interpolation != "undefined") {
		url += "interpolation=" + this.interpolation + "&";
	}
	if (typeof this.apLocation != "undefined") {
		url += "apLocation=" + this.apLocation + "&";
	}
	if (typeof this.echo != "undefined") {
		url += "echo=" + this.echo + "&";
	}
	if (this.outputSRS) {
		url += "outputSRS=" + this.outputSRS + "&";
	}
	if (this.address) {
		url += "addressString=" + this.address + "&";
	}
	if (this.siteName) {
		url += "siteName=" + this.siteName + "&";
	}
	if (this.unitDesignator) {
		url += "unitDesignator=" + this.unitDesignator + "&";
	}
	if (this.unitNumber) {
		url += "unitNumber=" + this.unitNumber + "&";
	}
	if (this.unitNumberSuffix) {
		url += "unitNumberSuffix=" + this.unitNumberSuffix + "&";
	}
	if (this.civicNumber) {
		url += "civicNumber=" + this.civicNumber + "&";
	}
	if (this.civicNumberSuffix) {
		url += "civicNumberSuffix=" + this.civicNumberSuffix + "&";
	}
	if (this.streetName) {
		url += "streetName=" + this.streetName + "&";
	}
	if (this.streetType) {
		url += "streetType=" + this.streetType + "&";
	}
	if (this.streetDirection) {
		url += "streetDirection=" + this.streetDirection + "&";
	}
	if (this.streetQualifier) {
		url += "streetQualifier=" + this.streetQualifier + "&";
	}
	if (this.locality) {
		url += "localityName=" + this.locality + "&";
	}
	if (this.province) {
		url += "province=" + this.province + "&";
	}
	if (this.locationDescriptor) {
		url += "locationDescriptor=" + this.locationDescriptor + "&";
	}
	return url;
};

/**
 * Reads all of the values for the geocodeRequest parameters from a form.
 * The form fields must be named the same as the request parameters, and
 * must be either simple text fields or select boxes with the correct values set.
 * @param theForm the form from which to read (eg. document.forms[0] or document.geocodeForm)
 */
GeocodeRequest.prototype.readForm = function(theForm) {
	if (theForm.outputFormat) {
		this.setOutputFormat(theForm.outputFormat.value);
	}
	if (theForm.setBack) {
		this.setSetBack(theForm.setBack.value);
	}
	if (theForm.tags) {
		this.setTags(theForm.tags.value);
	}
	if (theForm.minScore) {
		this.setMinScore(theForm.minScore.value);
	}
	if (theForm.maxResults) {
		this.setMaxResults(theForm.maxResults.value);
	}
	if (theForm.matchPrecision) {
		this.setMatchPrecision(theForm.matchPrecision.value);
	}
	if (theForm.matchPrecisionNot) {
		this.setMatchPrecisionNot(theForm.matchPrecisionNot.value);
	}
	if (theForm.localities) {
		this.setLocalities(theForm.localities.value);
	}
	if (theForm.notLocalities) {
		this.setNotLocalities(theForm.notLocalities.value);
	}
	if (theForm.centre) {
		this.setCentre(theForm.centre.value);
	}
	if (theForm.maxDistance) {
		this.setMaxDistance(theForm.maxDistance.value);
	}
	if (theForm.bbox) {
		this.setBbox(theForm.bbox.value);
	}
	if (theForm.parcelPoint) {
		this.setParcelPoint(theForm.parcelPoint.value);
	}
	if (theForm.extrapolate) {
		this.setExtrapolate(theForm.extrapolate.value);
	}
	if (theForm.quickMatch) {
		this.setQuickMatch(theForm.quickMatch.value);
	}
	if (theForm.quickMatchCheck) {
		this.setQuickMatch(theForm.quickMatchCheck.checked);
	}
	if (theForm.Interp[0].checked){
		this.setInterpolation(theForm.Interp[0].value);
	}
	if (theForm.Interp[1].checked){
		this.setInterpolation(theForm.Interp[1].value);
	}
	if (theForm.Interp[2].checked){
		this.setInterpolation(theForm.Interp[2].value);
	}
	if (theForm.apLocationCheck) {
		this.setapLocation(theForm.apLocationCheck.checked);
	}
	if (theForm.echo) {
		this.setEcho(theForm.echo.value);
	}
	if (theForm.echoCheck) {
		this.setEcho(theForm.echoCheck.checked);
	}
	if (theForm.outputSRS) {
		this.setOutputSRS(theForm.outputSRS.value);
	}
	if (theForm.address) {
		var addr = theForm.address.value;
		if(theForm.name && theForm.name.value.trim().length > 0) {
			if(theForm.name.value.indexOf("--") == -1
					&& theForm.address.value.indexOf("--") == -1) {
				addr = theForm.name.value + " -- " + theForm.address.value;
			} else {
				addr = theForm.name.value + " " + theForm.address.value;
			}
		}
		this.setAddress(addr);
	}
	if (theForm.siteName) {
		this.setSiteName(theForm.siteName.value);
	}
	if (theForm.unitDesignator) {
		this.setUnitDesignator(theForm.unitDesignator.value);
	}
	if (theForm.unitNumber) {
		this.setUnitNumber(theForm.unitNumber.value);
	}
	if (theForm.unitNumberSuffix) {
		this.setUnitNumberSuffix(theForm.unitNumberSuffix.value);
	}
	if (theForm.civicNumber) {
		this.setCivicNumber(theForm.civicNumber.value);
	}
	if (theForm.civicNumberSuffix) {
		this.setCivicNumberSuffix(theForm.civicNumberSuffix.value);
	}
	if (theForm.streetName) {
		this.setStreetName(theForm.streetName.value);
	}
	if (theForm.streetType) {
		this.setStreetType(theForm.streetType.value);
	}
	if (theForm.streetDirection) {
		this.setStreetDirection(theForm.streetDirection.value);
	}
	if (theForm.streetQualifier) {
		this.setStreetQualifier(theForm.streetQualifier.value);
	}
	if (theForm.locality) {
		this.setLocality(theForm.locality.value);
	}
	if (theForm.province) {
		this.setProvince(theForm.province.value);
	}
	if (theForm.locationDescriptor) {
		this.setLocationDescriptor(theForm.locationDescriptor.value);
	}
};

/* ------------------ SITES ---------------------- */

/* ---------------- Sites By Id ------------------ */

function siteIdRequest(baseUrl) {
	this.baseUrl = baseUrl;
}

siteIdRequest.prototype.setSiteId = function(siteId) {
	this.siteId = siteId;
};

siteIdRequest.prototype.setOutputSRS = function(outputSRS) {
	this.outputSRS = outputSRS;
};

siteIdRequest.prototype.setOutputFormat = function(outputFormat) {
	this.outputFormat = outputFormat;
};

siteIdRequest.prototype.setLocationDescriptor = function(locationDescriptor) {
	this.locationDescriptor = locationDescriptor;
};

siteIdRequest.prototype.getURL = function() {
	
	if (this.siteId){
		url = this.baseUrl + "/sites/" + this.siteId;
	}else{
		url = this.baseUrl + "/sites/empty";
	}
	if (this.outputFormat) {
		url += "." + this.outputFormat;
	}
	url += "?";
	if (this.minScore) {
		url += "minScore=" + this.minScore + "&";
	}
	if (this.outputSRS) {
		url += "outputSRS=" + this.outputSRS + "&";
	}
	if (this.locationDescriptor) {
		url += "locationDescriptor=" + this.locationDescriptor;
	}

	return url;
};

siteIdRequest.prototype.readForm = function(theForm) {
	if (theForm.outputFormat) {
		this.setOutputFormat(theForm.outputFormat.value);
	}
	if (theForm.outputSRS) {
		this.setOutputSRS(theForm.outputSRS.value);
	}
	if (theForm.siteId) {
		this.setSiteId(theForm.siteId.value);
	}
	if (theForm.locationDescriptor) {
		this.setLocationDescriptor(theForm.locationDescriptor.value);
	}
};

/* ---------------- SubSites By Id ------------------ */

function subSitesIdRequest(baseUrl) {
	this.baseUrl = baseUrl;
}

subSitesIdRequest.prototype.setSiteId = function(siteId) {
	this.siteId = siteId;
};

subSitesIdRequest.prototype.setOutputSRS = function(outputSRS) {
	this.outputSRS = outputSRS;
};

subSitesIdRequest.prototype.setOutputFormat = function(outputFormat) {
	this.outputFormat = outputFormat;
};

subSitesIdRequest.prototype.setLocationDescriptor = function(locationDescriptor) {
	this.locationDescriptor = locationDescriptor;
};

subSitesIdRequest.prototype.getURL = function() {
	if (this.siteId){
		url = this.baseUrl + "/sites/" + this.siteId + "/subsites";
	}else{
		url = this.baseUrl + "/sites/empty/subsites";
	}
	
	if (this.outputFormat) {
		url += "." + this.outputFormat;
	}
	url += "?";
	if (this.minScore) {
		url += "minScore=" + this.minScore + "&";
	}
	if (this.outputSRS) {
		url += "outputSRS=" + this.outputSRS + "&";
	}
	if (this.locationDescriptor) {
		url += "locationDescriptor=" + this.locationDescriptor;
	}

	return url;
};

subSitesIdRequest.prototype.readForm = function(theForm) {
	if (theForm.outputFormat) {
		this.setOutputFormat(theForm.outputFormat.value);
	}
	if (theForm.outputSRS) {
		this.setOutputSRS(theForm.outputSRS.value);
	}
	if (theForm.siteId) {
		this.setSiteId(theForm.siteId.value);
	}
	if (theForm.locationDescriptor) {
		this.setLocationDescriptor(theForm.locationDescriptor.value);
	}
};

/* ---------------- Nearest Site ------------------ */

function nearestSiteRequest(baseUrl) {
	this.baseUrl = baseUrl;
}

nearestSiteRequest.prototype.setPoint = function(point) {
	this.point = point;
};

nearestSiteRequest.prototype.setOutputSRS = function(outputSRS) {
	this.outputSRS = outputSRS;
};

nearestSiteRequest.prototype.setOutputFormat = function(outputFormat) {
	this.outputFormat = outputFormat;
};

nearestSiteRequest.prototype.setLocationDescriptor = function(locationDescriptor) {
	this.locationDescriptor = locationDescriptor;
};

nearestSiteRequest.prototype.getURL = function() {
	url = this.baseUrl + "/sites/nearest";
	if (this.outputFormat) {
		url += "." + this.outputFormat;
	}
	url += "?point=" + this.point + "&";
	
	if (this.minScore) {
		url += "minScore=" + this.minScore + "&";
	}
	if (this.outputSRS) {
		url += "outputSRS=" + this.outputSRS + "&";
	}
	if (this.maxResults) {
		url += "maxResults=" + this.maxResults + "&";
	}
	if (this.locationDescriptor) {
		url += "locationDescriptor=" + this.locationDescriptor;
	}

	return url;
};

nearestSiteRequest.prototype.readForm = function(theForm) {
	if (theForm.outputFormat) {
		this.setOutputFormat(theForm.outputFormat.value);
	}
	if (theForm.outputSRS) {
		this.setOutputSRS(theForm.outputSRS.value);
	}
	if (theForm.point) {
		this.setPoint(theForm.point.value);
	}
	if (theForm.locationDescriptor) {
		this.setLocationDescriptor(theForm.locationDescriptor.value);
	}
};

/* ---------------- Nearest Site ------------------ */

function sitesNearRequest(baseUrl) {
	this.baseUrl = baseUrl;
}

sitesNearRequest.prototype.setPoint = function(point) {
	this.point = point;
};

sitesNearRequest.prototype.setOutputSRS = function(outputSRS) {
	this.outputSRS = outputSRS;
};

sitesNearRequest.prototype.setOutputFormat = function(outputFormat) {
	this.outputFormat = outputFormat;
};

sitesNearRequest.prototype.setMaxResults = function(maxResults) {
	this.maxResults = maxResults;
};

sitesNearRequest.prototype.setLocationDescriptor = function(locationDescriptor) {
	this.locationDescriptor = locationDescriptor;
};

sitesNearRequest.prototype.getURL = function() {
	url = this.baseUrl + "/sites/near";
	if (this.outputFormat) {
		url += "." + this.outputFormat;
	}
	url += "?point=" + this.point + "&";
	
	if (this.minScore) {
		url += "minScore=" + this.minScore + "&";
	}
	if (this.outputSRS) {
		url += "outputSRS=" + this.outputSRS + "&";
	}
	if (this.maxResults) {
		url += "maxResults=" + this.maxResults + "&";
	}
	if (this.locationDescriptor) {
		url += "locationDescriptor=" + this.locationDescriptor;
	}

	return url;
};

sitesNearRequest.prototype.readForm = function(theForm) {
	if (theForm.outputFormat) {
		this.setOutputFormat(theForm.outputFormat.value);
	}
	if (theForm.outputSRS) {
		this.setOutputSRS(theForm.outputSRS.value);
	}
	if (theForm.point) {
		this.setPoint(theForm.point.value);
	}
	if (theForm.maxResults) {
		this.setMaxResults(theForm.maxResults.value);
	}
	if (theForm.locationDescriptor) {
		this.setLocationDescriptor(theForm.locationDescriptor.value);
	}
};

/* ---------------- Sites Within ------------------ */

function sitesWithinRequest(baseUrl) {
	this.baseUrl = baseUrl;
}

sitesWithinRequest.prototype.setBbox = function(bbox) {
	this.bbox = bbox;
};

sitesWithinRequest.prototype.setOutputSRS = function(outputSRS) {
	this.outputSRS = outputSRS;
};

sitesWithinRequest.prototype.setOutputFormat = function(outputFormat) {
	this.outputFormat = outputFormat;
};

sitesWithinRequest.prototype.setMaxResults = function(maxResults) {
	this.maxResults = maxResults;
};

sitesWithinRequest.prototype.setLocationDescriptor = function(locationDescriptor) {
	this.locationDescriptor = locationDescriptor;
};

sitesWithinRequest.prototype.getURL = function() {
	url = this.baseUrl + "/sites/within";
	if (this.outputFormat) {
		url += "." + this.outputFormat;
	}
	url += "?bbox=" + this.bbox + "&";
	
	if (this.minScore) {
		url += "minScore=" + this.minScore + "&";
	}
	if (this.outputSRS) {
		url += "outputSRS=" + this.outputSRS + "&";
	}
	if (this.maxResults) {
		url += "maxResults=" + this.maxResults + "&";
	}
	if (this.locationDescriptor) {
		url += "locationDescriptor=" + this.locationDescriptor;
	}

	return url;
};

sitesWithinRequest.prototype.readForm = function(theForm) {
	if (theForm.outputFormat) {
		this.setOutputFormat(theForm.outputFormat.value);
	}
	if (theForm.outputSRS) {
		this.setOutputSRS(theForm.outputSRS.value);
	}
	if (theForm.bbox) {
		this.setBbox(theForm.bbox.value);
	}
	if (theForm.maxResults) {
		this.setMaxResults(theForm.maxResults.value);
	}
	if (theForm.locationDescriptor) {
		this.setLocationDescriptor(theForm.locationDescriptor.value);
	}
};

/* ------------------ OCCUPANTS ---------------------- */

/* ---------------- Occupants By Id ------------------ */

function occupantIdRequest(baseUrl) {
	this.baseUrl = baseUrl;
}

occupantIdRequest.prototype.setOccupantId = function(occupantId) {
	this.occupantId = occupantId;
};

occupantIdRequest.prototype.setOutputSRS = function(outputSRS) {
	this.outputSRS = outputSRS;
};

occupantIdRequest.prototype.setOutputFormat = function(outputFormat) {
	this.outputFormat = outputFormat;
};

occupantIdRequest.prototype.setLocationDescriptor = function(locationDescriptor) {
	this.locationDescriptor = locationDescriptor;
};

occupantIdRequest.prototype.getURL = function() {
	
	if (this.occupantId){
		url = this.baseUrl + "/occupants/" + this.occupantId;
	}else{
		url = this.baseUrl + "/occupants/empty";
	}
	if (this.outputFormat) {
		url += "." + this.outputFormat;
	}
	url += "?";
	if (this.minScore) {
		url += "minScore=" + this.minScore + "&";
	}
	if (this.outputSRS) {
		url += "outputSRS=" + this.outputSRS + "&";
	}
	if (this.locationDescriptor) {
		url += "locationDescriptor=" + this.locationDescriptor;
	}

	return url;
};

occupantIdRequest.prototype.readForm = function(theForm) {
	if (theForm.outputFormat) {
		this.setOutputFormat(theForm.outputFormat.value);
	}
	if (theForm.outputSRS) {
		this.setOutputSRS(theForm.outputSRS.value);
	}
	if (theForm.occupantId) {
		this.setOccupantId(theForm.occupantId.value);
	}
	if (theForm.locationDescriptor) {
		this.setLocationDescriptor(theForm.locationDescriptor.value);
	}
};

/* ---------------- Nearest Occupant ------------------ */

function nearestOccupantRequest(baseUrl) {
	this.baseUrl = baseUrl;
}

nearestOccupantRequest.prototype.setPoint = function(point) {
	this.point = point;
};

nearestOccupantRequest.prototype.setTags = function(tags) {
	this.tags = tags;
};

nearestOccupantRequest.prototype.setOutputSRS = function(outputSRS) {
	this.outputSRS = outputSRS;
};

nearestOccupantRequest.prototype.setOutputFormat = function(outputFormat) {
	this.outputFormat = outputFormat;
};

nearestOccupantRequest.prototype.setLocationDescriptor = function(locationDescriptor) {
	this.locationDescriptor = locationDescriptor;
};

nearestOccupantRequest.prototype.getURL = function() {
	url = this.baseUrl + "/occupants/nearest";
	if (this.outputFormat) {
		url += "." + this.outputFormat;
	}
	url += "?point=" + this.point + "&";
	
	if (this.tags) {
		url += "tags=" + this.tags + "&";
	}
	if (this.minScore) {
		url += "minScore=" + this.minScore + "&";
	}
	if (this.outputSRS) {
		url += "outputSRS=" + this.outputSRS + "&";
	}
	if (this.maxResults) {
		url += "maxResults=" + this.maxResults + "&";
	}
	if (this.locationDescriptor) {
		url += "locationDescriptor=" + this.locationDescriptor;
	}

	return url;
};

nearestOccupantRequest.prototype.readForm = function(theForm) {
	if (theForm.outputFormat) {
		this.setOutputFormat(theForm.outputFormat.value);
	}
	if (theForm.outputSRS) {
		this.setOutputSRS(theForm.outputSRS.value);
	}
	if (theForm.point) {
		this.setPoint(theForm.point.value);
	}
	if (theForm.tags) {
		this.setTags(theForm.tags.value);
	}
	if (theForm.locationDescriptor) {
		this.setLocationDescriptor(theForm.locationDescriptor.value);
	}
};

/* ---------------- Occupants Near ------------------ */

function occupantsNearRequest(baseUrl) {
	this.baseUrl = baseUrl;
}

occupantsNearRequest.prototype.setPoint = function(point) {
	this.point = point;
};

occupantsNearRequest.prototype.setTags = function(tags) {
	this.tags = tags;
};

occupantsNearRequest.prototype.setOutputSRS = function(outputSRS) {
	this.outputSRS = outputSRS;
};

occupantsNearRequest.prototype.setOutputFormat = function(outputFormat) {
	this.outputFormat = outputFormat;
};

occupantsNearRequest.prototype.setMaxResults = function(maxResults) {
	this.maxResults = maxResults;
};

occupantsNearRequest.prototype.setLocationDescriptor = function(locationDescriptor) {
	this.locationDescriptor = locationDescriptor;
};

occupantsNearRequest.prototype.getURL = function() {
	url = this.baseUrl + "/occupants/near";
	if (this.outputFormat) {
		url += "." + this.outputFormat;
	}
	url += "?point=" + this.point + "&";
	
	if (this.tags) {
		url += "tags=" + this.tags + "&";
	}
	if (this.minScore) {
		url += "minScore=" + this.minScore + "&";
	}
	if (this.outputSRS) {
		url += "outputSRS=" + this.outputSRS + "&";
	}
	if (this.maxResults) {
		url += "maxResults=" + this.maxResults + "&";
	}
	if (this.locationDescriptor) {
		url += "locationDescriptor=" + this.locationDescriptor;
	}

	return url;
};

occupantsNearRequest.prototype.readForm = function(theForm) {
	if (theForm.outputFormat) {
		this.setOutputFormat(theForm.outputFormat.value);
	}
	if (theForm.outputSRS) {
		this.setOutputSRS(theForm.outputSRS.value);
	}
	if (theForm.point) {
		this.setPoint(theForm.point.value);
	}
	if (theForm.tags) {
		this.setTags(theForm.tags.value);
	}
	if (theForm.maxResults) {
		this.setMaxResults(theForm.maxResults.value);
	}
	if (theForm.locationDescriptor) {
		this.setLocationDescriptor(theForm.locationDescriptor.value);
	}
};

/* ---------------- Occupants Within ------------------ */

function occupantsWithinRequest(baseUrl) {
	this.baseUrl = baseUrl;
}

occupantsWithinRequest.prototype.setBbox = function(bbox) {
	this.bbox = bbox;
};

occupantsWithinRequest.prototype.setTags = function(tags) {
	this.tags = tags;
};

occupantsWithinRequest.prototype.setOutputSRS = function(outputSRS) {
	this.outputSRS = outputSRS;
};

occupantsWithinRequest.prototype.setOutputFormat = function(outputFormat) {
	this.outputFormat = outputFormat;
};

occupantsWithinRequest.prototype.setMaxResults = function(maxResults) {
	this.maxResults = maxResults;
};

occupantsWithinRequest.prototype.setLocationDescriptor = function(locationDescriptor) {
	this.locationDescriptor = locationDescriptor;
};

occupantsWithinRequest.prototype.getURL = function() {
	url = this.baseUrl + "/occupants/within";
	if (this.outputFormat) {
		url += "." + this.outputFormat;
	}
	url += "?bbox=" + this.bbox + "&";
	
	if (this.tags) {
		url += "tags=" + this.tags + "&";
	}
	if (this.minScore) {
		url += "minScore=" + this.minScore + "&";
	}
	if (this.outputSRS) {
		url += "outputSRS=" + this.outputSRS + "&";
	}
	if (this.maxResults) {
		url += "maxResults=" + this.maxResults + "&";
	}
	if (this.locationDescriptor) {
		url += "locationDescriptor=" + this.locationDescriptor;
	}

	return url;
};

occupantsWithinRequest.prototype.readForm = function(theForm) {
	if (theForm.outputFormat) {
		this.setOutputFormat(theForm.outputFormat.value);
	}
	if (theForm.outputSRS) {
		this.setOutputSRS(theForm.outputSRS.value);
	}
	if (theForm.bbox) {
		this.setBbox(theForm.bbox.value);
	}
	if (theForm.tags) {
		this.setTags(theForm.tags.value);
	}
	if (theForm.maxResults) {
		this.setMaxResults(theForm.maxResults.value);
	}
	if (theForm.locationDescriptor) {
		this.setLocationDescriptor(theForm.locationDescriptor.value);
	}
};

/* ------------------- INTERSECTIONS -------------------- */

/* ---------------- Intersection By Id ------------------ */

function intersectionIdRequest(baseUrl) {
	this.baseUrl = baseUrl;
}

intersectionIdRequest.prototype.setIntersectionId = function(intersectionId) {
	this.intersectionId = intersectionId;
};

intersectionIdRequest.prototype.setOutputSRS = function(outputSRS) {
	this.outputSRS = outputSRS;
};

intersectionIdRequest.prototype.setOutputFormat = function(outputFormat) {
	this.outputFormat = outputFormat;
};

intersectionIdRequest.prototype.getURL = function() {
	if (this.intersectionId){
		url = this.baseUrl + "/intersections/" + this.intersectionId;
	}else{
		url = this.baseUrl + "/intersections/empty";
	}
	if (this.outputFormat) {
		url += "." + this.outputFormat;
	}
	url += "?";
	if (this.minScore) {
		url += "minScore=" + this.minScore + "&";
	}
	if (this.outputSRS) {
		url += "outputSRS=" + this.outputSRS + "&";
	}

	return url;
};

intersectionIdRequest.prototype.readForm = function(theForm) {
	if (theForm.outputFormat) {
		this.setOutputFormat(theForm.outputFormat.value);
	}
	if (theForm.outputSRS) {
		this.setOutputSRS(theForm.outputSRS.value);
	}
	if (theForm.intersectionId) {
		this.setIntersectionId(theForm.intersectionId.value);
	}
};

/* ---------------- Nearest Intersection ------------------ */

function nearestIntersectionRequest(baseUrl) {
	this.baseUrl = baseUrl;
}

nearestIntersectionRequest.prototype.setPoint = function(point) {
	this.point = point;
};

nearestIntersectionRequest.prototype.setOutputSRS = function(outputSRS) {
	this.outputSRS = outputSRS;
};

nearestIntersectionRequest.prototype.setOutputFormat = function(outputFormat) {
	this.outputFormat = outputFormat;
};

nearestIntersectionRequest.prototype.setMinDegree = function(minDegree) {
	this.minDegree = minDegree;
};

nearestIntersectionRequest.prototype.setMaxDegree = function(maxDegree) {
	this.maxDegree = maxDegree;
};

nearestIntersectionRequest.prototype.getURL = function() {
	url = this.baseUrl + "/intersections/nearest";
	if (this.outputFormat) {
		url += "." + this.outputFormat;
	}
	url += "?point=" + this.point + "&";
	
	if (this.minScore) {
		url += "minScore=" + this.minScore + "&";
	}
	if (this.outputSRS) {
		url += "outputSRS=" + this.outputSRS + "&";
	}
	if (this.minDegree) {
		url += "minDegree=" + this.minDegree + "&";
	}
	if (this.maxDegree) {
		url += "maxDegree=" + this.maxDegree + "&";
	}

	return url;
};

nearestIntersectionRequest.prototype.readForm = function(theForm) {
	if (theForm.outputFormat) {
		this.setOutputFormat(theForm.outputFormat.value);
	}
	if (theForm.outputSRS) {
		this.setOutputSRS(theForm.outputSRS.value);
	}
	if (theForm.point) {
		this.setPoint(theForm.point.value);
	}
	if (theForm.minDegree) {
		this.setMinDegree(theForm.minDegree.value);
	}
	if (theForm.maxDegree) {
		this.setMaxDegree(theForm.maxDegree.value);
	}
};

/* ---------------- Intersections Near ------------------ */

function intersectionsNearRequest(baseUrl) {
	this.baseUrl = baseUrl;
}

intersectionsNearRequest.prototype.setPoint = function(point) {
	this.point = point;
};

intersectionsNearRequest.prototype.setOutputSRS = function(outputSRS) {
	this.outputSRS = outputSRS;
};

intersectionsNearRequest.prototype.setOutputFormat = function(outputFormat) {
	this.outputFormat = outputFormat;
};

intersectionsNearRequest.prototype.setMaxResults = function(maxResults) {
	this.maxResults = maxResults;
};

intersectionsNearRequest.prototype.setMinDegree = function(minDegree) {
	this.minDegree = minDegree;
};

intersectionsNearRequest.prototype.setMaxDegree = function(maxDegree) {
	this.maxDegree = maxDegree;
};

intersectionsNearRequest.prototype.getURL = function() {
	url = this.baseUrl + "/intersections/near";
	if (this.outputFormat) {
		url += "." + this.outputFormat;
	}
	url += "?point=" + this.point + "&";
	
	if (this.minScore) {
		url += "minScore=" + this.minScore + "&";
	}
	if (this.outputSRS) {
		url += "outputSRS=" + this.outputSRS + "&";
	}
	if (this.maxResults) {
		url += "maxResults=" + this.maxResults + "&";
	}
	if (this.minDegree) {
		url += "minDegree=" + this.minDegree + "&";
	}
	if (this.maxDegree) {
		url += "maxDegree=" + this.maxDegree + "&";
	}

	return url;
};

intersectionsNearRequest.prototype.readForm = function(theForm) {
	if (theForm.outputFormat) {
		this.setOutputFormat(theForm.outputFormat.value);
	}
	if (theForm.outputSRS) {
		this.setOutputSRS(theForm.outputSRS.value);
	}
	if (theForm.point) {
		this.setPoint(theForm.point.value);
	}
	if (theForm.maxResults) {
		this.setMaxResults(theForm.maxResults.value);
	}
	if (theForm.minDegree) {
		this.setMinDegree(theForm.minDegree.value);
	}
	if (theForm.maxDegree) {
		this.setMaxDegree(theForm.maxDegree.value);
	}
};


/* ---------------- Intersections Within ------------------ */

function intersectionsWithinRequest(baseUrl) {
	this.baseUrl = baseUrl;
}

intersectionsWithinRequest.prototype.setBbox = function(bbox) {
	this.bbox = bbox;
};

intersectionsWithinRequest.prototype.setOutputSRS = function(outputSRS) {
	this.outputSRS = outputSRS;
};

intersectionsWithinRequest.prototype.setOutputFormat = function(outputFormat) {
	this.outputFormat = outputFormat;
};

intersectionsWithinRequest.prototype.setMaxResults = function(maxResults) {
	this.maxResults = maxResults;
};

intersectionsWithinRequest.prototype.setMinDegree = function(minDegree) {
	this.minDegree = minDegree;
};

intersectionsWithinRequest.prototype.setMaxDegree = function(maxDegree) {
	this.maxDegree = maxDegree;
};

intersectionsWithinRequest.prototype.getURL = function() {
	url = this.baseUrl + "/intersections/within";
	if (this.outputFormat) {
		url += "." + this.outputFormat;
	}
	url += "?bbox=" + this.bbox + "&";
	
	if (this.minScore) {
		url += "minScore=" + this.minScore + "&";
	}
	if (this.outputSRS) {
		url += "outputSRS=" + this.outputSRS + "&";
	}
	if (this.maxResults) {
		url += "maxResults=" + this.maxResults + "&";
	}
	if (this.minDegree) {
		url += "minDegree=" + this.minDegree + "&";
	}
	if (this.maxDegree) {
		url += "maxDegree=" + this.maxDegree + "&";
	}

	return url;
};

intersectionsWithinRequest.prototype.readForm = function(theForm) {
	if (theForm.outputFormat) {
		this.setOutputFormat(theForm.outputFormat.value);
	}
	if (theForm.outputSRS) {
		this.setOutputSRS(theForm.outputSRS.value);
	}
	if (theForm.bbox) {
		this.setBbox(theForm.bbox.value);
	}
	if (theForm.maxResults) {
		this.setMaxResults(theForm.maxResults.value);
	}
	if (theForm.minDegree) {
		this.setMinDegree(theForm.minDegree.value);
	}
	if (theForm.maxDegree) {
		this.setMaxDegree(theForm.maxDegree.value);
	}
};

