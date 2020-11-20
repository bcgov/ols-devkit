#!/usr/local/bin/python

# The following Python code is an example of how to use the CPF's web API
# to submit a batch of addresses to geocode.
# Notice: TLS 1.1 was enabled on October 12th, 2017. You may need to update
# your python libraries (version 2.7+) to run this script.

import csv
import json
import re
import requests
from requests.auth import HTTPDigestAuth
import sys
import time

# Uncomment the appropriate SERVICE_URL for the target environment

# Test
# SERVICE_URL = 'http://test.apps.gov.bc.ca/pub/cpf/ws/'
# Production
SERVICE_URL = 'http://apps.gov.bc.ca/pub/cpf/ws/'


def log(msg):
	print(time.strftime("%Y/%m/%d|%H:%M:%S|") + msg)


# Get parameters from the command-line
if len(sys.argv) < 5 or len(sys.argv) > 6:
	print('Usage: <python> address_list_submitter.py ' +
		  '<url|file> <local file> <username> <password> [<e-mail>]')
	sys.exit(0)

# Alternatively you can hard-code the parameters below
filename = sys.argv[1]
localFile = sys.argv[2]

username = sys.argv[3]
password = sys.argv[4]

# To receive email notification when job complete,
# type your email address below.
email = None
if len(sys.argv) == 6:
	email = sys.argv[5]

# Setup parameters for job submission
headers = {'Accept': 'application/json'}
fields = {'inputDataContentType': 'csv',
		  'resultSrid': '4326',
		  'interpolation': 'adaptive',
		  'locationDescriptor': 'any',
		  'resultDataContentType': 'csv',
		  'media': 'application/json'}
if email:
	fields['notificationEmail'] = email

files = None

m = re.search('^(http|ftp)s?://', filename)

if m:
	fields['inputDataUrl'] = filename
	files = {'foo': ('', 'bar')}
else:
	files = {'inputData': open(filename, 'rb')}

# Submit the job
url = SERVICE_URL + 'apps/geocoder/multiple.json'
log("Sending initial request to: " + url)
r1 = requests.post(url, files=files, headers=headers,
				   data=fields, auth=HTTPDigestAuth(username, password))
m = re.search('\/jobs\/(\d+)\/', r1.url)

if m:
	jobId = m.group(1)
else:
	jobStatus = json.loads(r1.text)
	jobId = jobStatus[u'id']
	m = re.search('\/jobs\/(\d+)\/', jobId)
	jobId = m.group(1)

# Poll the status URL for the job to determine when it is finished
url = SERVICE_URL + 'jobs/' + jobId + '.json'

# Maximum time to wait for job to complete, in seconds
MAX_WAIT_TIME = 60
startTime = time.clock()

while True:
	log("Checking status of job " + jobId + " at: " + url)
	r2 = requests.get(url, headers=headers,
					  auth=HTTPDigestAuth(username, password))
	jobStatus = json.loads(r2.text)
	if jobStatus[u'jobStatus'] == 'resultsCreated':
		break
	else:
		if time.clock() - startTime > MAX_WAIT_TIME:
			log("Maximum wait time (" + str(MAX_WAIT_TIME) + "s) exceeded")
			exit()

		waitSecs = float(jobStatus[u'secondsToWaitForStatusCheck'])
		if waitSecs is None or waitSecs < 1:
			waitSecs = 1
		if time.clock() - startTime + waitSecs > MAX_WAIT_TIME:
			waitSecs = MAX_WAIT_TIME - (time.clock() - startTime)
		log("Waiting for job to complete (%ds)" % waitSecs)
		time.sleep(waitSecs)

# Request the result information to determine the
# URL to retrieve the results from
url = SERVICE_URL + 'jobs/' + jobId + '/results.json'
log("Fetching result file url from: " + url)

r3 = requests.get(url, headers=headers,
				  auth=HTTPDigestAuth(username, password))
resultStatus = json.loads(r3.text)

try:
	resultUrl = resultStatus[u'resources'][0][u'resourceUri']
except KeyError:
	print('Geocoding failed on input file.')
	sys.exit(0)

# Download the file and save it locally
log("Fetching job results from: " + resultUrl)
r4 = requests.get(resultUrl, headers=headers,
				  auth=HTTPDigestAuth(username, password), stream=True)

with open(localFile, 'wb') as f:
	for chunk in r4.iter_content(chunk_size=1024):
		if chunk:
			f.write(chunk)
			f.flush()

# Process the file
log("Processing results from local file: " + localFile)
with open(localFile, 'r') as f:
	cr = csv.DictReader(f)
	count = 0
	totalScore = 0
	for row in cr:
		count += 1
		totalScore += float(row['score'])
log("Average Score: " + str(totalScore/count))