#!/bin/bash

now=$(date)
echo -e $now:BEGIN CLOC EXTRACTION \\n

repository=$1
#path=$2
current=${PWD}


cd $repository
if [ -f "linguistfiles.log" ];
then
  echo "linguistfiles.log ok"
  while IFS=";" read language path
  	do	
  		linesinfo=$(cloc --sum-one $path | awk '$1 == "SUM:" {printf "%d;%d\n", $4,$5 }  
                                              $1 == "1" && $3 == "ignored." {printf ";\n"}') 
  		#linesinfo=$(cloc --sum-one $path | awk '{if($1 == "SUM:"){printf "%d;%d\n", $4,$5 }  
      #                                         if($3 == "ignored.") {printf "-;\n"}}')
  		echo "$path;$linesinfo"
  	done < linguistfiles.log > cloc_info.log
else
  echo "linguistfiles.log not exist. Processing by using filelist.log"
  while IFS=";" read path
  	do	
  		linesinfo=$(cloc --sum-one $path | awk '$1 == "SUM:" {printf "%d;%d\n", $4,$5 }  
                                              $1 == "1" && $3 == "ignored." {printf ";\n"}') 
  		#linesinfo=$(cloc --sum-one $path | awk '{if($1 == "SUM:"){printf "%d;%d\n", $4,$5 }  
      #                                         if($3 == "ignored.") {printf "-;\n"}}')
  		echo "$path;$linesinfo"
  	done < filelist.log > cloc_info.log
fi
    
cd $current

now=$(date)
echo -e $now:END CLOC EXTRACTION \\n