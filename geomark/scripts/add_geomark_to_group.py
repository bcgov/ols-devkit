import hashlib
import hmac
import base64
import time
import requests
import urllib.parse

# Replace GEOMARK, GROUP and SECRET_KEY with actual values
URL_BASE = "https://test.apps.gov.bc.ca/pub/geomark/"
GEOMARK = "gm-..."
GROUP = "gg-..."
SECRET_KEY = "kg-..."

REQ_PATH = "/geomarkGroups/" + GROUP + "/geomarks/add"
TIMESTAMP = str(int(time.time() * 1000))

MESSAGE = REQ_PATH + ":" + TIMESTAMP + ":" + "geomarkId=" + GEOMARK

def url_encode(s):
    s1 = urllib.parse.quote(s)
    s2 = str.replace(s1, '_', '%2F')
    return str.replace(s2, '-', '%2B')

def sign(message, key):
    key = bytes(key, 'UTF-8')
    message = bytes(message, 'UTF-8')
    digester = hmac.new(key, message, hashlib.sha1)
    signature = digester.digest()
    signature64 = base64.urlsafe_b64encode(signature)
    return str(signature64, 'UTF-8')

if __name__ == '__main__':
    SIGNATURE = sign("/geomarkGroups/" + GROUP + "/geomarks/add:" + TIMESTAMP + ":geomarkId=" + GEOMARK, SECRET_KEY)
    SIGNATURE_ENCODED = url_encode(SIGNATURE)
    URL = URL_BASE + "geomarkGroups/" + GROUP + "/geomarks/add?geomarkId=" + GEOMARK + "&signature=" + SIGNATURE_ENCODED + "&time=" + TIMESTAMP
    response = requests.post(URL, headers = {'Accept': 'application/json'})
    print("response is: ", response.json())
