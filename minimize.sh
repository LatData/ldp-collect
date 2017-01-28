#!/bin/bash
grep -rl 'Jar>true</min' ./ | xargs sed -i 's/Jar>false<\/min/Jar>true<\/min/g'
