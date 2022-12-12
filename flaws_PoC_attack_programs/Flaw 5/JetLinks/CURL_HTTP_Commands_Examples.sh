#!/bin/bash
#----------------------------- JetLinks-----------------------------
# Publish smart bulb state.
curl -v -X POST --data '{"deviceId":"http_client_test", "success":"true", "properties": {"state":1}}'  http://server-address/report-property --header "Content-Type:application/json"

# Polling to pull messages (the URL of the protocol imported by the specific platform is different)
