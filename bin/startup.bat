@echo off
setlocal
set BATCH_HOME=..\
set LIB_HOME=%BATCH_HOME%lib\

set JAVA_HOME=C:\PROGRA~1\Java\jdk1.7.0_80\jre
set JAVACMD="%JAVA_HOME%\bin\java"

set CLASSPATH="%BATCH_HOME%\classes"
set CLASSPATH=%CLASSPATH%;"%LIB_HOME%commons-beanutils-1.8.3.jar"
set CLASSPATH=%CLASSPATH%;"%LIB_HOME%commons-codec-1.9.jar"
set CLASSPATH=%CLASSPATH%;"%LIB_HOME%commons-collections-3.2.1.jar"
set CLASSPATH=%CLASSPATH%;"%LIB_HOME%commons-digester-1.8.jar"
set CLASSPATH=%CLASSPATH%;"%LIB_HOME%commons-io-2.2.jar"
set CLASSPATH=%CLASSPATH%;"%LIB_HOME%commons-lang3-3.3.2.jar"
set CLASSPATH=%CLASSPATH%;"%LIB_HOME%commons-logging-1.2.jar"
set CLASSPATH=%CLASSPATH%;"%LIB_HOME%kwic-support.jar"
set CLASSPATH=%CLASSPATH%;"%LIB_HOME%scpdb.jar"

%JAVACMD% -Xms512m -Xmx512m -classpath %CLASSPATH% -Dfile.encoding=EUC-KR hanacard.batch.Startup
endlocal
