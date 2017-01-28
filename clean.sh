#!/bin/bash
find -type f -name dependency-reduced-pom.xml |xargs rm -rdf
find -type d -name target |xargs rm -rdf
find -type f -name original\*.jar |xargs rm -rf
find -type f -name \*.iml |xargs rm -rf
