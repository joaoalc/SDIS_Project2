#!/bin/bash

mkdir -p ../src/build

javac -d ../src/build/ ../src/peers/*.java ../src/messages/*.java ../src/chordProtocol/*.java ../src/filesystem/*.java ../src/subProtocols/*.java -Xlint:unchecked
