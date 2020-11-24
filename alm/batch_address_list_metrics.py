# -------------------------------------------------------------------
# -------------------------------------------------------------------

# DESCRIPTION
"""


This script will take a result file from the Batch Geocoder and produce
several CSV files containing summary stats on the input file along with
a single md file.


Instructions:

 1. Start menu -> Run -> Type 'cmd'
 2. Navigate to the folder where this script is located

        python <Script_name>.py <workspace filepath including final slash>
                                <input filename including file extension>

Example:
        python batch_addres_list_metrics.py
        H:\Metrics\ job-69005-result-1.csv

Assumptions:

- You are providing an unmodified result file from the Batch Geocoder.
- You have both an 'input' and 'output' folder in the same folder
  as the python script

"""
# -------------------------------------------------------------------
# -------------------------------------------------------------------

# IMPORT MODULES

import csv
from datetime import datetime
import numpy
import pandas as pd
from pandas.api.types import CategoricalDtype
import platform
import os
import re
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

# Input csv file including extension
inputFileName = sys.argv[2]

# The current time
timeStp = time.strftime("%Y%m%d-%H%M%S")

# The output file will always be called 'results_<Today's date>
outputFileLocation = wrkspc + "output" + slash + \
                     "results_{}.md".format(timeStp)

# Output CSV files
outputFaultCounts = wrkspc + "output" + slash + \
                    "fault_counts_{}.csv".format(timeStp)

outputMatchPrecisionCounts = (
    wrkspc + "output" + slash + "match_precision_counts_{}.csv".format(timeStp)
)

outputFaultComboCounts = (
    wrkspc + "output" + slash + "fault_combo_counts_{}.csv".format(timeStp)
)

outputFaultWithHighScoreCounts = (
    wrkspc + "output" + slash +
    "fault_with_high_score_counts_{}.csv".format(timeStp)
)

outputSummaryStats = wrkspc + "output" + slash + \
                     "summary_stats_{}.csv".format(timeStp)

outputScoreHistogram = (
    wrkspc + "output" + slash + "score_histogram_{}.csv".format(timeStp)
)

# Create a list containing all faults
faultTypeList = [
    "CIVIC_NUMBER.missing",
    "CIVIC_NUMBER.notInAnyBlock",
    "CIVIC_NUMBER_SUFFIX.notMatched",
    "LOCALITY.isAlias",
    "LOCALITY.missing",
    "LOCALITY.notMatched",
    "LOCALITY.spelledWrong",
    "MAX_RESULTS.too_low_to_include_all_best_matches",
    "POSTAL_ADDRESS_ELEMENT.notAllowed",
    "PROVINCE.missing",
    "PROVINCE.notMatched",
    "SITE_NAME.missing",
    "SITE_NAME.notMatched",
    "SITE_NAME.partiallyMatched",
    "SITE_NAME.spelledWrong",
    "STREET.missing",
    "STREET.qualifierMissing",
    "STREET.qualifierNotMatched",
    "STREET.qualifierSpelledWrong",
    "STREET_NAME.notMatched",
    "STREET_NAME.isAlias",
    "STREET_NAME.missing",
    "STREET_NAME.spelledWrong",
    "STREET_TYPE.missing",
    "STREET_TYPE.notMatched",
    "STREET_TYPE.notPrefix",
    "STREET_TYPE.notSuffix",
    "STREET_TYPE.spelledWrong",
    "STREET_DIRECTION.missing",
    "STREET_DIRECTION.notMatched",
    "STREET_DIRECTION.notPrefix",
    "STREET_DIRECTION.notSuffix",
    "STREET_DIRECTION.spelledWrong",
    "UNRECOGNIZED.notAllowed",
    "UNIT_DESIGNATOR.isAlias",
    "UNIT_DESIGNATOR.missing",
    "UNIT_DESIGNATOR.spelledWrong",
    "UNIT_NUMBER.missing",
    "UNIT_NUMBER.notMatched",
    "UNIT_NUMBER.suffixMissing",
    "UNIT_NUMBER.suffixNotMatched",
]

