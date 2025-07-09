# -------------------------------------------------------------------
# -------------------------------------------------------------------

# DESCRIPTION
'''

This script will take a result file from the proximity analysis script
and attempts to replace unroutable addresses with routable addresses.
Candidate addresses will be randomly selected from the unsampled
portion of the complete address list. Each candidate address will then be
submitted to the BC Route Planner to confirm that they are routable. Once
validated, a candidate address will be saved as a replacement address.

The result from this script will be a CSV file containing only the
replacement addresses in a format that is ready to be processed by the
proximity analysis script.

Instructions:

 1. Start menu -> Run -> Type 'cmd'
 2. Navigate to the folder where this script is located

        python <Script_name.py> <workspace path including final slash>
                                <proximity result file with extension>
                                <full, unsampled address file with extension>
                                <Administrative area ID field>
                                <address field name>
                                <X coordinate field (for addresses)>
                                <Y coordinate field (for addresses)>
                                <tag field value>
                                <API key for BC Route Planner (PROD)>

Example:
        python route_salvager.py C:\temp\route_salvager\
                        proximity_results_using_schools.csv
                        site_Hybrid_geocoder_joined.csv
                        DAID
                        FULL_ADDRESS
                        SITE_ALBERS_X
                        SITE_ALBERS_Y
                        schools
                        xMylwLRZi3Phs2tnbfmyiFA700bQRD0i

Assumptions:

- You have both an 'input' and 'output' folder in your workspace
- The tag field is included in input csv data as 'tag'
- Hardcoded values for request to BC Route Planner
   > criteria=fastest
   > correctSide=false
   > distanceUnit=km
   > enable=gdf,ldf,tr,xc,tc

'''
# -------------------------------------------------------------------
# -------------------------------------------------------------------

# IMPORT MODULES

from datetime import datetime
import json
import pandas as pd
import platform
import random
import requests
import sys


# -------------------------------------------------------------------
# -------------------------------------------------------------------

# FUNCTIONS

# This function will attempt to calculate a route for each candidate
# address as a replacement for the previous unroutable address.
def isolation_check(fromPt, toPts, epsg_code):
    isolation_routePlanner = (
        f'https://router.api.gov.bc.ca/directions.json?'
        f'points={fromPt.strip()},{toPts.strip()}&criteria=fastest&outputSRS={epsg_code}'
        f'&enable=gdf,ldf,tr,xc,tc&correctSide=false&distanceUnit=km{route_planner_apikey.strip()}'
    )
    try:
        isolation_response = requests.get(isolation_routePlanner, timeout=10)
        isolation_response.raise_for_status()
        return isolation_response.json()
    except requests.exceptions.Timeout:
        print(f'Timeout occurred: fromPoint={fromPt}, toPoints={toPts}')
        return None
    except requests.exceptions.RequestException as e:
        print(f'Request error: {e} (fromPoint={fromPt}, toPoints={toPts})')
        return None

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

# The name of the tag field is assumed to be 'tag'
tag_column = 'tag'

# Check that the correct number of arguments were provided
if len(sys.argv) != 9:
    print(f'{len(sys.argv)} arguments were provided. 9 are required.')
    exit()

# Input arguments

# Your workspace filepath (including final slash)
wrkspc = sys.argv[1]

# CSV result file from proximity analysis script
proximity_result_file = sys.argv[2]

# Full address list (unsampled)
full_2M_address_file = sys.argv[3]

# administrative boundary ID field
admin_id_field = sys.argv[4]

# full address field from proximity analysis result file
address_field = sys.argv[5]

# Input address X coordinate field
address_x_coord_field = sys.argv[6]

# Input address Y coordinate field
address_y_coord_field = sys.argv[7]
tag_value = sys.argv[8]

# Your API key for the BC Route Planner (PROD)
apiKey = sys.argv[9]

# Build full file paths using input arguments
csv_proximity_result = wrkspc + 'input' + slash + proximity_result_file
csv_full_addresses = wrkspc + 'input' + slash + full_2M_address_file
csv_replacement_addresses = wrkspc + 'output' + slash + 'replacement_addresses_' + tag_value + '.csv'

# Variables used to assemble a web request
route_planner_apikey = f'&apikey={apiKey}'
headers = {'apikey': route_planner_apikey}

# Test destination coordinate
test_coord = '1401507,644858'


