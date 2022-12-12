#!/bin/bash
#----------------------------- ThingsBoard -----------------------------
# Publish smart bulb state.
curl -v -X POST --data '{"state":1}' http://$THINGSBOARD_HOST_NAME/api/v1/$ACCESS_TOKEN/telemetry --header "Content-Type:application/json"

# Subscribe to RPC commands from the cloud, send GET request with optional 'timeout' request parameter 
curl -v -X GET http://$THINGSBOARD_HOST_NAME/api/v1/$ACCESS_TOKEN/rpc?timeout=20000


#----------------------------- Kaa Enterprise-----------------------------
# Publish smart bulb state.
curl --location --request POST 'https://connect.cloud.kaaiot.com:443/kp1/<app-version-name>/dcx/<endpoint-token>/json' \
--data-raw '{
  "state": 1
}'

# Polling to obtain commands from the cloud platform (take switch-light as an example)
curl --location --request POST 'https://connect.cloud.kaaiot.com:443/kp1/<app-version-name>/cex/<endpoint-token>/command/switch-light' \
--data-raw '{}'
