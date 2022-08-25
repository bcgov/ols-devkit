#!/usr/bin/env python3.9

# DESCRIPTION
"""
This script will accept an input folder path and convert all
spatial file types found in the allowedFileTypes list to
individual Geomarks. The resulting Geomark info page URLs will
print to screen and be saved to a CSV file.

For more information including links to the developer guide,
glossary of terms, api specification and tutorials please
visit the Geomark homepage.
https://www2.gov.bc.ca/gov/content?id=F6BAF45131954020BCFD2EBCC456F084

Instructions:
1. Start menu -> Run -> Type 'cmd'
2. Navigate to the folder where this script is located
        python <Script_name>.py <path to folder>

Example:
        python createGeomarksFromFolder.py H:\proj2\files\

Notes:
1. To modify the number of input arguments, see the 'fields' dictionary below.
2. This version of the createGeomarksFromFolder.py script does not support the
   'Any' value for geometryType. Input files must contain a single geometryType.
3. This version of the createGeomarksFromFolder.py script does not support the
   GeoPackage file format.
"""
# -------------------------------------------------------------------
# -------------------------------------------------------------------

# IMPORT MODULES

import csv
import json
import os
import pathlib
import platform
import re
import requests
import shapefile
import shutil
import sys
import time

# -------------------------------------------------------------------
# -------------------------------------------------------------------

# CHECK ARGUMENTS AND OPERATING SYSTEM

# Check that the correct number of arguments were provided
if len(sys.argv) < 1 or len(sys.argv) > 2:
    print("Missing arguments (1 required). Script will now exit.")
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

# Geomark request URL.
geomarkEnv = "https://apps.gov.bc.ca/pub/geomark/geomarks/new"

# Uncomment to submit requests to the test environment
# geomarkEnv = 'https://test.apps.gov.bc.ca/pub/geomark/geomarks/new'

headers = {"Accept": "*/*"}

folderPath = sys.argv[1]

# If missing, add final slash to the folder path
if folderPath[-1] != '\\' or folderPath[-1] != '/':
    folderPath += slash

resultLog = open(
    folderPath + "geomarkURLs_" +
    time.strftime("%Y%m%d-%H%M%S") + ".csv", "a")

resultLog.write("inputFile,geomarkUrl,errorMessage\n")

allowedFileTypes = ["geojson", "gml", "kml", "shp", "wkt"]

convertFileTypes = ["kmz", "shpz"]

# -------------------------------------------------------------------
# -------------------------------------------------------------------

# FUNCTIONS


def geojsonDetails(geojsonFile):
    # This function will examine a GeoJSON file and return geometryType
    # and multiple(true/false).

    suportedGeometryTypesGeoJSON = {
        '"geometryType": "Point"': "Point",
        '"geometryType": "LineString"': "LineString",
        '"geometryType": "Polygon"': "Polygon",
        '"geometryType": "MultiPoint"': "Point",
        '"geometryType": "MultiLineString"': "LineString",
        '"geometryType": "MultiPolygon"': "Polygon",
    }

    data = json.loads(geojsonFile)

    # Determine the geometry type
    if len(re.findall(rf"{'FeatureCollection'}", geojsonFile)) == 0:
        geomType_geojson = data["geometry"]["type"]
    elif len(re.findall(rf"{'FeatureCollection'}", geojsonFile)) > 0:
        geomType_geojson = data["features"][0]["geometry"]["type"]

    # Determine if there are multiple features
    if "Multi" in geomType_geojson:
        multi_geojson = "true"
    elif (
        "Multi" not in geomType_geojson
        and len(re.findall(rf"{'coordinates'}", geojsonFile)) > 1
    ):
        multi_geojson = "true"
    elif (
        "Multi" not in geomType_geojson
        and len(re.findall(rf"{'coordinates'}", geojsonFile)) == 1
    ):
        multi_geojson = "false"

    # Translate the geometry type label
    for geomKeyGeojson, geomValGeojson in suportedGeometryTypesGeoJSON.items():
        if geomKeyGeojson == geomType_geojson:
            geomType_geojson = geomValGeojson

    return geomType_geojson, multi_geojson


def gmlDetails(gmlFile):
    # This function will examine a GML file and return geometryType,
    # srid and multiple(true/false).

    sridValuesGML = {
        'rsName="EPSG:3005"': "3005",
        'srsName="EPSG:4326"': "4326",
        'srsName="EPSG:26907"': "26907",
        'srsName="EPSG:26908"': "26908",
        'srsName="EPSG:26909"': "26909",
        'srsName="EPSG:26910"': "26910",
        'srsName="EPSG:26911"': "26911",
    }

    suportedGeometryTypesGML = {
        "</gml:Point>": "Point",
        "</gml:LineString>": "LineString",
        "</gml:Polygon>": "Polygon",
    }

    # Determine the SRID value
    for sridKeyGML, sridValGML in sridValuesGML.items():
        sridMatchGML = len(re.findall(rf"{sridKeyGML}", gmlFile))
        if sridMatchGML > 0:
            projID_gml = sridValGML
            break

    # Determine the geometry type and if there are multiple features
    for geomKeyGML, geomValGML in suportedGeometryTypesGML.items():
        geomMatchGML = len(re.findall(rf"{geomKeyGML}", gmlFile))
        if geomMatchGML == 1:
            geomType_gml = geomValGML
            multi_gml = "false"
            break
        elif geomMatchGML > 1:
            geomType_gml = geomValGML
            multi_gml = "true"
            break

    return geomType_gml, multi_gml, projID_gml


