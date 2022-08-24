#!/usr/bin/env python3.9

# DESCRIPTION
"""
This script will create a new Geomark using the contents of the clipboard.
The resulting Geomark info page URL will print to screen.

For more information including links to the developer guide, glossary of terms
and tutorials please visit the Geomark homepage.
https://www2.gov.bc.ca/gov/content?id=F6BAF45131954020BCFD2EBCC456F084

Instructions:
1. Start menu -> Run -> Type 'cmd'
2. Navigate to the folder where this script is located
        python <Script_name>.py <fileFormat> <geometryType>
               <multiple> <srid> <body>

Example:
python createGeomarkFromClipboard.py
       wkt Polygon false 4326
       "SRID=4326;POLYGON((-123.376287 48.463696,-123.375529 48.463119,
       -123.37476 48.462977,-123.373887 48.462343,-123.373281 48.46212,
       -123.372183 48.462174,-123.370455 48.462674,-123.369631 48.463367,
       -123.369848 48.463731,-123.371008 48.464257,-123.372742 48.464411,
       -123.375033 48.464121,-123.376108 48.464232,-123.376452 48.464071,
       -123.376287 48.463696))"

Note:
To modify the number of input arguments, see the 'fields' dictionary below.
"""
# -------------------------------------------------------------------
# -------------------------------------------------------------------

# IMPORT MODULES

import json
import requests
import sys

# -------------------------------------------------------------------
# -------------------------------------------------------------------

# CHECK ARGUMENTS

# Check that the correct number of arguments were provided
if len(sys.argv) < 5 or len(sys.argv) > 6:
    print("Missing arguments (5 required). Script will now exit.")
    exit()

# -------------------------------------------------------------------
# -------------------------------------------------------------------

# VARIABLE DEFINITIONS

# Geomark request URL.
geomarkEnv = "https://apps.gov.bc.ca/pub/geomark/geomarks/new"

# Uncomment to submit requests to the test environment
# geomarkEnv = 'https://test.apps.gov.bc.ca/pub/geomark/geomarks/new'

headers = {"Accept": "*/*"}

# Options include KML, geoJSON, GML or WKT
fileFormat = sys.argv[1]

# Options include Polygon, LineString, Point, Any
geometryType = sys.argv[2]

# Indicate if multiple geometries are to be used for the geomark
# Options include 'true' or 'false'
multiple = sys.argv[3]

# Coordinate system spatial reference identifier
srid = sys.argv[4]

# The clipboard contents (KML, geoJSON, GML or WKT)
body = sys.argv[5]

# Geomark Web Service request parameter values

fields = {
    "allowOverlap": "false",
    "body": body,
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
    geomarkRequest = requests.post(geomarkEnv, headers=headers, data=fields)
    geomarkResponse = (
        str(geomarkRequest.text).replace("(", "").
        replace(")", "").replace(";", ""))
    data = json.loads(geomarkResponse)
    geomarkInfoPage = data["url"]
except (NameError, TypeError, KeyError, ValueError) as error:
    print("*****************************************************************")
    print("Error processing Geomark request")
    print(data["error"])
    print("*****************************************************************")
    exit()

print("\nGeomark info page URL:\n" + geomarkInfoPage)
