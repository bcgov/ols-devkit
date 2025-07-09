# -------------------------------------------------------------------
# -------------------------------------------------------------------

# DESCRIPTION
'''

This script will calculate the average drive time,
distance (average, median, min, max) and number of
addresses per facility location. This script will provide
results based on the input file only and not make use of the
BC Route Planner.

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
                                <drive distance>
                                <drive time>
                                <epsg_code>

Example:
        python 2b_avg_dist_time_per_destination_no_routing.py
               C:\temp\nearestLocationBetweenPairs\
               sample_addresses_nearest_schools_20241231_114219.csv
               address albers_x albers_y
               nearest_facility coord_x coord_y
               drv_dist drv_time_sec
               3005

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

'''
# -------------------------------------------------------------------
# -------------------------------------------------------------------

# IMPORT MODULES

import pandas as pd
import platform
import sys
import time

# -------------------------------------------------------------------
# -------------------------------------------------------------------

# FUNCTIONS

# This function will determine if the minimum distance value should
# be zero, or the next highest value.
def adjusted_min_dist(group):
    distances = group['drv_dist'].sort_values()
    zeros = (distances == 0).sum()
    if zeros == 1:
        # Return the next smallest value after the zero
        return distances[distances > 0].iloc[0]
    else:
        # Keep zero if there are multiple or none
        return distances.min()

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
if len(sys.argv) != 12:
    print('Incorrect number of arguments (11 required). Script will now exit.')
    exit()

# Your workspace filepath (including final slash)
wrkspc = sys.argv[1]

# Input CSV file containing the addresses and nearest facilities.
nearest_facility_filename = sys.argv[2]

input_filepath = wrkspc + 'input' + slash + nearest_facility_filename

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

# Drive distance field
drv_dist = sys.argv[9]

# Drive time field (in seonds)
drv_time_sec = sys.argv[10]

# List of valid EPSG codes
valid_epsg_codes = {4326, 4269, 3005, 26907, 26908, 26909, 26910, 26911}

try:
    epsg_code = int(sys.argv[11])
except (IndexError, ValueError):
    print('Invalid input. Please provide an integer value for EPSG code.')
    sys.exit(1)

# Check if the EPSG code is valid
if epsg_code not in valid_epsg_codes:
    print(f'Invalid EPSG code: {epsg_code}. '
          'Please use one of the following'
          ': 4326, 4269, 3005, 26907, 26908, 26909, 26910, 26911.')
    sys.exit(1)

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
             facility_x_coord, facility_y_coord, drv_dist, drv_time_sec],
    dtype={
        address_id: 'object',
        address_x_coord: 'object',
        address_y_coord: 'object',
        facility_id: 'object',
        facility_x_coord: 'object',
        facility_y_coord: 'object',
        drv_dist: 'float',
        drv_time_sec: 'float'
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

# Optionally log skipped rows to file for debugging
if skipped_rows:
    with open(wrkspc + 'output' + slash + 'skipped_rows.log', 'w') as log_file:
        log_file.write('Rows skipped due to errors:\n')
        for skipped_chunk in skipped_rows:
            log_file.write(skipped_chunk.to_csv(index=False) + '\n')

del tr_nearest_facilities

# Group by facility and calculate summary statistics
facility_group = df_nearest_fac.groupby(facility_id)

summary = facility_group.agg(
    avg_drv_time_sec=(drv_time_sec, 'mean'),
    avg_dist=(drv_dist, 'mean'),
    median_dist=(drv_dist, 'median'),
    max_dist=(drv_dist, 'max'),
    address_count=(facility_id, 'count')
).reset_index()

# Calaculate min_dist
min_dist_list = facility_group.apply(adjusted_min_dist).reset_index(name='min_dist')
summary = summary.merge(min_dist_list, on=facility_id)

# Rename nearest facility field
summary.rename(columns={facility_id: 'facility_id'}, inplace=True)

# Convert seconds to HH:MM:SS
summary['avg_drv_time_sec'] = summary['avg_drv_time_sec'].round().astype(int)
summary['avg_drv_time_hrs'] = summary['avg_drv_time_sec'].apply(
    lambda x: f'{int(x // 3600):02}:{int((x % 3600) // 60):02}:{int(x % 60):02}'
)

# Round distance values
summary[['avg_dist', 'median_dist', 'min_dist', 'max_dist']] = summary[[
    'avg_dist', 'median_dist', 'min_dist', 'max_dist'
]].round(3)

# Reorder columns
summary = summary[[
    'facility_id', 'avg_drv_time_sec', 'avg_drv_time_hrs',
    'avg_dist', 'median_dist', 'min_dist', 'max_dist', 'address_count'
]]

# Save results to a CSV file
summary.to_csv(outputFileName, index=False)

print(f'Summary saved to: {outputFileName}')

scriptStopTime = time.perf_counter()
scriptProcessingTime = scriptStopTime - scriptStartTime
print('\nThe script processed in: ' +
      str(time.strftime('%H:%M:%S', time.gmtime(scriptProcessingTime))))

scriptStartTime = None
scriptStopTime = None
scriptProcessingTime = None
