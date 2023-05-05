#!/bin/bash

path=$1
hash=$2

cd $path
git checkout $hash
