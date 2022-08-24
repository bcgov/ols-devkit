#!/usr/bin/env python3.9

# DESCRIPTION
"""
This script will accept an input file and submit a request to the
Geomark Web Services to create a new Geomark. The resulting Geomark info
page URL will print to screen.

For more information including links to the developer guide, glossary of terms
and tutorials please visit the Geomark homepage.
https://www2.gov.bc.ca/gov/content?id=F6BAF45131954020BCFD2EBCC456F084

Instructions:
1. Start menu -> Run -> Type 'cmd'
2. Navigate to the folder where this script is located
        python <Script_name>.py <path to inputfile> <geometryType> <multiple>
               <srid>


Example:
python createGeomarkFromFile.py H:\proj2\StudyArea.kml Polygon true 4326

Note:
To modify the number of input arguments, see the 'fields' dictionary below.
"""
# -------------------------------------------------------------------
# -------------------------------------------------------------------

# IMPORT MODULES

import json
import pathlib
import requests
import sys

# -------------------------------------------------------------------
# -------------------------------------------------------------------

# CHECK ARGUMENTS

# Check that the correct number of arguments were provided
if len(sys.argv) < 4 or len(sys.argv) > 5:
    print("Missing arguments (4 required). Script will now exit.")
    exit()

# -------------------------------------------------------------------
# -------------------------------------------------------------------


# VARIABLE DEFINITIONS

# Geomark request URL.
geomarkEnv = "https://apps.gov.bc.ca/pub/geomark/geomarks/new"

# Uncomment to submit requests to the test environment
# geomarkEnv = 'https://test.apps.gov.bc.ca/pub/geomark/geomarks/new'

headers = {"Accept": "*/*"}

# Input file name (full path including file extension)
filename = sys.argv[1]

# Input file geometries will be submitted in the body of a POST request
files = {"body": open(filename, "rb")}

# The file format of the input file
fileFormat = (pathlib.Path(filename).suffix).replace(".", "")

# Geometry type options 'Point', 'Line', 'Polygon' or 'Any'
geometryType = sys.argv[2]

# Indicate if multiple geometries are to be used for the geomark
# Options include 'true' or 'false'
multiple = sys.argv[3]

# Coordinate system spatial reference identifier
srid = sys.argv[4]

# Geomark Web Service request parameter values

fields = {
    "allowOverlap": "false",
    "bufferCap": "ROUND",
    "bufferJoin": "ROUND",
    "bufferMetres": "",
    "bufferMitreLimit": "5",
    "bufferSegments": "8",
    "callback": "",
    "failureRedirectUrl": "",
    "format": fileFormat,
    "geometryType": geometryType,
    "multiple": multiple,
    "redirectUrl": "",
    "resultFormat": "json",
    "srid": srid
}


# -------------------------------------------------------------------
# -------------------------------------------------------------------

# PROCESSING

print("Sending request to:\n" + geomarkEnv)

# Submit request to the Geomark Web Service and parse response

try:
    geomarkRequest = requests.post(
        geomarkEnv, files=files, headers=headers, data=fields)
    geomarkResponse = (
        str(geomarkRequest.text).replace("(", "").
        replace(")", "").replace(";", ""))
    data = json.loads(geomarkResponse)
    geomarkInfoPage = data["url"]
except (NameError, TypeError, KeyError, ValueError) as error:
    print("*****************************************************************")
    print("Error processing Geomark request for " + filename)
    print(data["error"])
    print("*****************************************************************")
    exit()

print("\nGeomark info page URL:\n" + geomarkInfoPage)
