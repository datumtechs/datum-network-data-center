#!/bin/bash
PROFILE=$1
ps -ef|grep -v grep|grep metis-storage-2.0-SNAPSHOT|grep active=$PROFILE|awk '{print $2}'|xargs kill -9;
echo 'Process List:'
ps -elf|grep metis-storage-2.0-SNAPSHOT