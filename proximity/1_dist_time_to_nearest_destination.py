# -------------------------------------------------------------------
# -------------------------------------------------------------------

# DESCRIPTION
'''

This script will use two CSV input files (addresses, facility locations)
and read them into memory. Following that, the script will confirm the
nearest facility to each address by evaluating a number of the nearest
facilities to each address as a percentage of the total. For example, if
there are 120 facilities and a value of .01 is provided, then the nearest
12 facilities (euclidean distance) to each address will be routed
to determine the single nearest facility.

Instructions:

 1. Start menu -> Run -> Type 'cmd'
 2. Navigate to the folder where this script is located

        python <Script_name.py> <workspace path including final slash>
                                <fromPoints (addresses)
                                    filename including extension>
                                <Unique ID field (from addresses file)>
                                <X coordinate field (for addresses)>
                                <Y coordinate field (for addresses)>
                                <toPoints (facilities)
                                    filename including extension>
                                <Unique ID field (from facility file)>
                                <X coordinate field (from facilities file)>
                                <Y coordinate field (from facilities file)>
                                <Facility location subset percentage value
                                    (between 0.001 and 1.0)>
                                <EPSG code (Available values : 4326, 4269,
                                    3005, 26907, 26908, 26909, 26910, 26911)>
                                <API key for BC Route Planner (PROD)>
                                <dataset tag>

Example:
        python 1_dist_time_to_nearest_destination.py
            C:\temp\nearestLocationBetweenPairs\
            sample_addresses.csv
            address albers_x albers_y
            schools_vancouver.csv
            school_nam albers_x albers_y
            0.3
            3005
            rHmXTRFtYq162zO0A5YkyZlTKdt3EFZO
            schools


Assumptions:

- You have both an 'input' and 'output' folder in your workspace
- Correct field names have been provided including a unique ID field.
- Facility location subset percentage is written as follows:
    - 10% = 0.1
    - 100% = 1 (or 1.0)
- The epsg code provided applies to the coordinate system used in both input files.
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
import json
import math
import os
import platform
import pandas as pd
import requests
from requests.sessions import Session
from retry import retry
import sys
import time
import warnings

warnings.filterwarnings('ignore')

# -------------------------------------------------------------------
# -------------------------------------------------------------------

# FUNCTIONS


def calculate_euclidean_distance(coord1, coord2):
    # Calculate the Euclidean distance between two points.

    x1, y1 = map(float, coord1.split(','))
    x2, y2 = map(float, coord2.split(','))

    return math.sqrt((x2 - x1) ** 2 + (y2 - y1) ** 2)


s = requests.Session()


@retry(Exception, tries=3, delay=5, backoff=2)
def submit_request(fromPt, toPts, epsg_code):
    routePlanner = (
        'https://router.api.gov.bc.ca/distance/'
        'betweenPairs.json?'
        'fromPoints={}&toPoints={}&criteria='
        'fastest&outputSRS={}&enable=gdf,ldf,tr,xc,tc&'
        'correctSide=false'
        '&distanceUnit=km'.format(fromPt.strip(), toPts.strip(), epsg_code)
    )

    try:
        response = (requests.get(routePlanner + route_planner_apikey.strip(),
                    timeout=10))
        # Raise HTTPError for bad responses (4xx and 5xx)
        response.raise_for_status()
        # Return parsed JSON response
        return response.json()
    except requests.exceptions.Timeout:
        print(f'Timeout occurred: fromPoint={fromPt}, toPoints={toPts}')
        return None
    except requests.exceptions.RequestException as e:
        print(f'Request error: {e} (fromPoint={fromPt}, toPoints={toPts})')
        return None


def find_min_drive_time(json_data, address):
    pairs = json_data['pairs']

    min_time = float('inf')
    min_to_index = -1
    min_to_point = []
    min_to_point.append(0)
    min_to_point.append(0)

    for pair in pairs:
        if pair['time'] != -1.0 and pair['time'] < min_time:
            min_time = pair['time']
            min_to_index = pair['to']

    if min_to_index != -1:
        min_to_point = json_data['toPoints'][min_to_index]
        min_drv_time = json_data['pairs'][min_to_index]['time']
        min_drv_dist = json_data['pairs'][min_to_index]['distance']
    elif min_to_index == -1:
        coordinates = json_data['fromPoints']
        log_entry = [address] + coordinates
        write_to_csv(log_no_route_found, log_entry)
    else:
        print('No toPoints found. Script exiting (see json below)')
        print(json_data)
        sys.exit()

    return min_to_index, min_to_point, min_drv_time, min_drv_dist


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

# API rate limit for the BC Route Planner (minus 1)
api_rate_limit = 999

# Check that the correct number of arguments were provided
if len(sys.argv) != 14 and len(sys.argv) != 15:
    print('Incorrect number of arguments (13 required). Script will now exit.')
    exit()

# Your workspace filepath (including final slash)
wrkspc = sys.argv[1]

# Input csv file (of address locations) including extension
address_file_name = sys.argv[2]

# Input fromPoints addresses file
address_id_field = sys.argv[3]

# Input address X coordinate field
address_x_coord_field = sys.argv[4]

# Input address Y coordinate field
address_y_coord_field = sys.argv[5]

# Input csv file (of facility locations) including extension
facility_file_name = sys.argv[6]

# Input address (full address) field for facilities (destination points)
facility_id_field = sys.argv[7]

# Input facility address X coordinate field
facility_x_coord_field = sys.argv[8]

# Input facility address Y coordinate field
facility_y_coord_field = sys.argv[9]

# The percentage of the total facility locations across the province to
# select near each address to determine the closest facility.
facility_sample = float(sys.argv[10])

# List of valid EPSG codes
valid_epsg_codes = {4326, 4269, 3005, 26907, 26908, 26909, 26910, 26911}

try:
    epsg_code = int(sys.argv[11])
except (IndexError, ValueError):
    print("Invalid input. Please provide an integer value for EPSG code.")
    sys.exit(1)

# Check if the EPSG code is valid
if epsg_code not in valid_epsg_codes:
    print(f"Invalid EPSG code: {epsg_code}. "
    "Please use one of the following"
    ": 4326, 4269, 3005, 26907, 26908, 26909, 26910, 26911.")
    sys.exit(1)

# Your API key for the BC Route Planner (PROD)
apiKey = sys.argv[12]

# dataset tag to flag results from a specific dataset in the combined
# results CSV. For example, if using a schools dataset, the tag could be
# 'schools'.
data_tag = sys.argv[13]

try:
    # Check if the value is within the valid range
    if not (0.001 <= facility_sample <= 1.0):
        raise ValueError(f'Invalid value for facility_sample: {facility_sample}. Must be between 0.001 and 100.0.')
except IndexError:
    print('Error: Missing required argument for facility_sample.')
    sys.exit(1)
except ValueError as e:
    print(f'Error: {e}')
    sys.exit(1)

timestamps = []

# Generate the timestamp string
current_timestamp = time.strftime("%Y%m%d_%H%M%S")

# Output csv file containing the closest facility to each address
output_file_from_points = (
    wrkspc
    + 'output'
    + slash
    + address_file_name.replace('.csv', '')
    + '_nearest_' + data_tag + '_' + current_timestamp + '.csv'
)

### A backup copy of the fromPoints file
##from_point_backup = output_file_from_points.replace('.csv', '_backup.csv')

# File to hold a list of fromPoints that don't navigate to any toPoints.
log_no_route_found = output_file_from_points.replace(
    '.csv', '_errors.csv'
)

error_file_headings = 'address,coordinates'

write_to_csv(log_no_route_found, error_file_headings)

# Temporary list to hold field values for the output CSV file
output_file_contents = []

# Variables used to assemble a web request
route_planner_apikey = '&apikey={}'.format(apiKey)
headers = {'apikey': route_planner_apikey}

# The web request sent to the BC Route Planner using urllib
web_request = ''

# Used to capture JSON response from the web request.
json_response = ''

# Create a backup partial results file each time the following
# number of records are processed.
backup_rows = 0

# Initialize a counter for processed requests
processed_requests = 0

# -------------------------------------------------------------------
# -------------------------------------------------------------------

# PROCESSING

scriptStartTime = time.perf_counter()
print('Current time: ' + str(time.ctime()))

# Since the write mode is append, remove any previous copies
# of the following files.
if os.path.exists(output_file_from_points):
    os.remove(output_file_from_points)
elif os.path.exists(log_no_route_found):
    os.remove(log_no_route_found)

# Read in the CSV files with specified fields
# Read the input address CSV into a pandas dataframe in chunks
tpAddressPoints = pd.read_csv(
    wrkspc + 'input' + slash + address_file_name,
    sep=',',
    usecols=[
        address_id_field,
        address_x_coord_field,
        address_y_coord_field,
    ],
    dtype={
        address_id_field: 'object',
        address_x_coord_field: 'object',
        address_y_coord_field: 'object',
    },
    iterator=True,
    on_bad_lines='warn',
    index_col=False,
    chunksize=1000,
)

dfAddresses = pd.concat(list(tpAddressPoints), ignore_index=True)

del tpAddressPoints

dfAddresses['nearest_facility'] = ['Unknown'] * len(dfAddresses)

# Subtract 1 to not count the CSV file header
print(f'\nTotal number of addresses: {len(dfAddresses)-1}')

# Total number of addresses to process
total_requests = len(dfAddresses)-1

# Read the facilities CSV into a pandas dataframe in chunks
tpFacilities = pd.read_csv(
    wrkspc + 'input' + slash + facility_file_name,
    sep=',',
    usecols=[
        facility_id_field,
        facility_x_coord_field,
        facility_y_coord_field,
    ],
    dtype={
        facility_id_field: 'object',
        facility_x_coord_field: 'object',
        facility_y_coord_field: 'object',
    },
    iterator=True,
    on_bad_lines='warn',
    index_col=False,
    chunksize=1000,
)

dfFacilities = pd.concat(list(tpFacilities), ignore_index=True)

dfFacilities['unique_id'] = range(1, len(dfFacilities) + 1)

# Add the new 'tag' field populated with the value from data_tag
dfFacilities['tag'] = data_tag

del tpFacilities

print(f'Total number of facilities: {len(dfFacilities)}')

print(f'\nCalculations required: {len(dfAddresses)-1}\n')

try:
    # Calculate near_facility_count
    near_facility_count = round(len(dfFacilities) * facility_sample)

    # Check if the value is valid
    if near_facility_count < 1 or near_facility_count > len(dfFacilities):
        raise ValueError(
            f'Invalid value for near_facility_count: {near_facility_count}. '
            'The value must be at least 1 and at most '
            'the total number of facilities.'
        )
except ValueError as e:
    print(f'Error: {e}')
    sys.exit(1)

print('For each address, this script will calculate ' +
      'Euclidean distance to all facilities.')
print('A subset of the ' + str(near_facility_count) +
      ' closest facilities to each address will be created.'
)
print('Road distance and drive time will be calculated to all ' +
      str(near_facility_count) +
      ' facilities to determine which one is the nearest.\n'
)

# Iterate through each address and find the N closest facilities as provided
# by the input argument 'facility_sample'.
for index, address_row in dfAddresses.iterrows():
    try:
        # Increment the counter
        processed_requests += 1

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

        current_address = address_row[address_id_field]

        # Create address coordinate string
        address_coord_str = (
            f'{address_row[address_x_coord_field]},{address_row[address_y_coord_field]}'
        )

        # Calculate distances to all facility coordinates
        # and collect facility data
        facility_distances = []
        for _, facility_row in dfFacilities.iterrows():
            facility_coord_str = f'{facility_row[facility_x_coord_field]},{facility_row[facility_y_coord_field]}'
            distance = calculate_euclidean_distance(
                address_coord_str, facility_coord_str
            )
            facility_data = {
                **facility_row.to_dict(), # Include all cols from dfFacilities
                'distance': distance,  # Add calculated distance
            }
            facility_distances.append(facility_data)

        # Sort distances and get the N closest facilities
        closest_facilities_data = sorted(
            facility_distances, key=lambda x: x['distance']
        )[:near_facility_count]

        # Convert the list of dictionaries to a DataFrame
        closest_facilities = pd.DataFrame(closest_facilities_data)

        from_points = address_coord_str

        to_points = ','.join(
            f'{row[facility_x_coord_field]},{row[facility_y_coord_field]}'
            for _, row in closest_facilities.iterrows()
        )

        # Submit the request using the new coordinates string
        routerResponse = submit_request(from_points, to_points, epsg_code)

        if routerResponse == 'error':
            vp_index = -1
            print('no route found for this address: ' + str(current_address))
        else:
            vp_index, vp_coords, vp_time, vp_dist = find_min_drive_time(
                                                      routerResponse,
                                                      current_address)
            avgDriveTimeHrs = seconds_to_hours(vp_time)
        if vp_index != -1:
            # Update dfAddresses fields where 'unique_id' matches
            # vp_index_filter = vp_index + 1
            closest_facility = closest_facilities.iloc[vp_index]
            dfAddresses.loc[
                dfAddresses[address_id_field] == current_address, 'coord_x'
            ] = closest_facility[facility_x_coord_field]
            dfAddresses.loc[
                dfAddresses[address_id_field] == current_address, 'coord_y'
            ] = closest_facility[facility_y_coord_field]
            dfAddresses.loc[
                dfAddresses[address_id_field] == current_address, 'nearest_facility'
            ] = closest_facility[facility_id_field]
            dfAddresses.loc[
                dfAddresses[address_id_field] == current_address, 'drv_time_sec'
            ] = vp_time
            dfAddresses.loc[
                dfAddresses[address_id_field] == current_address, 'drv_time_hrs'
            ] = avgDriveTimeHrs
            dfAddresses.loc[
                dfAddresses[address_id_field] == current_address, 'drv_dist'
            ] = vp_dist
            dfAddresses.loc[
                dfAddresses[address_id_field] == current_address, 'tag'
            ] = closest_facility['tag']
        elif vp_index == -1:
            dfAddresses.loc[
                dfAddresses[address_id_field] == current_address,
                'nearest_facility'
                ] = 'no route found'

        # Print status message after every 100 requests
        if processed_requests % 100 == 0:
            remaining_requests = total_requests - processed_requests
            print(
                'Status ['
                + str(time.ctime())
                + f']: {processed_requests} requests completed, {remaining_requests} requests remaining.'
            )
    except Exception as e:
        print('Error processing address: ' + str(current_address) +
               ' (' + address_coord_str + ').' +
               ' Continuing to process the next address.\n')
        continue  # Skip this address and proceed to the next address


# Reorder the fields in dfAddresses
dfAddresses = dfAddresses[
    [
        address_id_field,
        address_x_coord_field,
        address_y_coord_field,
        'nearest_facility',
        'coord_x',
        'coord_y',
        'drv_time_sec',
        'drv_time_hrs',
        'drv_dist',
        'tag',
    ]
]

try:
    dfAddresses.to_csv(output_file_from_points, index=False)
    print(f'\nResults successfully saved to {output_file_from_points}')
except Exception as e:
    print(f'Error writing to file {output_file_from_points}: {e}')
    backup_file = output_file_from_points.replace('.csv', '_backup.csv')
    print(f'Attempting to write to backup file: {backup_file}')
    try:
        dfAddresses.to_csv(backup_file, index=False)
    except Exception as backup_error:
        print(f'Backup file write also failed: {backup_error}')

# Clear variables holding large datasets
del dfAddresses
del dfFacilities
del output_file_contents

# Clear timestamps and other temporary lists
timestamps.clear()

# Explicitly run garbage collection
gc.collect()

print('\nMemory cleanup completed.')

scriptEndTime = time.perf_counter()
elapsed_time = scriptEndTime - scriptStartTime

# Convert elapsed time to hours, minutes, and seconds
hours, rem = divmod(elapsed_time, 3600)
minutes, seconds = divmod(rem, 60)

print(
    f'\nProcessing complete. Elapsed time: {int(hours)} hours, {int(minutes)} minutes, and {seconds:.2f} seconds'
)