# Create a list containing all matchPrecision levels
listMatchPrecision = [
    "OCCUPANT",
    "SITE",
    "SITE_STREET",
    "SITE_LOCALITY",
    "UNIT",
    "CIVIC_NUMBER",
    "INTERSECTION",
    "BLOCK",
    "STREET",
    "LOCALITY",
    "PROVINCE",
]

# Integer variables used in summary stats
ave_score = 0.0
min_score = max_score = 0
ave_execTime = 0.0
min_execTime = max_execTime = 0.0

# Histogram bins
bin1 = bin2 = bin3 = bin4 = bin5 = bin6 = bin7 = bin8 = bin9 = bin10 = 0

# Counters for faults
noFaults = oneFaults = twoFaults = threeFaults = fourFaults = 0
fiveFaults = aboveFiveFaults = 0
threeFaultsAndHighScore = fourFaultsAndHighScore = 0
fiveFaultsAndHighScore = aboveFiveFaultsAndHighScore = 0

# -------------------------------------------------------------------
# -------------------------------------------------------------------
# FUNCTIONS


def readData(inputFileName, column, dataType):
    # Read the csv file in chunks
    tpGeocoderResult = pd.read_csv(
        wrkspc + "input" + slash + inputFileName,
        usecols=[column],
        dtype={column: dataType},
        iterator=True,
        error_bad_lines=False,
        index_col=False,
        chunksize=1000,
    )
    dfGeocoderResult = pd.concat(list(tpGeocoderResult), ignore_index=True)
    del tpGeocoderResult
    # Create lists from the dataframe
    listX = dfGeocoderResult[column].tolist()
    # Delete the dataframe
    del dfGeocoderResult

    return listX


def frequencyCount(categoryList, dataList, file1, file2):
    # Count occurances of category values in a list
    for item in categoryList:
        file1.write("|" + item + "|")
        file2.write(item + ",")
        file1.write(str(sum(item in s for s in dataList)) + "\n")
        file2.write(str(sum(item in s for s in dataList)) + "\n")


def listMetrics(listN):
    # Compute the average, minimum and maximum value in a list
    avg_val = numpy.average(listN)
    min_val = numpy.min(listN)
    max_val = numpy.max(listN)

    return avg_val, min_val, max_val


