#!/usr/bin/env bash

set -euo pipefail

BROKER_BASE_URL="${BROKER_BASE_URL:-http://localhost:9090}"
PROTECTED_API_BASE_URL="${PROTECTED_API_BASE_URL:-http://localhost:8082}"
CLIENT_ID="${CLIENT_ID:-protected-test-api-hypermedia-client}"
CLIENT_SECRET="${CLIENT_SECRET:-protected-test-api-secret}"
SCOPE="${SCOPE:-hypermedia.access}"
MAX_TOKEN_ATTEMPTS="${MAX_TOKEN_ATTEMPTS:-30}"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1"
    exit 1
  fi
}

step() {
  echo
  echo "============================================================"
  echo "$1"
  echo "============================================================"
}

press_enter() {
  read -r -p "Press Enter to continue..."
}

extract_json_value() {
  local json="$1"
  local filter="$2"
  echo "$json" | jq -er "$filter"
}

require_cmd http
require_cmd jq

step "Step 0: Preconditions"
echo "This script does NOT start apps."
echo "Ensure these are already running:"
echo "  - auth-server at $BROKER_BASE_URL"
echo "  - protected-test-api at $PROTECTED_API_BASE_URL"
press_enter

step "Step 1: Inspect protected API root"
http GET "$PROTECTED_API_BASE_URL/" Accept:application/prs.hal-forms+json
press_enter

step "Step 2: Inspect auth server metadata"
http GET "$BROKER_BASE_URL/.well-known/openid-configuration"
press_enter

step "Step 3: Start device authorization"
DEVICE_JSON="$(http --pretty=none --body --form POST "$BROKER_BASE_URL/oauth2/device_authorization" \
  client_id="$CLIENT_ID" \
  client_secret="$CLIENT_SECRET" \
  scope="$SCOPE")"
echo "$DEVICE_JSON" | jq .

DEVICE_CODE="$(extract_json_value "$DEVICE_JSON" '.device_code')"
USER_CODE="$(extract_json_value "$DEVICE_JSON" '.user_code')"
VERIFICATION_URI="$(extract_json_value "$DEVICE_JSON" '.verification_uri')"
VERIFICATION_URI_COMPLETE="$(echo "$DEVICE_JSON" | jq -r '.verification_uri_complete // empty')"
INTERVAL="$(echo "$DEVICE_JSON" | jq -r '.interval // 5')"

echo
echo "Device authorization values:"
echo "  DEVICE_CODE=$DEVICE_CODE"
echo "  USER_CODE=$USER_CODE"
echo "  VERIFICATION_URI=$VERIFICATION_URI"
echo "  VERIFICATION_URI_COMPLETE=$VERIFICATION_URI_COMPLETE"
echo "  INTERVAL=$INTERVAL"
press_enter

step "Step 4: Browser verification and login"
if [[ -n "$VERIFICATION_URI_COMPLETE" ]]; then
  echo "Open this URL in browser:"
  echo "  $VERIFICATION_URI_COMPLETE"
else
  echo "Open this URL in browser and enter USER_CODE:"
  echo "  $VERIFICATION_URI"
  echo "  USER_CODE=$USER_CODE"
fi
echo "Login with your auth-server credentials and approve consent."
press_enter

step "Step 5: Poll token endpoint"
ACCESS_TOKEN=""
attempt=1
poll_interval="$INTERVAL"

while [[ "$attempt" -le "$MAX_TOKEN_ATTEMPTS" ]]; do
  echo
  echo "Polling attempt $attempt/$MAX_TOKEN_ATTEMPTS ..."

  TOKEN_JSON="$(
    http --ignore-stdin --pretty=none --body --form POST "$BROKER_BASE_URL/oauth2/token" \
      grant_type='urn:ietf:params:oauth:grant-type:device_code' \
      device_code="$DEVICE_CODE" \
      client_id="$CLIENT_ID" \
      client_secret="$CLIENT_SECRET" 2>/dev/null || true
  )"

  if [[ -z "$TOKEN_JSON" ]]; then
    echo "No response body from token endpoint."
  else
    echo "$TOKEN_JSON" | jq .
  fi

  ACCESS_TOKEN="$(echo "$TOKEN_JSON" | jq -r '.access_token // empty' 2>/dev/null || true)"
  if [[ -n "$ACCESS_TOKEN" ]]; then
    echo
    echo "Access token obtained."
    break
  fi

  error_code="$(echo "$TOKEN_JSON" | jq -r '.error // empty' 2>/dev/null || true)"
  if [[ "$error_code" == "slow_down" ]]; then
    poll_interval=$((poll_interval + 5))
    echo "Received slow_down. New polling interval: ${poll_interval}s"
  fi
  if [[ "$error_code" == "authorization_pending" ]]; then
    echo "Authorization still pending."
  fi
  if [[ "$error_code" == "access_denied" || "$error_code" == "expired_token" || "$error_code" == "invalid_grant" ]]; then
    echo "Device flow ended with error: $error_code"
    break
  fi

  echo "Waiting ${poll_interval}s before next poll."
  press_enter
  attempt=$((attempt + 1))
  sleep "$poll_interval"
done

if [[ -z "$ACCESS_TOKEN" ]]; then
  echo
  echo "No access token obtained."
  echo "If needed, restart from Step 3 with a new device authorization request."
  exit 1
fi
press_enter

step "Step 6: Call protected endpoint with token"
http GET "$PROTECTED_API_BASE_URL/protected" "Authorization:Bearer $ACCESS_TOKEN"
press_enter

step "Step 7: Negative test (no token)"
http GET "$PROTECTED_API_BASE_URL/protected" || true

echo
echo "Manual device flow walkthrough complete."
