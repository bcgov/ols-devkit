/*
 * Copyright 2008-2015, Province of British Columbia
 * All rights reserved.
 */

var geocoder_url;
var queryDict = {};
location.search.substr(1).split("&").forEach(function(item) {queryDict[item.split("=")[0]] = item.split("=")[1]});
var env = queryDict["env"];
if(env == "devel") {
	geocoder_url = "http://localhost:8080/pub/geocoder";
} else if(env == "deliv") {
	geocoder_url = "https://delivery.apps.gov.bc.ca/pub/geocoder";
} else if(env == "test") {
	geocoder_url = "https://test.apps.gov.bc.ca/pub/geocoder";
} else {
	geocoder_url = "https://apps.gov.bc.ca/pub/geocoder";
}

var MAX_REQUESTS = 1000;

var ROW_NUM_COL = 1;
var ADDRESS_STRING_COL = 2;
var FULL_ADDRESS_COL = 3;
var SCORE_COL = 4;
var MATCH_PRECISION_COL = 5;
var FAULTS_COL = 6;
var X_COL = 7;
var Y_COL = 8;

var completed = 0;
var toComplete = 0;
var errors = 0;
var rowCount = 0;
var delimiter;
var addressStringField;
var otherFields = [];

//cutoff to mark the score in red text < score_cutoff will be marked.
var SCORE_CUTOFF = 85;

$(document).ready(function() {
	// hide the stage-2 parts
	$('.stage2').hide();

	// hide pop-up divs
	$('#moreInfo').hide();
	$('#copyToClipboard').hide();

	// setup button events
	$('#restartButton').click(restart);

	$('#moreInfoButton').click(function() {
		$('#moreInfo').show();
	});

	$('#closeMoreInfoButton').click(function() {
		$('#moreInfo').hide();
	});

	$('#closeCopyToClipboardButton').click(function() {
		$('#copyArea').val("");
		$('#copyToClipboard').hide();
	});

	$('#exportResultsButton').click(function(){
		csv = table2string(',');
		export2csv(csv);
	});

	$('#copyResultsButton').click(function(){
		results = table2string();
		$('#copyArea').val(results);
		$('#copyToClipboard').show();
		$('#copyArea').select();
	});

	$('#geocodeButton').click(function() {
		var inputText = $('#inputArea').val();
		matches = inputText.match(/^\s*$/);
		if(matches != null) {
			alert('No input given, please provide some data to geocode.');
			return;
		}
		matches = inputText.match(/\n/g);
		if(matches == null) {
			alert('You must provide a header row with column names in addition to the actual data to be geocoded.');
			return;
		}
		if(matches.length > MAX_REQUESTS+1) {
			alert('No more than 1000 requests are allowed. Please reduce the number of requests.');
			return;
		}
		completed = 0;
		toComplete = -1;
		updateStatus();
	    Papa.parse($('#inputArea').val(), {header: true, worker: false,
	    	step: function(results, parser) {
	    		// if this is the first row, identify the input fields and add any additional column headings
	    		if(rowCount == 0) {
	    			for(var i = 0; i < results.meta.fields.length; i++) {
	    				if(results.meta.fields[i].match(/^\s*addressString\s*$/) != null) {
	    					addressStringField = results.meta.fields[i];
	    				} else {
	    					var f = results.meta.fields[i];

	    					if(f != "fullAddress" && f != "score" && f != "precision"
	    							&& f != "faults" && f != "X" && f != "Y" && f != "Notes" ) {
	    						otherFields.push(results.meta.fields[i]);
	    						$('#resultsTable thead tr:first').append($('<th class="extraCol"/>').text(results.meta.fields[i]));
	    					}
	    				}
	    			}
	    			delimiter = results.meta.delimiter;
	    			if(addressStringField == undefined) {
	    				alert("No column named 'addressString' could be found. The data must include the column headings, and there must be one column named addressString which must contain the input address.");
	    				parser.abort();
	    				return;
	    			}
	    			$('.stage1').hide();
	    			$('.stage2').show();
	    		}
	    		// add the row to the table
	    		rowCount++;
	    		var rowData = results.data[0];
	    		var noteVal = "";
	    		if(rowData['Notes'] != undefined) {
	    			noteVal = ' value="' + rowData['Notes'] + '"';
	    		}
	    		$('#resultsTable').append('<tr id="row' + rowCount + '">'
	    				+ '<td style="border-bottom: none;">' + rowCount + '</td>'
	    				+ '<td style="border-bottom: none;"><textarea class="addressString" cols="35" rows="2" id="addressString' + rowCount
	    					+ '">' + rowData['addressString'] + '</textarea></td>'
	    				+ '<td style="border-bottom: none;"><img src="img/ajax-loader.gif" title="processing..."/></td>' // FULL_ADDRESS_COL
	    				+ '<td rowspan="2"></td>' // SCORE_COL
	    				+ '<td rowspan="2"></td>' // MATCH_PRECISION_COL
	    				+ '<td rowspan="2"></td>' // FAULTS_COL
	    				+ '<td rowspan="2"></td>' // X_COL
	    				+ '<td rowspan="2"></td>' // Y_COL
	    				+ '<td rowspan="2"><input type="text" size="25"' + noteVal + '/></td>' // NOTES_COL
	    				+ '</tr><tr id="row' + rowCount + 'r2">'
	    				+ '<td class="buttonCell"><input type="button" id="deleteRow' + rowCount + '" value="Delete"/></td>'
	    				+ '<td class="buttonCell"><input type="button" id="reGeocodeRow' + rowCount + '" value="Geocode"/></td>'
	    				+ '<td class="buttonCell"></td>' // Show in Map button
	    				+ '</tr>');
	    		// additional/optional fields
	    		for(var i = 0; i < otherFields.length; i++) {
	    			$('#row' + rowCount).append('<td rowspan="2" class="extraCol">' + rowData[otherFields[i]] + '</td>');
	    		}
	    		$('#deleteRow'+rowCount).click(function(rowNum) {return function() {deleteRow(rowNum);};}(rowCount));
	    		$('#reGeocodeRow'+rowCount).click(function(rowNum) {return function() {reGeocodeRow(rowNum);};}(rowCount));
	    		geocodeRow(rowCount);
	    	}, // end parse step function
	    complete: function() {
	    		toComplete = rowCount;
	    		updateStatus();
	    	} // end parse complete function
	    }); // end parse options/call
	}); // end geocode button onClick function

	$('#geocodeAllButton').click(function() {
		completed = 0;
		toComplete = rowCount;
		$('#resultsTable tbody tr:nth-child(odd)').each(function() {
			geocodeRow($(this).attr('id').substring(3));
		});
	});

}); // end document.ready