def histTable(scoreList):
    # Create a histogram of score values
    histArray = numpy.histogram(
        scoreList, bins=[0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
    )
    # Calculate a histogram of scores
    b1 = b2 = b3 = b4 = b5 = b6 = b7 = b8 = b9 = b10 = 0
    b1 = histArray[0][0]
    b2 = histArray[0][1]
    b3 = histArray[0][2]
    b4 = histArray[0][3]
    b5 = histArray[0][4]
    b6 = histArray[0][5]
    b7 = histArray[0][6]
    b8 = histArray[0][7]
    b9 = histArray[0][8]
    b10 = histArray[0][9]
    return b1, b2, b3, b4, b5, b6, b7, b8, b9, b10


def faultCountAndHighScore(faultList, scoreList):
    # Count the number of times a result has multiple
    # faults (0, 1, 2, etc).
    ct = noF = oneF = twoF = threeF = fourF = fiveF = 0
    aboveFiveF = threeFaultsAndHS = fourFaultsAndHS = 0
    fiveFaultsAndHS = aboveFiveFaultsAndHS = 0
    for i in faultList:
        if faultList[ct].count(":") == 0:
            noF += 1
        elif faultList[ct].count(":") == 1:
            oneF += 1
        elif faultList[ct].count(":") == 2:
            twoF += 1
        elif faultList[ct].count(":") == 3:
            threeF += 1
            if (scoreList[ct]) >= 90:
                threeFaultsAndHS += 1
        elif faultList[ct].count(":") == 4:
            fourF += 1
            if (scoreList[ct]) >= 90:
                fourFaultsAndHS += 1
        elif faultList[ct].count(":") == 5:
            fiveF += 1
            if (scoreList[ct]) >= 90:
                fiveFaultsAndHS += 1
        elif faultList[ct].count(":") > 5:
            aboveFiveF += 1
            if (scoreList[ct]) >= 90:
                aboveFiveFaultsAndHS += 1
        ct += 1

    return (
        noF,
        oneF,
        twoF,
        threeF,
        fourF,
        fiveF,
        aboveFiveF,
        threeFaultsAndHS,
        fourFaultsAndHS,
        fiveFaultsAndHS,
        aboveFiveFaultsAndHS,
    )


# -------------------------------------------------------------------
# -------------------------------------------------------------------
# PROCESSING

# Open the md file in append mode
output = open(outputFileLocation, "a")

# Populate a list with the faults column
faultList = readData(inputFileName, "faults", "object")

# Acquire a row count for the input file
rowCount = len(faultList)

output.write("# Geocoded Address List Statistics\n\n")
output.write("## Address Count: " + str(rowCount) + "\n\n")
output.write("## Fault Counts\n")
output.write("|Fault Name|Count\n")
output.write("|----|----:\n")

outputFC = open(outputFaultCounts, "a")
outputFC.write("Fault,Count\n")

# Count the number of times each fault occurs
# in the geocoder result file
frequencyCount(faultTypeList, faultList, output, outputFC)

outputFC.close()

output.write("\n")
output.write("## matchPrecision counts\n")
output.write("|matchPrecision|Count\n")
output.write("|-----|-----:\n")

outputMPC = open(outputMatchPrecisionCounts, "a")
outputMPC.write("matchPrecision,Count\n")

# Populate a list with the matchPrecision column
matchPrecisionList = readData(inputFileName, "matchPrecision", "category")

# Count the number of times each matchPrecision level occurs
# in the geocoder result file
frequencyCount(listMatchPrecision, matchPrecisionList, output, outputMPC)

# Delete the matchPrecision list from memory
del matchPrecisionList

outputMPC.close()

# Populate a list with the score column
scoreList = readData(inputFileName, "score", "int8")

# Count the number of records with a score within each range
bin1, bin2, bin3, bin4, bin5, bin6, bin7, bin8, bin9, bin10 = \
      histTable(scoreList)

# Calculate metrics for the scoreList
ave_score, min_score, max_score = listMetrics(scoreList)

(
    noFaults,
    oneFaults,
    twoFaults,
    threeFaults,
    fourFaults,
    fiveFaults,
    aboveFiveFaults,
    threeFaultsAndHighScore,
    fourFaultsAndHighScore,
    fiveFaultsAndHighScore,
    aboveFiveFaultsAndHighScore,
) = faultCountAndHighScore(faultList, scoreList)

# Delete the score and fault lists from memory
del scoreList
del faultList

executionTimeList = readData(inputFileName, "executionTime", "float")

# Calculate stats for 'executionTime'
ave_execTime, min_execTime, max_execTime = listMetrics(executionTimeList)

# Delete the executionTime list from memory
del executionTimeList

# Write the results to file

output.write("\n## Fault combination counts\n")
output.write("|Number of faults in fault list|Count\n")
output.write("|-----|-----:" + "\n")

outputFCC = open(outputFaultComboCounts, "a")
outputFCC.write("Number of faults in fault list,Count\n")

faultRowCount = (
    noFaults
    + oneFaults
    + twoFaults
    + threeFaults
    + fourFaults
    + fiveFaults
    + aboveFiveFaults
)

output.write("|0|" + str(noFaults) + "\n")
output.write("|1|" + str(oneFaults) + "\n")
output.write("|2|" + str(twoFaults) + "\n")
output.write("|3|" + str(threeFaults) + "\n")
output.write("|4|" + str(fourFaults) + "\n")
output.write("|5|" + str(fiveFaults) + "\n")
output.write("|6 or more|" + str(aboveFiveFaults) + "\n\n")

outputFCC.write("0," + str(noFaults) + "\n")
outputFCC.write("1," + str(oneFaults) + "\n")
outputFCC.write("2," + str(twoFaults) + "\n")
outputFCC.write("3," + str(threeFaults) + "\n")
outputFCC.write("4," + str(fourFaults) + "\n")
outputFCC.write("5," + str(fiveFaults) + "\n")
outputFCC.write("6 or more," + str(aboveFiveFaults) + "\n")

outputFCC.close()

output.write("## Multiple Faults with score of 90 or above\n")
output.write("|Number of faults in fault list|Count\n")
output.write("|---|---:\n")

outputFHS = open(outputFaultWithHighScoreCounts, "a")
outputFHS.write("Number of faults in fault list,Count\n")

output.write("|3|" + str(threeFaultsAndHighScore) + "\n")
output.write("|4|" + str(fourFaultsAndHighScore) + "\n")
output.write("|5|" + str(fiveFaultsAndHighScore) + "\n")
output.write("|6 or more|" + str(aboveFiveFaultsAndHighScore) + "\n\n")

outputFHS.write("3," + str(threeFaultsAndHighScore) + "\n")
outputFHS.write("4," + str(fourFaultsAndHighScore) + "\n")
outputFHS.write("5," + str(fiveFaultsAndHighScore) + "\n")
outputFHS.write("6 or more," + str(aboveFiveFaultsAndHighScore) + "\n")

outputFHS.close()

output.write("\n")

addressMatchAccuracy = "{:.2%}".format(float(bin10) / float(rowCount))

output.write("## Summary Stats\n")
output.write("|Measure|Count\n")
output.write("|---|---:\n")

outputSS = open(outputSummaryStats, "a")
outputSS.write("Measure, Count\n")

output.write("|Average score|" + str(round(ave_score, 2)) + "\n")
output.write("|Minimum score|" + str(min_score) + "\n")
output.write("|Maximum score|" + str(max_score) + "\n")
output.write("|Average executionTime|" + str(round(ave_execTime, 2)) + "\n")
output.write("|Minimum executionTime|" + str(min_execTime) + "\n")
output.write("|Maximum executionTime|" + str(max_execTime) + "\n")
output.write("|Total number of addresses|" + str(rowCount) + "\n")
output.write("|Address match accuracy|" + str(addressMatchAccuracy) + "\n\n")

outputSS.write("Average score," + str(round(ave_score, 2)) + "\n")
outputSS.write("Minimum score," + str(min_score) + "\n")
outputSS.write("Maximum score," + str(max_score) + "\n")
outputSS.write("Average executionTime," + str(round(ave_execTime, 2)) + "\n")
outputSS.write("Minimum executionTime," + str(min_execTime) + "\n")
outputSS.write("Maximum executionTime," + str(max_execTime) + "\n")
outputSS.write("Total number of addresses," + str(rowCount) + "\n")
outputSS.write("Address match accuracy," + str(addressMatchAccuracy) + "\n\n")

outputSS.close()

output.write("## Scores Histogram\n")
output.write("|Interval|Count\n")
output.write("|---|---:\n")

output.write("|0 - 9|" + str(bin1) + "\n")
output.write("|10 - 19|" + str(bin2) + "\n")
output.write("|20 - 29|" + str(bin3) + "\n")
output.write("|30 - 39|" + str(bin4) + "\n")
output.write("|40 - 49|" + str(bin5) + "\n")
output.write("|50 - 59|" + str(bin6) + "\n")
output.write("|60 - 69|" + str(bin7) + "\n")
output.write("|70 - 79|" + str(bin8) + "\n")
output.write("|80 - 89|" + str(bin9) + "\n")
output.write("|90 - 100|" + str(bin10) + "\n")

output.close()

outputSH = open(outputScoreHistogram, "a")
outputSH.write("timeStamp,inputFile,0s,10s,20s,30s,40s,50s,60s,70s,80s,90s"
               + "\n")
outputSH.write(
    timeStp
    + ","
    + inputFileName
    + ","
    + str(bin1)
    + ","
    + str(bin2)
    + ","
    + str(bin3)
    + ","
    + str(bin4)
    + ","
    + str(bin5)
    + ","
    + str(bin6)
    + ","
    + str(bin7)
    + ","
    + str(bin8)
    + ","
    + str(bin9)
    + ","
    + str(bin10)
    + "\n"
)

outputSH.close()
