#!/usr/bin/env python3.9

# DESCRIPTION
"""
This script will accept a list of one or more existing Geomark URLs
(from the same environment) to create a new single Geomark.
The resulting Geomark info page URL will print to screen.

For more information including links to the developer guide, glossary of terms
and tutorials please visit the Geomark homepage.
https://www2.gov.bc.ca/gov/content?id=F6BAF45131954020BCFD2EBCC456F084

Instructions:
1. Start menu -> Run -> Type 'cmd'
2. Navigate to the folder where this script is located
        python <Script_name>.py <geomarkUrl> <allowOverlap>

Example:
python createGeomarkFromGeomarks.py
       gm-abcdefghijklmnopqrstuv0bcislands,gm-abcdefghijklmnopqrstuvwxyz0000bc
       true

Assumptions:
    1. If copying multiple geomarks then all the geomarks have the same
    geometry type (Point, LineString or Polygon). Unless a buffer is specified,
    in which case all geometries will be converted to polygons.

    2. The geomarkUrl parameter can accept either a comma separated list
    of full Geomark URLs OR Geomark IDs.

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
if len(sys.argv) < 2 or len(sys.argv) > 3:
    print("Missing arguments (2 required). Script will now exit.")
    exit()

# -------------------------------------------------------------------
# -------------------------------------------------------------------

# VARIABLE DEFINITIONS

# Geomark request URL.
geomarkEnv = "https://apps.gov.bc.ca/pub/geomark/geomarks/copy"

# Uncomment to submit requests to the test environment
# geomarkEnv = 'https://test.apps.gov.bc.ca/pub/geomark/geomarks/new'

headers = {"Accept": "*/*"}

# A comma separated list of one or more Geomark URLs or Geomark IDs
geomarkUrl = sys.argv[1]

# Count the number of Geomarks provided
geomarkUrlCt = geomarkUrl.count(",")

# List containing values from 'geomarkUrl'.
geomarkUrlList = []

# When multiple=true select this option to allow overlapping geometries
# Options are true / false
allowOverlap = sys.argv[2]

# Geomark Web Service request parameter values

fields = {
    "allowOverlap": allowOverlap,
    "bufferCap": "ROUND",
    "bufferJoin": "ROUND",
    "bufferMetres": "",
    "bufferMitreLimit": "5",
    "bufferSegments": "8",
    "callback": "",
    "failureRedirectUrl": "",
    "redirectUrl": "",
    "resultFormat": "json"
}


# -------------------------------------------------------------------
# -------------------------------------------------------------------

# PROCESSING


if geomarkUrlCt >= 1:
    geomarkUrlList = geomarkUrl.split(",")
    print("Creating a new Geomark from multiple input Geomarks.\n")
elif geomarkUrlCt == 0:
    geomarkUrlList.append(geomarkUrl)
    print("Creating a new Geomark from a single input Geomark.\n")

fields["geomarkUrl"] = geomarkUrlList

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
