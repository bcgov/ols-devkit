# -------------------------------------------------------------------
# -------------------------------------------------------------------

# DESCRIPTION
'''

This script will calculate the average drive time,
distance (average, median, min, max) and number of
addresses per facility location.

If there is a single minimum road distance value of 0 m, it will be assumed
to represent an identical origin and destination coordinate. In this case,
the 0 m value will be ignored and the next largest minimum value used. If
there is more than one minimum road distance value of 0 m it will be assumed
to represent multiple units within the same building and used as the minimum
distance value.

Instructions:

 1. Start menu -> Run -> Type 'cmd'
 2. Navigate to the folder where this script is located

        python <Script_name.py> <workspace path including final slash>
                                <input filename including extension>
                                <addresses ID field>
                                <X coordinate field (from address file)>
                                <Y coordinate field (from address file)>
                                <facility ID field>
                                <X coordinate field (from facility file)>
                                <Y coordinate field (from facility file)>
                                <epsg_code>
                                <API key for BC Route Planner (PROD)>

Example:
        python 2_avg_dist_time_per_destination.py
               C:\temp\nearestLocationBetweenPairs\
               sample_addresses_nearest_schools_20241231_114219.csv
               address albers_x albers_y
               nearest_facility coord_x coord_y
               3005
               0rm3P1W86iBbaSYafinyOXrIZjiSbOgi

Assumptions:

- You have both an 'input' and 'output' folder in your workspace
- ID fields have unique values
- The nearest facility python script has been run first which created the
  csv file referenced by the input parameter 'nearest_facility_filename'
- Correct field names have been provided.
- The EPSG code assumes the same coordinate system for both location
  fields in the CSV.
- EPSG code is one of the following:
  4326, 4269, 3005, 26907, 26908, 26909, 26910, 26911
  - Hardcoded values for request to BC Route Planner
   > criteria=fastest
   > correctSide=false
   > distanceUnit=km
   > enable=gdf,ldf,tr,xc,tc

'''
# -------------------------------------------------------------------
# -------------------------------------------------------------------

# IMPORT MODULES

import csv
import gc
import pandas as pd
import platform
import requests
from requests.sessions import Session
from retry import retry
from statistics import mean, median
import sys
import time
import warnings

warnings.filterwarnings('ignore')


# -------------------------------------------------------------------
# -------------------------------------------------------------------

# FUNCTIONS

s = requests.Session()


@retry(Exception, tries=3, delay=5, backoff=2)
def submit_request(fromPt, toPts, epsg_code):
    # Submit a betweenPairs request to the BC Route Planner

    routePlanner = (
        'https://router.api.gov.bc.ca/distance/'
        'betweenPairs.json?'
        'fromPoints={}&toPoints={}&criteria=fastest&'
        'outputSRS={}&enable=sc,gdf,ldf,tr,xc,tc&'
        'correctSide=false&'
        'distanceUnit=km'.format(fromPt.strip(), toPts.strip(), epsg_code)
    )
    try:
        response = (requests.get(routePlanner + route_planner_apikey.strip(),
                    timeout=10))
        # Raise HTTPError for responses (400's and 500's)
        response.raise_for_status()
        # Return parsed JSON response
        return response.json()
    except requests.exceptions.Timeout:
        print(f'Timeout occurred: fromPoint={fromPt}, toPoints={toPts}')
        return None
    except requests.exceptions.RequestException as e:
        print(f'Request error: {e} (fromPoint={fromPt}, toPoints={toPts})')
        return None


def parse_response(jsonResponse, propValue1, propValue2):
    # Parse the propValue (drive time or distance)
    # from the response and append to a list

    propValue1List = []
    propValue2List = []
    global totalRouteCount
    global routeNotFoundCount

    try:
        coordPairs = jsonResponse['pairs']
        for i in range(0, len(coordPairs)):
            totalRouteCount += 1
            if (coordPairs[i][propValue1] != -1
                    and coordPairs[i][propValue2] != 0):
                propValue1List.append(int(coordPairs[i][propValue1]))
                propValue2List.append(float(coordPairs[i][propValue2]))
            else:
                routeNotFoundCount += 1
    except KeyError:
        coordPairs = 'Missing'
        print('Pairs missing from response. Ending script')
        exit()

    return propValue1List, propValue2List


