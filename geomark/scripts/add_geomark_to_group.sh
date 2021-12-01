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


GEOMARK=gm-E0B5CA985D6A48A39F2908E45CFC01D2

GROUP=gg-A14A786B6D2A4639E04400144FA876C2

PARAMS="geomarkId=${GEOMARK}"

SECRET_KEY=kg-D0661306F41C44FA8D98DA1CB12B88BB

REQ_PATH="/geomarkGroups/${GROUP}/geomarks/add"

TIMESTAMP=`date "+%s"`

MESSAGE="${REQ_PATH}:${TIMESTAMP}:geomarkId=${GEOMARK}"


SIGNATURE=`printf %s $MESSAGE | openssl dgst -hmac "$SECRET_KEY" -binary -sha1 | base64`

ENCODED_SIG=`urlencode $SIGNATURE`

curl -vvv -H "Accept: application/json" -X POST "https://test.apps.gov.bc.ca/pub/geomark/geomarkGroups/$GROUP/geomarks/add?geomarkId=$GEOMARK&signature=$ENCODED_SIG&time=$TIMESTAMP"
