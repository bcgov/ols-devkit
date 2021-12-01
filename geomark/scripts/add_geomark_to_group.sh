#!/bin/bash

urlencode() {

local length="${#1}"

for (( i = 0; i < length; i++ )); do

local c="${1:i:1}"

case $c in

[a-zA-Z0-9.~_-]) printf "$c" ;;

*) printf '%%%02X' "'$c"

esac

done

}

# Replace GEOMARK, GROUP and SECRET_KEY with actual values
GEOMARK="gm-..."
GROUP="gg-..."
SECRET_KEY="kg-...""

PARAMS="geomarkId=${GEOMARK}"

REQ_PATH="/geomarkGroups/${GROUP}/geomarks/add"

TIMESTAMP=`date "+%s"`

MESSAGE="${REQ_PATH}:${TIMESTAMP}:geomarkId=${GEOMARK}"


SIGNATURE=`printf %s $MESSAGE | openssl dgst -hmac "$SECRET_KEY" -binary -sha1 | base64`

ENCODED_SIG=`urlencode $SIGNATURE`

curl -vvv -H "Accept: application/json" -X POST "https://test.apps.gov.bc.ca/pub/geomark/geomarkGroups/$GROUP/geomarks/add?geomarkId=$GEOMARK&signature=$ENCODED_SIG&time=$TIMESTAMP"
