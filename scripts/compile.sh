#!/bin/bash

mkdir -p ../src/build

javac -d ../src/build/ ../src/peers/*.java ../src/messages/*.java ../src/chordProtocol/*.java -Xlint:unchecked