/**
 * Remove an element and provide a function that inserts it into its original position
 * @param element {Element} The element to be temporarily removed
 * @return {Function} A function that inserts the element into its original position
 **/
function removeToInsertLater(element) {
  var parent = element.parent();
  var nextSibling = element.next();
  element.detach();
  return function() {
    if (nextSibling) {
      nextSibling.before(element);
    } else {
      parent.append(element);
    }
  };
}

function updateStatus() {
	// only update every ten records or when completed
	if(completed % 10 == 0 || completed == toComplete) {
		var text = '';
		if(completed == toComplete) {
			text = 'Geocoding complete: ';
		} else {
			text = 'Geocoding in progress: ';
		}
		text += '' + completed;
		// toComplete isn't set until after the parsing is complete
		if(toComplete > 0) {
			 text += '/' + toComplete;
		}
		text += ' geocodes are completed and editable.';
		$('#status').text(text);
	}
}

function deleteRow(rowNum) {
	$('#row' + rowNum).remove();
	$('#row' + rowNum + 'r2').remove();
	rowCount--;
	if($('#resultsTable tbody tr').length == 0) {
		restart();
	}
}

function restart() {
	$('#resultsTable tbody tr').remove();
	$('#resultsTable thead tr th.extraCol').remove();
	$('#inputArea').val('');
	rowCount = 0;
	otherFields = [];

	$('.stage1').show();
	$('.stage2').hide();
}

function reGeocodeRow(rowNum) {
	completed--;
	updateStatus();
	geocodeRow(rowNum);
}

