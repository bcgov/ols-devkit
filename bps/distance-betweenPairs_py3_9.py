#!/usr/bin/env python3.9

# DESCRIPTION
"""
This script will iterate through a CSV file containing
two sets of coordinates. Each iteration will submit a request
to the BC Route Planner. Specifically, it will use betweenPairs
to compute the distance between a list of fromPoints and toPoints
in the csv. The number of fromPoints times the number of toPoints 
should not exceed 100 or the request will time out.

The script then partitions the response and saves these 
distances to a new CSV file.

Note:
If creating a CSV file in MS Excel you may need to select
the 'CSV UTF-8' file type.

Instructions:

1. Start menu -> Run -> Type 'cmd'
2. Navigate to the folder where this script is located

        python <Script_name>.py <your API key>
                                <workspace filepath including final slash>
                                <input filename including file extension>
                                <criteria> - written as 'shortest' or 'fastest'
                                <mode> - written as 'table'

Example:
        python distance-betweenPairs_py3_9.py
        <your API key>
        H:\scripts\bps\ location_list.csv
        shortest table

Assumptions:

- You have both an 'input' and 'output' folder in the same folder
  as the python script
- The location names are stored in fields spelled 'Location1' and 'Location2'.
- The coordinates are stored in separate fields spelled 'Latitude1',
  'Longitude1','Latitude2','Longitude2'. All other fields are ignored.
"""
# -------------------------------------------------------------------
# -------------------------------------------------------------------

# IMPORT MODULES

import csv
import datetime
import json
import numpy as np
import os
import pandas as pd
import platform
import requests
import sys
import urllib.error
import urllib.request
import urllib.parse

# -------------------------------------------------------------------
# -------------------------------------------------------------------

# CHECK ARGUMENTS AND OPERATING SYSTEM

# Check that the correct number of arguments were provided
if len(sys.argv) < 5 or len(sys.argv) > 6:
    print("Missing arguments (5 required). Script will now exit.")
    exit()

# Determine if script is running on Windows or Linux
pltFrm = platform.system()

# Define which slash to use based on platform
if pltFrm == "Windows":
    slash = "\\"
elif pltFrm == "Linux":
    slash = "/"

# -------------------------------------------------------------------
# -------------------------------------------------------------------

# VARIABLE DEFINITIONS

# The API key.
apiKey = sys.argv[1]

# Your workspace filepath (including final slash)
wrkspc = sys.argv[2]

# The input filename (including file type extension)
inputFileName = sys.argv[3]

# Criteria determines if the BC Route Planner will compute
# the shortest route or the fastest route.
criteria = sys.argv[4]

# Mode controls which type of report is output from the script.
mode = sys.argv[5]

inputFile = wrkspc + "input" + slash + inputFileName

# The location and filename for the output of this script.
outputReportFile = (
    wrkspc
    + "output"
    + slash
    + "reportTable_"
    + datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
    + ".csv"
)

# Variables used to assemble a web request
routePlanner_apikey = "&apikey={}".format(apiKey)
headers = {"apikey": routePlanner_apikey}

# The web request sent to the BC Route Planner using urllib
web_request = ""

# used to capture JSON response from the web request.
json_response = ""

# A list to hold the coordinates of all locations in the csv.
coord_list_1 = []
coord_list_2 = []

# A list to hold the names of all locations in the csv.
locationName_1 = []
locationName_2 = []

# A dictionary holding both the location names and coordinates
locationDict_1 = dict()

# A dictionary holding both the location names and position
# number in the list of all locations
nameDict = dict()

# A dictonary to hold the 'pairs' response from the route planner
# specifically the 'to' and 'distance' values.
pairsDict = dict()

# -------------------------------------------------------------------
# -------------------------------------------------------------------

# FUNCTIONS


def parse_response(data_response):
    # Parse the response from the BC Route Planner (betweenPairs)
    # print("parsing response from the BC Route Planner")
    try:
        FRM = data_response["fromPoints"]
        FRM = str(FRM).replace("[[", "").replace("]]", "").replace(" ", "")
    except KeyError:
        FRM = "Missing"
        print("fromPoints missing from response. Ending script")
        exit()
    try:
        TO = data_response["toPoints"]
        TO = (
            str(TO)
            .replace("[[", "")
            .replace("]]", "")
            .replace(" ", "")
            .replace("[", "")
            .replace("]", "")
        )
    except KeyError:
        TO = "Missing"
        print("toPoints missing from response. Ending script")
        exit()
    try:
        PAIRS = data_response["pairs"]
    except KeyError:
        PAIRS = "Missing"
        print("pairs missing from response. Ending script")
        exit()
    return FRM, TO, PAIRS


