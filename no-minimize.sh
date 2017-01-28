#!/bin/bash
grep -rl 'Jar>true</min' ./ | xargs sed -i 's/Jar>true<\/min/Jar>false<\/min/g'
