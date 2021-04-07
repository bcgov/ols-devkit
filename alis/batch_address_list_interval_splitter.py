# -------------------------------------------------------------------
# -------------------------------------------------------------------

# DESCRIPTION
"""

This script will take a CSV result file from the Batch Geocoder and
produce a series of CSV files based on score intervals of 10.


Instructions:

 1. Start menu -> Run -> Type 'cmd'
 2. Navigate to the folder where this script is located

        python <Script_name>.py <workspace path including final slash>
                                <input filename including file extension>

Example:
        python batch_address_list_interval_splitter.py H:\interval_splitter\
                                                       job-65907-result-1.csv


Assumptions:

- You are providing an unmodified result file from the Batch Geocoder.
- You have both an 'input' and 'output' folder in your workspace

"""
# -------------------------------------------------------------------
# -------------------------------------------------------------------

# IMPORT MODULES

import csv
from datetime import datetime
import pandas as pd
from pandas.api.types import CategoricalDtype
import platform
import sys
import time

# -------------------------------------------------------------------
# -------------------------------------------------------------------

# CHECK ARGUMENTS AND OPERATING SYSTEM

# Check that the correct number of arguments were provided
if len(sys.argv) < 2 or len(sys.argv) > 3:
    print("Missing arguments (2 required). Script will now exit.")
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

# Your workspace filepath (including final slash)
wrkspc = sys.argv[1]

# Input file including file extension
inputFileName = sys.argv[2]

# The output files
outputFileLocation00 = wrkspc + "output\interval-00s_{}.csv".format(
    time.strftime("%Y%m%d-%H%M")
)

outputFileLocation10 = wrkspc + "output\interval-10s_{}.csv".format(
    time.strftime("%Y%m%d-%H%M")
)

outputFileLocation20 = wrkspc + "output\interval-20s_{}.csv".format(
    time.strftime("%Y%m%d-%H%M")
)

outputFileLocation30 = wrkspc + "output\interval-30s_{}.csv".format(
    time.strftime("%Y%m%d-%H%M")
)

outputFileLocation40 = wrkspc + "output\interval-40s_{}.csv".format(
    time.strftime("%Y%m%d-%H%M")
)

outputFileLocation50 = wrkspc + "output\interval-50s_{}.csv".format(
    time.strftime("%Y%m%d-%H%M")
)

outputFileLocation60 = wrkspc + "output\interval-60s_{}.csv".format(
    time.strftime("%Y%m%d-%H%M")
)

outputFileLocation70 = wrkspc + "output\interval-70s_{}.csv".format(
    time.strftime("%Y%m%d-%H%M")
)

outputFileLocation80 = wrkspc + "output\interval-80s_{}.csv".format(
    time.strftime("%Y%m%d-%H%M")
)

outputFileLocation90 = wrkspc + "output\interval-90s_{}.csv".format(
    time.strftime("%Y%m%d-%H%M")
)

# -------------------------------------------------------------------
# -------------------------------------------------------------------

# PROCESSING

# Read the input CSV into a pandas dataframe in chunks
# No field filters are applied to allow for flexible inputs
tpGeocoderResult = pd.read_csv(
    wrkspc + "input" + slash + inputFileName,
    sep=",",
    usecols=[
        "yourId",
        "fullAddress",
        "intersectionName",
        "score",
        "matchPrecision",
        "precisionPoints",
        "faults",
        "siteName",
        "unitDesignator",
        "unitNumber",
        "unitNumberSuffix",
        "civicNumber",
        "civicNumberSuffix",
        "streetName",
        "streetType",
        "isStreetTypePrefix",
        "streetDirection",
        "isStreetDirectionPrefix",
        "streetQualifier",
        "localityName",
        "localityType",
        "electoralArea",
        "provinceCode",
        "location",
        "locationPositionalAccuracy",
        "locationDescriptor",
        "siteID",
        "blockID",
        "intersectionID",
        "fullSiteDescriptor",
        "accessNotes",
        "siteStatus",
        "siteRetireDate",
        "changeDate",
        "isOfficial",
        "degree",
        "executionTime",
        "sid",
    ],
    dtype={
        "yourId": "object",
        "fullAddress": "object",
        "intersectionName": "object",
        "score": "int8",
        "matchPrecision": "category",
        "precisionPoints": "int8",
        "faults": "object",
        "siteName": "object",
        "unitDesignator": "category",
        "unitNumber": "object",
        "unitNumberSuffix": "object",
        "civicNumber": "object",
        "civicNumberSuffix": "object",
        "streetName": "object",
        "streetType": "category",
        "isStreetTypePrefix": "bool",
        "streetDirection": "category",
        "isStreetDirectionPrefix": "bool",
        "streetQualifier": "object",
        "localityName": "category",
        "localityType": "category",
        "electoralArea": "category",
        "provinceCode": "category",
        "location": "object",
        "locationPositionalAccuracy": "category",
        "locationDescriptor": "category",
        "accessNotes": "object",
        "siteStatus": "category",
        "siteRetireDate": "object",
        "changeDate": "object",
        "isOfficial": "bool",
        "degree": "object",
        "executionTime": "float16",
        "sid": "object",
    },
    iterator=True,
    error_bad_lines=False,
    index_col=False,
    chunksize=1000,
)

dfGeocoderResult = pd.concat(list(tpGeocoderResult), ignore_index=True)

del tpGeocoderResult

# write only rows to csv where the vector is true:
df00 = dfGeocoderResult[dfGeocoderResult['score'] < 10]
df00.to_csv(outputFileLocation00, index=False, chunksize=1000)
del df00

df10 = dfGeocoderResult[(dfGeocoderResult.score >= 10) &
                        (dfGeocoderResult.score < 20)]
df10.to_csv(outputFileLocation10, index=False, chunksize=1000)
del df10

df20 = dfGeocoderResult[(dfGeocoderResult.score >= 20) &
                        (dfGeocoderResult.score < 30)]
df20.to_csv(outputFileLocation20, index=False, chunksize=1000)
del df20

df30 = dfGeocoderResult[(dfGeocoderResult.score >= 30) &
                        (dfGeocoderResult.score < 40)]
df30.to_csv(outputFileLocation30, index=False, chunksize=1000)
del df30

df40 = dfGeocoderResult[(dfGeocoderResult.score >= 40) &
                        (dfGeocoderResult.score < 50)]
df40.to_csv(outputFileLocation40, index=False, chunksize=1000)
del df40

df50 = dfGeocoderResult[(dfGeocoderResult.score >= 50) &
                        (dfGeocoderResult.score < 60)]
df50.to_csv(outputFileLocation50, index=False, chunksize=1000)
del df50

df60 = dfGeocoderResult[(dfGeocoderResult.score >= 60) &
                        (dfGeocoderResult.score < 70)]
df60.to_csv(outputFileLocation60, index=False, chunksize=1000)
del df60

df70 = dfGeocoderResult[(dfGeocoderResult.score >= 70) &
                        (dfGeocoderResult.score < 80)]
df70.to_csv(outputFileLocation70, index=False, chunksize=1000)
del df70

df80 = dfGeocoderResult[(dfGeocoderResult.score >= 80) &
                        (dfGeocoderResult.score < 90)]
df80.to_csv(outputFileLocation80, index=False, chunksize=1000)
del df80

df90 = dfGeocoderResult[dfGeocoderResult['score'] >= 90]
df90.to_csv(outputFileLocation90, index=False, chunksize=1000)
del df90
