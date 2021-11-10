#!/bin/bash
set -e

RESOURCES=./src/test/resources

if diff <(java -jar ./build/libs/trace-query-1.0-SNAPSHOT.jar < $RESOURCES/test-input.txt) \
    $RESOURCES/test-output.txt
then
  echo "OK"
else
  echo "Test failed"
fi
