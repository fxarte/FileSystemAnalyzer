# File System Analyzer
Finds duplicate files and folders.
Update provided settings.properties with the proper values.
##Usage:
  watchDirectory: the folder to analyse
  commands: Optional, use to create commands to be run to process the items, otherwise will just generate a list
  logFolder= Optional, defaults to logs
  logFile=O ptional, defaults to output.log

##Requirements:
Java 1.8
###Libraries:
  + commons-io-2.4.jar
  + sqlite4java.jar
      * Windows:
          + sqlite4java-win32-x64.dll
          + sqlite4java-win32-x86.dll
      * unix/linux
          + libsqlite4java-linux-i386.so
          + libsqlite4java-linux-amd64.so
          + libsqlite4java-android-armv7.so
      * OSX
          + libsqlite4java-osx.jnilib
          + libsqlite4java-osx-10.4.jnilib
          + libsqlite4java-osx-ppc.jnilib
  + tika-app-1.4.jar
  
  
###run with:
java -Djava.library.path="libs" -jar "FileSystemAnalyserEclipse.jar" 


required libraries:
  commons-io-2.4.jar
  sqlite4java.jar
    Windows:
      sqlite4java-win32-x64.dll
      sqlite4java-win32-x86.dll
    unix/linux
      libsqlite4java-linux-i386.so
      libsqlite4java-linux-amd64.so
      libsqlite4java-android-armv7.so
    OSX
      libsqlite4java-osx.jnilib
      libsqlite4java-osx-10.4.jnilib
      libsqlite4java-osx-ppc.jnilib
  tika-app-1.4.jar