def kmlDetails(kmlFile):
    # This function will examine a KML file and return geometryType
    # and multiple(true/false).

    suportedGeometryTypesKML = {
        "<Point>": "Point",
        "<LineString>": "LineString",
        "<Polygon>": "Polygon",
    }

    # Determine the geometry type and if there are multiple features
    for geomKeyKML, geomValKML in suportedGeometryTypesKML.items():
        geomMatchKML = len(re.findall(rf"{geomKeyKML}", kmlFile))

        if geomMatchKML == 1:
            geomType_kml = geomValKML
            multi_kml = "false"
            break
        elif geomMatchKML > 1:
            geomType_kml = geomValKML
            multi_kml = "true"
            break

    return geomType_kml, multi_kml


def shpDetails(inputShapefile, shpFolderPath, shpFilename):
    # This function will examine a shapefile and return geometryType,
    # srid and multiple(true/false).

    inputPrj = shpFolderPath + (shpFilename.replace("shp", "prj"))
    projection = open(inputPrj, "r").read()

    sridValuesSHP = {
        "BC_Environment_Albers": "3005",
        "WGS_1984": "4326",
        "UTM_Zone_7": "26907",
        "UTM_Zone_8": "26908",
        "UTM_Zone_9": "26909",
        "UTM_Zone_10": "26910",
        "UTM_Zone_11": "26911",
    }

    suportedGeometryTypesSHP = {
        "POINT": "Point",
        "POLYLINE": "LineString",
        "POLYGON": "Polygon",
    }

    # Determine the SRID value
    for sridKeySHP, sridValSHP in sridValuesSHP.items():
        sridMatchSHP = len(re.findall(rf"{sridKeySHP}", projection))
        if sridMatchSHP > 0:
            projID_shp = sridValSHP
            break

    # Determine the geometry type
    for geomKeySHP, geomValSHP in suportedGeometryTypesSHP.items():
        geomMatchSHP = inputShp.shapeTypeName
        if geomMatchSHP == geomKeySHP:
            geomType_shp = geomValSHP
            break

    # Determine if the file has multiple features
    featureCount = inputShapefile.numShapes
    if featureCount > 1:
        multi_shp = "true"
    elif featureCount == 1:
        multi_shp = "false"
    else:
        print("Shapefile (" + str(inputShapefile) + ") must contain features")

    return geomType_shp, multi_shp, projID_shp


def wktDetails(wktFile):
    # This function will examine a WKT file and return geometryType,
    # srid and multiple(true/false).

    # Assign initial blank values to validate assignment
    geomType_wkt = ""
    projID_wkt = ""

    sridValues = {
        "SRID=3005": "3005",
        "SRID=4326": "4326",
        "SRID=26907": "26907",
        "SRID=26908": "26908",
        "SRID=26909": "26909",
        "SRID=26910": "26910",
        "SRID=26911": "26911",
    }

    suportedGeometryTypes = {
        "POINT": "Point",
        "LINESTRING": "LineString",
        "POLYGON": "Polygon",
        "MULTIPOINT": "Point",
        "MULTILINESTRING": "LineString",
        "MULTIPOLYGON": "Polygon",
        "GEOMETRYCOLLECTION": "Any",
    }

    # Determine the SRID value
    for sridKey, sridVal in sridValues.items():
        sridMatch = len(re.findall(rf"\b{sridKey}\b", wktFile))
        if sridMatch > 0:
            projID_wkt = sridVal
            break

    # Determine the geometry type and if there are multiple features
    for geomKey, geomVal in suportedGeometryTypes.items():
        geomMatch = len(re.findall(rf"\b{geomKey}\b", wktFile))
        if geomMatch > 0:
            geomType_wkt = geomVal
            if geomMatch > 1 or geomKey.count("MULTI") >= 1:
                multi_wkt = "true"
            elif geomMatch == 1 and geomKey.count("MULTI") == 0:
                multi_wkt = "false"
            break

    return geomType_wkt, multi_wkt, projID_wkt


def unZipFile(inputZipFile, fileFolderPath, fileType):

    if pathlib.Path(inputZipFile).suffix.replace(".", "") == fileType:
            newCopy = str(inputZipFile).replace("." +
                                                str(fileType), ".zip")
            shutil.copyfile(inputZipFile, newCopy)
            shutil.unpack_archive(newCopy, fileFolderPath)
            os.remove(newCopy)
            # Unzipped KMZ files contain a doc.kml file.
            # Rename this to the original file name to avoid overwriting.
            if fileType == 'kmz':
                srcFile = os.path.basename(inputZipFile)
                os.replace(fileFolderPath + 'doc.kml', fileFolderPath +
                          str(srcFile).replace('.kmz', '.kml'))


