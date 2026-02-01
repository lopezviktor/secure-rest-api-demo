#!/usr/bin/env bash
set -euo pipefail

export METRICS_TOKEN_FILE="observability/prometheus/token.txt"

./mvnw spring-boot:run