def seconds_to_hours(secs):

    return time.strftime('%H:%M:%S', time.gmtime(secs))


def write_to_csv(file, row_values):
    # Write to a resultant CSV file

    with open(file, 'a', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(row_values)


# -------------------------------------------------------------------
# -------------------------------------------------------------------

# VARIABLE DEFINITIONS

# Determine if script is running on Windows or Linux
pltFrm = platform.system()

# Define which slash to use based on platform
if pltFrm == 'Windows':
    slash = '\\'
elif pltFrm == 'Linux':
    slash = '/'

# Check that the correct number of arguments were provided
if len(sys.argv) != 11:
    print('Incorrect number of arguments (10 required). Script will now exit.')
    exit()

# Your workspace filepath (including final slash)
wrkspc = sys.argv[1]

# Input CSV file containing the addresses and nearest facilities.
nearest_facility_filename = sys.argv[2]

input_filepath = wrkspc + 'input' + slash + nearest_facility_filename

expected_columns = [
    sys.argv[3], sys.argv[4], sys.argv[5],  # address_id, x, y cooords
    sys.argv[6], sys.argv[7], sys.argv[8]   # facility_id, x, y coords
]

# Get the current timestamp
timestamp = time.strftime('%Y%m%d_%H%M%S', time.localtime())

# Output csv file
outputFileName = (wrkspc + 'output' + slash +
                  'average_drive_time_and_distance_' +
                  timestamp + '.csv')

# The unique ID field for the address list (can be fullAddress, ID, etc)
address_id = sys.argv[3]

# The X coordinates for the addresses
address_x_coord = sys.argv[4]

# The Y coordinates for the addresses
address_y_coord = sys.argv[5]

# The unique ID field for the facility list (can be facility name, ID, etc)
facility_id = sys.argv[6]

# The X coordinates for the facilities
facility_x_coord = sys.argv[7]

# The X coordinates for the facilities
facility_y_coord = sys.argv[8]

# List of valid EPSG codes
valid_epsg_codes = {4326, 4269, 3005, 26907, 26908, 26909, 26910, 26911}

try:
    epsg_code = int(sys.argv[9])
except (IndexError, ValueError):
    print('Invalid input. Please provide an integer value for EPSG code.')
    sys.exit(1)

# Check if the EPSG code is valid
if epsg_code not in valid_epsg_codes:
    print(f'Invalid EPSG code: {epsg_code}. '
          'Please use one of the following'
          ': 4326, 4269, 3005, 26907, 26908, 26909, 26910, 26911.')
    sys.exit(1)

# Your API key for the BC Route Planner (PROD)
apiKey = sys.argv[10]

# API rate limit for the BC Route Planner (minus 1)
api_rate_limit = 999

# A dictionary to hold the ID and coordinates of the facilities
unique_facilities = {}

# Each between pairs request can accept 100 coordinate pairs.
# 98 represents the 'toPoints'. In addition, there will be 1 fromPoint
# which represents the facility
dfCeiling = 98

# A list to hold timestamps to ensure the API key limit is not reached.
timestamps = []

# Variables used to assemble a web request
route_planner_apikey = '&apikey={}'.format(apiKey)

# An integer representing the average drive time in minutes.
avgDriveTime = 0

# A float representing the average distance (in km).
avgDistance = 0.0

# Count of total routes
totalRouteCount = 0

# Count of total route not found cases
routeNotFoundCount = 0

# List to hold the drive times and distances
# from each site point to the admin area facility locations
driveTimeList = []
driveTimeListComplete = []
distanceList = []
distanceListComplete = []

# Time stamps to calculate script processing time.
scriptStartTime = 0
scriptStopTime = 0
scriptProcessingTime = 0

# -------------------------------------------------------------------
# -------------------------------------------------------------------

# PROCESSING

scriptStartTime = time.perf_counter()
print('Current time: ' + str(time.ctime()) + '\n')

# Read the CSV file in chunks and concatenate into a single DataFrame
tr_nearest_facilities = pd.read_csv(
    wrkspc + 'input' + slash + nearest_facility_filename,
    sep=',',
    usecols=[address_id, address_x_coord, address_y_coord, facility_id,
             facility_x_coord, facility_y_coord],
    dtype={
        address_id: 'object',
        address_x_coord: 'object',
        address_y_coord: 'object',
        facility_id: 'object',
        facility_x_coord: 'object',
        facility_y_coord: 'object'
    },
    iterator=True,
    on_bad_lines='warn',
    index_col=False,
    chunksize=1000,
)

# Initialize a list to store skipped rows
skipped_rows = []

# Process each chunk and handle NA values in integer columns
processed_chunks = []
for chunk in tr_nearest_facilities:
    try:
        # Drop rows with NA in integer columns
        chunk.dropna(subset=[address_x_coord, address_y_coord,
                             facility_x_coord, facility_y_coord],
                     inplace=True)
        processed_chunks.append(chunk)
    except ValueError as e:
        skipped_rows.append(chunk)  # Log the skipped rows
        print(f"Skipped a chunk due to error: {e}")

# Combine all valid chunks into a single DataFrame
df_nearest_fac = pd.concat(processed_chunks, ignore_index=True)

# Optionally log skipped rows to a file for debugging
if skipped_rows:
    with open(wrkspc + 'output' + slash + 'skipped_rows.log', 'w') as log_file:
        log_file.write("Rows skipped due to errors:\n")
        for skipped_chunk in skipped_rows:
            log_file.write(skipped_chunk.to_csv(index=False) + "\n")

del tr_nearest_facilities

# Check to see if the CSV (now in a dataframe) contains the expected columns.
csv_column_check = set(expected_columns) - set(df_nearest_fac.columns)
if csv_column_check:
    raise ValueError(f'Missing columns in CSV: {csv_column_check}')

# Create a dictionary with unique facility IDs as keys and
# concatenated coordinates as values
for facility in df_nearest_fac[facility_id].unique():
    # Filter rows for the current facility
    facility_data = (
        df_nearest_fac[df_nearest_fac[facility_id] == facility].iloc[0]
    )
    # Concatenate the coordinates
    concatenated_coords = (
        f'{facility_data[facility_x_coord]},{facility_data[facility_y_coord]}'
        .replace(' ', '')
    )
    # Add to the dictionary
    unique_facilities[facility] = concatenated_coords

# Create an empty DataFrame with the same structure as df_nearest_fac
df_temp_fac = pd.DataFrame(columns=df_nearest_fac.columns)

# Ensure column data types in df_temp_fac match df_nearest_fac
for column in df_nearest_fac.columns:
    df_temp_fac[column] = (
        df_temp_fac[column].astype(df_nearest_fac[column].dtype)
    )

outputFileContents = [
    'facility_id',
    'avg_drv_time_sec',
    'avg_drv_time_hrs',
    'avg_dist',
    'median_dist',
    'min_dist',
    'max_dist',
    'address_count'
]

write_to_csv(outputFileName, outputFileContents)

# Iterate through unique facility IDs in the list created above
for unique_facility in unique_facilities.keys():

    # First check to ensure that we are not reaching the API key limit
    current_time = time.time()
    timestamps.append(current_time)

    # Remove timestamps older than 59 seconds
    timestamps = [t for t in timestamps if current_time - t <= 59]

    # Check if we are approaching the API key rate limit
    if len(timestamps) > api_rate_limit:
        wait_time = 60 - (
            current_time - timestamps[0]
        )  # Time until rate limit resets
        print(f'Rate limit reached. Pausing for {wait_time:.2f} seconds.')
        time.sleep(wait_time)

    print('Processing: ' + str(unique_facility))

    # Create the facility fromPoints
    fromPoints = unique_facilities[unique_facility]

    # Populate df_temp_fac with rows matching the current facility ID
    # (copied from the main dataframe called df_nearest_fac)
    df_temp_fac = df_nearest_fac[df_nearest_fac[facility_id] == unique_facility]

    # Number of addresses that are closest to this facility
    address_count = len(df_temp_fac)

    # Check if the number of rows exceeds dfCeiling
    if address_count > dfCeiling:

        driveTimeListComplete.clear()
        distanceListComplete.clear()

        # Process in chunks of dfCeiling
        for i in range(0, len(df_temp_fac), dfCeiling):
            chunk = df_temp_fac.iloc[i:i + dfCeiling]

            # Create the toPoints variable by concatenating address coordinates
            toPoints = ','.join(
                f'{row[address_x_coord]},{row[address_y_coord]}'.replace(' ', '')
                for _, row in chunk.iterrows()
            )

            # Send request for each chunk
            routerResponse = submit_request(str(fromPoints), str(toPoints), epsg_code)
            driveTimeList, distanceList = parse_response(
                routerResponse, 'time', 'distance'
            )
            driveTimeListComplete.extend(driveTimeList)
            distanceListComplete.extend(distanceList)

    else:
        # Create the toPoints variable for all rows (if less than dfCeiling)
        toPoints = ','.join(
            f'{row[address_x_coord]},{row[address_y_coord]}'.replace(' ', '')
            for _, row in df_temp_fac.iterrows()
        )

        driveTimeListComplete.clear()
        distanceListComplete.clear()
        routerResponse = submit_request(str(fromPoints), str(toPoints), epsg_code)
        driveTimeList, distanceList = parse_response(
            routerResponse, 'time', 'distance'
        )
        driveTimeListComplete = driveTimeList
        distanceListComplete = distanceList

    # Clear the contents of df_temp_fac
    df_temp_fac = df_temp_fac.iloc[0:0]

    # Calculate average drive time and distance
    avgDriveTime = int(mean(driveTimeListComplete))
    avgDriveTimeHrs = seconds_to_hours(avgDriveTime)
    avgDistance = round(mean(distanceListComplete), 2)

    # Calculate average, median, min, and max distances
    medianDistance = round(median(distanceListComplete), 2)
    maxDistance = round(max(distanceListComplete), 2)

    # Count the number of times a driving distance of 0 m is found.
    zero_distance_count = sum(1 for d in distanceListComplete if d == 0)

    if zero_distance_count == 1:
        # Find the smallest non-zero distance
        non_zero_distances = [d for d in distanceListComplete if d > 0]
        minDistance = round(min(non_zero_distances), 4) if non_zero_distances else 0
    else:
        minDistance = round(min(distanceListComplete), 4)

    outputFileContents = [
        unique_facility,
        str(avgDriveTime),
        str(avgDriveTimeHrs),
        str(avgDistance),
        str(medianDistance),
        str(minDistance),
        str(maxDistance),
        str(address_count)
        ]
    write_to_csv(outputFileName, outputFileContents)


# Final Cleanup Section
# Clear lists, variables, and invoke garbage collection
print('\nCleaning up variables')

address_id = None
address_x_coord = None
address_y_coord = None
apiKey = None
api_rate_limit = None
avgDriveTime = None
avgDriveTimeHrs = None
avgDistance = None
csv_column_check = None
del df_nearest_fac
del df_temp_fac
distanceList.clear()
distanceListComplete.clear()
driveTimeList.clear()
driveTimeListComplete.clear()
epsg_code = None
facility_id = None
facility_x_coord = None
facility_y_coord = None
maxDistance = None
medianDistance = None
minDistance = None
nearest_facility_filename = None
route_planner_apikey = None
routeNotFoundCount = None
timestamps.clear()
totalRouteCount = None
unique_facilities.clear()
wrkspc = None

# Explicitly run garbage collection
gc.collect()

print('\nMemory cleanup completed.')

scriptStopTime = time.perf_counter()
scriptProcessingTime = scriptStopTime - scriptStartTime
print('\nThe script processed in: ' +
      str(time.strftime('%H:%M:%S', time.gmtime(scriptProcessingTime))))

scriptStartTime = None
scriptStopTime = None
scriptProcessingTime = None