function geocodeRow(rowNum, retries) {
	if(!retries) {
		retries = 0;
	}
	$('#row' + rowNum + ' td:nth-child(' + FULL_ADDRESS_COL + ')').html('<img src="img/ajax-loader.gif" title="processing..."/>');

	req = new GeocodeRequest(geocoder_url);
	req.setOutputFormat("jsonp");
	req.setMaxResults(1);
	req.setAddress($('#addressString' + rowNum).val());

	$.ajax({
    	url: req.getURL(),
    	type: "GET",
    	dataType: "jsonp",
    	success: function (response, textStatus, jqXHR) {
    			var row = $('#row' + rowNum);
    			var insertFunction = removeToInsertLater(row);
    			row.removeClass('failed');
    			var feature = response.features[0];
    			$('td:nth-child(' + FULL_ADDRESS_COL + ')', row).text(feature.properties.fullAddress);
    			$('td:nth-child(' + SCORE_COL + ')', row).text(feature.properties.score);
    			$('td:nth-child(' + MATCH_PRECISION_COL + ')', row).text(feature.properties.matchPrecision
    					+ '(' + feature.properties.precisionPoints + ')');
    			$('td:nth-child(' + FAULTS_COL + ')', row).text(faultsToString(feature.properties.faults));
    			$('td:nth-child(' + X_COL + ')', row).text(feature.geometry.coordinates[0]);
    			$('td:nth-child(' + Y_COL + ')', row).text(feature.geometry.coordinates[1]);
    			$('#row' + rowNum + 'r2 td:nth-child(' + FULL_ADDRESS_COL + ')').html('<input type="button" id="showMap' + rowNum + '" value="Show in Map"/>');
    			if(feature.properties.score < SCORE_CUTOFF) {
    				$('td:nth-child(' + SCORE_COL + ')', row).addClass('red');
    				$('td:nth-child(' + FAULTS_COL + ')', row).addClass('red');
    			} else {
    				$('td:nth-child(' + SCORE_COL + ')', row).removeClass('red');
    				$('td:nth-child(' + FAULTS_COL + ')', row).removeClass('red');
    			}
    			insertFunction();
    			$('#showMap'+rowNum).click(function() {showMap(rowNum);});
				completed++;
				updateStatus();
    		},
    	error: function(xhr, err) {
    			errors++;
    			if(retries < 1 && errors < 10) {
    				geocodeRow(rowNum, retries+1);
    			} else {
    				$('#row' + rowNum).addClass('failed');
        			$('#row' + rowNum + ' td:nth-child(' + FULL_ADDRESS_COL + ')').text('Network or server error; please retry.');
    				completed++;
    				updateStatus();
    			}
    		}
    });	// end ajax call
} // end geocodeRow function

function export2csv(csv){
	if($('.csv-data').length) $('.csv-data').remove();
	$('body').append('<div class="csv-data"><form id="the_csv_form" enctype="application/x-www-form-urlencoded" accept-charset="UTF-8" method="POST" action="' + geocoder_url + '/geo/editor/export.csv"><textarea id="csv" class="form" name="csv">'+csv+'</textarea> </input><input type="submit" class="submit" value="Download as file" /></form></div>');
	$('.csv-data').hide();
	$('#the_csv_form').submit();
}

function table2string(delim) {
	var data = [];
	$('#resultsTable thead tr, #resultsTable tbody tr:nth-child(odd)').each(function() {
		var rowData = [];
		var first = true;
		$(this).find("td, th").each(function() {
			// skip the first column, the row number
			if(first) {
				first = false;
				return;
			}
			var input = $(this).find("input").val();
			if(input == undefined) {
				input = $(this).find("textarea").val();
			}
			if(input !== undefined) {
				rowData.push(input);
			} else {
				rowData.push($(this).text());
			}
		});
		data.push(rowData);
	});
	console.log(data);
	if(delim === undefined) {
		delim = delimiter;
	}
	return Papa.unparse(data, {delimiter: delim});
}

function faultsToString(faults) {
	if(faults.length > 0) {
		result = "[";
		for(var i = 0; i < faults.length; i++) {
			result += faults[i].element + '.' + faults[i].fault + ':' + faults[i].penalty + ', ';
		}
		return result.substring(0,result.length-2) + "]";
	}
	return "[]";
}

function showMap(rowNum) {
	window.open('https://maps.google.com/maps?z=11&t=k&q=' 
			+ $('#row' + rowNum + ' td:nth-child(' + Y_COL + ')').text()
			+ "," + $('#row' + rowNum + ' td:nth-child(' + X_COL + ')').text());
}
