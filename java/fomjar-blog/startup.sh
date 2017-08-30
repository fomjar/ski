#!/bin/sh

java -server -jar fomjar-blog*.jar 2>&1 | tee output.log &

