#!/bin/bash
find -type d -name target |xargs ls -lah| grep -v original |grep .jar
