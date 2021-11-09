#!/bin/bash
set -e

RESOURCES=./src/test/resources

if diff <(cat $RESOURCES/test-input.txt \
      | java -jar ./build/libs/trace-query-1.0-SNAPSHOT.jar) \
    $RESOURCES/test-output.txt
then
  echo "OK"
else
  echo "Test failed"
fi