# -------------------------------------------------------------------
# -------------------------------------------------------------------

# PROCESSING

# Read CSVs using chunksize to manage memory better
chunk_size = 1000
df_route_not_found = pd.concat(pd.read_csv(csv_proximity_result, chunksize=chunk_size), ignore_index=True)
df_2M_addresses = pd.concat(pd.read_csv(csv_full_addresses, chunksize=chunk_size), ignore_index=True)

# Filter rows where 'tag' is null
df_route_not_found = df_route_not_found[df_route_not_found[tag_column].isna()]

# Print subset row count
print(f'Subset row count where the "{tag_column}" column is null: {len(df_route_not_found)}')

# Count cases per admin area
da_dict = df_route_not_found[admin_id_field].value_counts().to_dict()

# List for not routable admin areas
da_not_routable = []

# Temporary list to store replacement rows
replacement_rows = []

# Set to store already selected rows to avoid duplicates
selected_rows = set()

# Iterate through each admin area
for daid in da_dict.keys():
    current_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    print(f'[{current_time}] Finding replacement addresses for {admin_id_field} = {daid}')
    filtered_df_route_not_found = df_route_not_found[df_route_not_found[admin_id_field] == daid]
    filtered_df_2M_addresses = df_2M_addresses[df_2M_addresses[admin_id_field] == daid]

    if daid not in da_not_routable:
        for _, row in filtered_df_route_not_found.iterrows():
            non_matching_df = filtered_df_2M_addresses[filtered_df_2M_addresses[address_field] != row[address_field]]

            if not non_matching_df.empty:
                route_found = False
                attempts = 0  # Limit attempts to prevent infinite loops

                while not route_found and attempts < len(non_matching_df):
                    random_row = non_matching_df.sample(n=1).iloc[0]

                    # Check if this row was already selected
                    random_row_tuple = tuple(random_row)
                    if random_row_tuple in selected_rows:
                        attempts += 1
                        continue  # Skip if already selected

                    new_address_coords = f'{random_row[address_x_coord_field]},{random_row[address_y_coord_field]}'
                    router_response = isolation_check(new_address_coords, test_coord, '3005')

                    if router_response and router_response.get('routeFound'):
                        route_found = True
                        replacement_rows.append(random_row.to_dict())
                        if len(replacement_rows) % 50 == 0:
                            print('Replacement addresses validated: ' + str(len(replacement_rows)))

                        selected_rows.add(random_row_tuple)  # Mark as selected

                    attempts += 1

                if not route_found:
                    print(f'No valid route found for {daid}. Using original address.')
                    da_not_routable.append(daid)
                    break

        # Append remaining rows for the same daid without exceeding original count
        remaining_rows_same_daid = df_route_not_found[df_route_not_found[admin_id_field] == daid]
        max_replacements_for_daid = len(remaining_rows_same_daid)
        current_replacements_for_daid = len([row for row in replacement_rows if row[admin_id_field] == daid])

        for _, remaining_row in remaining_rows_same_daid.iterrows():
            remaining_row_tuple = tuple(remaining_row)
            if (remaining_row_tuple not in selected_rows and
                current_replacements_for_daid < max_replacements_for_daid):

                replacement_rows.append(remaining_row.to_dict())
                selected_rows.add(remaining_row_tuple)  # Mark as selected
                current_replacements_for_daid += 1  # Increment count for this DAID

            # Stop if the limit is reached
            if current_replacements_for_daid >= max_replacements_for_daid:
                print(f'Reached maximum replacements for {admin_id_field} = {daid}')
                break

# Convert replacement rows list to DataFrame
df_replacement_addresses = pd.DataFrame(replacement_rows)

# Print not routable areas
if da_not_routable:
    print('\nList of DAIDs that were not 100% routable:')
    for daid in da_not_routable:
        print(daid)
else:
    print('\nAll DAIDs were routable.')

# Export the new replacement addresses to a CSV
df_replacement_addresses.to_csv(csv_replacement_addresses, index=False)

# Print summaries
print(f'Length of dataframe with route not found cases: {len(df_route_not_found)}')
print(df_route_not_found['DAID'].value_counts())
print(f'Length of dataframe with route found cases: {len(df_replacement_addresses)}')
print(df_replacement_addresses['DAID'].value_counts())
