#!/usr/bin/python

# The following Python code is an example of how to submit geocode
# requests to the synchronous bulk geocoding API using multiple
# python threads <MAX_WORKERS> to make simultaneous requests, with
# each request containing <GEOCODES_PER_REQUEST> addresses to geocode.


import concurrent.futures
import copy
import csv
import requests
import statistics
import sys
import threading
import time
import io

# Uncomment the appropriate SERVICE_URL for the target environment

# Dev
SERVICE_URL = 'https://ssl.refractions.net/ols/pub/geocoder/'
# Test
# SERVICE_URL = 'https://geocodertst.api.gov.bc.ca/'
# Production
#SERVICE_URL = 'https://geocoder.api.gov.bc.ca/'

REQUEST_URL = SERVICE_URL + 'addresses/bulk.csv'

# Setup parameters for job submission
headers = {'Accept': 'text/csv'}
global_params = {'outputSrs': '4326',
		  'interpolation': 'adaptive',
		  'locationDescriptor': 'any'}

GEOCODES_PER_REQUEST = 10
MAX_WORKERS = 4

fileHeader = 'yourId,addressString\n'

seq_num = 0
request_times = []

def log(msg):
	print((time.strftime("%Y/%m/%d|%H:%M:%S|") + msg))


# reads GEOCODES_PER_REQUEST rows of input, sends them in a geocode request, and writes the results
def geocode(dictReader, readLock, outFile, writeLock):
	global seq_num, request_times
	#log('running geocode...')
	buffer = io.StringIO()
	buffer.write(fileHeader)

	moreToRead = True
	num_read = 0
	#log('acquiring readLock...')
	with readLock:
		#log('acquired readLock...')
		start_seq = seq_num
		for i in range(GEOCODES_PER_REQUEST):
			#log('i = ' + str(i))
			try:
				row = next(dictReader)
				seq_num += 1
				num_read += 1
				if row['yourId']:
					buffer.write(','.join([row['yourId'], row['addressString']]) + '\n')
				else:
					buffer.write(','.join(['', row['addressString']]) + '\n')
			except StopIteration:
				moreToRead = False
				break
	
	# if there were no rows to read at all, there is no request to make, return now
	if len(buffer.getvalue()) == len(fileHeader):
		return False

	buffer.seek(0)
	files = {'file': buffer}
		
	request_start = time.time()

	log(f'Sending request for {start_seq}-{start_seq+num_read-1}')
	params = global_params.copy()
	params['startSeqNum'] = start_seq
	req = requests.post(REQUEST_URL, files=files, headers=headers, data=params)

	resultBuffer = io.StringIO(req.text)
	resultBuffer.seek(0)

	request_time = time.time() - request_start
	with writeLock:
		request_times.append(request_time)
		# if we aren't writing the first line, skip the header line
		if outFile.tell() != 0:
			resultBuffer.readline()
		outFile.write(resultBuffer.read())

	return moreToRead


# keeps geocoding until it runs out of input
def geocodeLoop(dictReader, readLock, outFile, writeLock):
	keepGoing = True
	while keepGoing:
		keepGoing = geocode(dictReader, readLock, outFile, writeLock)

	
def main(args):
	# Get parameters from the command-line
	if len(args) != 3:
		print(('Usage: <python> bulk_submitter.py ' +
			'<infile> <outfile>'))
		sys.exit(1)

	inFileName = args[1]
	outFileName = args[2]

	t0 = time.time()

	# open the input file
	csvFile = open(inFileName, mode='r', newline='')
	reader = csv.DictReader(csvFile)
	if 'addressString' not in reader.fieldnames:
		print('Input CSV must have an \'addressString\' column; columns found:')
		print(reader.fieldnames)
		sys.exit(1)

	readLock = threading.Lock()

	# open the output file
	outFile = open(outFileName, mode='w')
	writeLock = threading.Lock()

	print('Bulk Geocoding Submission')
	print(f'Input File name: {inFileName}')
	print(f'Worker Threads: {MAX_WORKERS}')
	print(f'Geocodes per Request: {GEOCODES_PER_REQUEST}')
	print(f'API URL: {REQUEST_URL}')
	print(f'Output File name: {outFileName}')

	# run MAX_WORKERS geocodeLoops in MAX_WORKERS threads until they all complete
	with concurrent.futures.ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
		for i in range(MAX_WORKERS):
			executor.submit(geocodeLoop, reader, readLock, outFile, writeLock)

	# clean up
	csvFile.close()
	outFile.close()

	t1 = time.time()
	print(f'Total Time taken: {t1-t0} seconds')
	print(f'Average Time per request {round(statistics.mean(request_times),3)} seconds')
	print(f'Max Time per request {round(max(request_times),3)} seconds')
	print(f'Min Time per request {round(min(request_times),3)} seconds')


if __name__ == '__main__':
	main(sys.argv)