# -------------------------------------------------------------------
# -------------------------------------------------------------------

# PROCESSING

print("\nSupported file types: " + str(allowedFileTypes).strip("[]"))
print(
    "\nNotes:\n"
    "1. KMZ and SHPZ files will automatically be "
    "converted to KML and SHP format before processing.\n"
    "2. This script does not support the 'Any' value for geometryType. "
    "Input files must contain a single geometryType.\n"
)


# If KMZ or SHPZ files are found, convert to KML or SHP
for fileTypeToConvert in convertFileTypes:
    if len(list(pathlib.Path(folderPath).glob("*." + str(fileTypeToConvert)))) > 0:
        for zipFound in list(pathlib.Path(folderPath).glob("*." + str(fileTypeToConvert))):
            print("Unzipping " + str(zipFound))
            unZipFile(zipFound, folderPath, fileTypeToConvert)

# Filter folder contents to those with allowed formats
folderContents = [
    f for f in os.listdir(folderPath)
    if re.match(r"^.*\.(?:geojson|gml|kml|kmz|shp|wkt)$", f)]

# Iterate allowed files in folder and convert to Geomarks
for file in folderContents:
    fileFormat = (pathlib.Path(file).suffix).replace(".", "")
    if len(re.findall(rf"\b{fileFormat}\b", str(allowedFileTypes))) > 0:

        geometryType = ""
        multiple = "false"
        srid = ""
        files = {"body": open(folderPath + file, "rb")}

        if fileFormat == "geojson":
            srid = "4326"
            geojsonContents = open(folderPath + file, "r").read()
            geometryType, multiple = geojsonDetails(geojsonContents)
            print(
                "\n"
                + str(file)
                + " is a GeoJSON file ("
                + str(geometryType)
                + ") with multiple="
                + multiple
                + ", in srid="
                + srid
            )
        elif fileFormat == "gml":
            gmlContents = open(folderPath + file, "r").read()
            geometryType, multiple, srid = gmlDetails(gmlContents)
            print(
                "\n"
                + str(file)
                + " is a gml file ("
                + str(geometryType)
                + ") with multiple="
                + multiple
                + ", in srid="
                + srid
            )
        elif fileFormat == "kml":
            srid = "4326"
            kmlContents = open(folderPath + file, "r").read()
            geometryType, multiple = kmlDetails(kmlContents)
            print(
                "\n"
                + str(file)
                + " is a KML file ("
                + str(geometryType)
                + ") with multiple="
                + multiple
                + ", in srid="
                + srid
            )
        elif fileFormat == "shp":
            inputShp = shapefile.Reader(folderPath + file)
            geometryType, multiple, srid = shpDetails(inputShp,
                                                      folderPath, file)
            inputShp.close()
            print(
                "\n"
                + str(file)
                + " is a shapefile ("
                + str(geometryType)
                + ") with multiple="
                + multiple
                + ", in srid="
                + srid
            )
        elif fileFormat == "wkt":
            wktContents = open(folderPath + file, "r").read()
            geometryType, multiple, srid = wktDetails(wktContents)
            print(
                "\n"
                + str(file)
                + " is a wkt file ("
                + str(geometryType)
                + ") with multiple="
                + multiple
                + ", in srid="
                + srid
            )

        # Geomark Web Service request parameter values
        fields = {
            "allowOverlap": "true",
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

        print("Sending request to: " + geomarkEnv)

        # Submit request to the Geomark Web Service and parse response

        try:
            geomarkRequest = requests.post(
                geomarkEnv, files=files, headers=headers, data=fields
            )
            geomarkResponse = (
                str(geomarkRequest.text)
                .replace("(", "")
                .replace(")", "")
                .replace(";", "")
            )
            data = json.loads(geomarkResponse)
            geomarkInfoPage = data["url"]

            print("Geomark info page: " + geomarkInfoPage)
            logEntry = str(file) + "," + geomarkInfoPage + "\n"
            resultLog.write(logEntry)
        except (NameError, TypeError, KeyError) as error:
            print("*********************************************************")
            print("Error processing Geomark request for " + str(file))
            print(data["error"])
            print("*********************************************************")
            logEntry = (
                str(file) + "," + "" + "," + '"' +
                str(data["error"]) + '"' + "\n"
            )
            resultLog.write(logEntry)
            pass
        except (ValueError) as error2:
            print("*********************************************************")
            print("Error processing Geomark request for " + str(file))
            print(error2)
            print("*********************************************************")
            logEntry = (str(file) + "," + "" + "," +
                        '"' + str(error2) + '"' + "\n")
            resultLog.write(logEntry)
            pass

# Close the CSV file
resultLog.close()
