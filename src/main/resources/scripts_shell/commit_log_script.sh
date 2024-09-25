#!/bin/bash

path=$1
currentpath=${PWD}
now=$(date)
echo -e $now: BEGIN COMMIT EXTRACTION \\n 

cd $path

git config diff.renameLimit 999999 

#Extract commit information
git log --pretty=format:"%H;%aN;%aE;%at;%s" --no-merges > commitinfo.log

#Extract and format commit files information
git log --name-status --pretty=format:"commit	%H" --find-renames --no-merges > log.log
#awk -F$'\t' -f $currentpath/log.awk log.log > commitfileinfo.log

#Get current file list
git ls-files > filelist.log

git log --numstat --pretty=format:"commit %H" --no-merges > diff.log

#Remove temp file
#rm log.log

git config --unset diff.renameLimit


now=$(date)
echo -e "Log files (commitinfo.log, commitfileinfo.log and filelist.log) were generated in $path folder:  \\n"
echo -e $now: END COMMIT EXTRACTION \\n 
