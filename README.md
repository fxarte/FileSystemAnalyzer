# File System Analyzer
Goes through all the files and subfolders of the provided starting folder, extracts information and generate reports. 
Currently supported:
+ Duplicates: files and folders.

##Usage:
Update provided settings.properties with the proper values:

**Available options:**

* `watchDirectory`: Folder to analyze, value: valid path to directory
* `recurse`: whether to go deeper that the provided folder, recommended value: yes
* `commandsRowOperations`: When the report is build, what command line associate the paths found
* `skipProcessed`: <True|False> whether to ignore or not items already analyzed
* `commandsOperationSkipRow`: <First|Last> whether to leave the last|first row on each group without command operation
* `showBiggestItems`: <integer> how many items to show in the report

### Run:

```
#!shell

java -Djava.library.path="libs" -jar "FileSystemAnalyserEclipse.jar" 

```


##Requirements:
Java 1.8
