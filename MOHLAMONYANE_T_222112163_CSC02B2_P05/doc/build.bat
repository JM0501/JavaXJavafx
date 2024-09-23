REM Mr. T. MOHLAMONYANE
REM Computer Science 2B 2024


REM Turn echo off and clear the screen.
@echo off
cls

REM Set java compile version
set JAVA_HOME=C:\jdk-21
set PATH=%JAVA_HOME%\BIN;%PATH%;

REM Set JavaFX SDK path
set JAVAFX_LIB=C:\Users\User\Downloads\openjfx-22.0.1_windows-x64_bin-sdk\javafx-sdk-22.0.1\lib

REM Check JDK version
javac -version

REM Go to the parent folder
cd..

REM Setting source and bin folder variable
set PRAC_BIN=.\bin
set PRAC_SRC=.\src
set PRAC_DOCS=.\docs

REM Clean all files in the folder
del %PRAC_BIN%\*.class

REM Compile code
echo Compiling
javac -sourcepath %PRAC_SRC% -cp "%JAVAFX_LIB%\*" -d %PRAC_BIN% %PRAC_SRC%\application\Main.java
echo compiled

REM Run the program
echo Running
java -cp "%PRAC_BIN%;%JAVAFX_LIB%\*" --module-path %JAVAFX_LIB% --add-modules javafx.controls,javafx.fxml application.Main

pause
