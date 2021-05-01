#!/bin/bash

mkdir -p ../src/build

javac -d ../src/build/ ../src/peers/*.java -Xlint:unchecked