def parse_pairs(allPairs, rows, CRT, idx):
    # Empty the pairs dictionary for the next iteration
    pairsDict.clear()
    toTemp = ""
    distTemp = ""
    loopCount = 0
    for i in range(0, len(allPairs)):
        if i != idx:
            distTemp = allPairs[i]["distance"]
            toTemp = allPairs[i]["to"]
            pairsDict[toTemp] = distTemp
        else:
            pairsDict[idx] = "0"
    return pairsDict


# -------------------------------------------------------------------
# -------------------------------------------------------------------

# PROCESSING

# --------------------------------------------------------
# Step 1: read in the CSV file
# --------------------------------------------------------
print("\nReading the csv file into a dataframe")
dfInputFileContent = pd.read_csv(inputFile)

# Count how many rows and columns are in the input file
rows, columns = dfInputFileContent.shape
print(("This file has " + str(rows) + " rows"))


# --------------------------------------------------------
# Step 2: Assign fields from the dataframe to lists
# --------------------------------------------------------

print("Populating lists")

# The list of location names from the dataframe
locationName_1 = dfInputFileContent["fromPoints"].values.tolist()
locationName_2 = dfInputFileContent["toPoints"].values.tolist()

# Join the separate longitude and latitude fields
coordinates_1 = (
    dfInputFileContent["Longitude1"].astype(str)
    + ","
    + dfInputFileContent["Latitude1"].astype(str)
)

coordinates_2 = (
    dfInputFileContent["Longitude2"].astype(str)
    + ","
    + dfInputFileContent["Latitude2"].astype(str)
)

# Convert to a list
coord_list_1 = coordinates_1.values.tolist()
coord_list_2 = coordinates_2.values.tolist()

# --------------------------------------------------------
# Step 3: Make a dictionary of locations and coordinates
# --------------------------------------------------------

print("Populating dictionary")

# Populate values into the dictionary
locationDict_1 = dict(list(zip(locationName_1, coord_list_1)))
locationDict_2 = dict(list(zip(locationName_2, coord_list_2)))

# --------------------------------------------------------
# Step 4: Processing
# --------------------------------------------------------

toPoint = ""
loopCounter1 = 0
print("Processing. Please wait")
print("Sending web requests and parsing response")

for fromKey in locationName_1:
    fromPoint = locationDict_1[fromKey]
    if pd.isnull(fromKey) is True:
        print("End of coordinate list. Processing complete")
        exit()
    ctName = fromKey
    nameCounter = 0
    for toKey in locationName_2:
        if toKey != fromKey:
            if toPoint == "":
                toPoint = locationDict_2[toKey]
                toNames = toKey
                nameDict[nameCounter] = toKey
                nameCounter += 1
            else:
                toPoint = toPoint + "," + locationDict_2[toKey]
                toNames = toNames + "," + toKey
                nameDict[nameCounter] = toKey
                nameCounter += 1
    # Route planner URLs
    routePlanner = (
        "https://router.api.gov.bc.ca/distance/"
        "betweenPairs.json?"
        "fromPoints={}&toPoints={}"
        "&criteria={}".format(fromPoint.strip(),
                              toPoint.strip(),
                              criteria.strip())
    )
    r = requests.post(routePlanner, data=json.dumps(routePlanner),
                      headers=headers)
    web_request = urllib.request.urlopen(routePlanner +
                                         routePlanner_apikey.strip())
    json_response = web_request.read()
    # Load the JSON response for parsing
    data = json.loads(json_response)
    # Call function to parse the test RP response
    fromPT, toPTS, pairs = parse_response(data)
    toPoint = ""
    if mode == "table":
        index = rows + 1000
        pairsDict = parse_pairs(pairs, rows, ctName, index)
    # Find the position number of the current location name in the
    # list of all location names.
    index = locationName_1.index(ctName)
    index = index + 1

    if mode == "table":
        # Create a tabular report file
        with open(outputReportFile, "a") as b:
            if loopCounter1 == 0:
                b.write(
                    ""
                    + ","
                    + str(list
                          (nameDict.values())).strip("[]").replace("'", "")
                    + "\n"
                )
            b.write(
                ctName
                + ","
                + str(list(pairsDict.values()))
                .strip("[]")
                .replace("u", "")
                .replace("'", "")
                + "\n"
            )
            loopCounter1 += 1

print("Report generation complete")
