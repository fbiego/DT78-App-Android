@echo off

echo Compiling...
call kotlinc translations.kt -include-runtime -d translate.jar

echo Exporting files...
call java -jar translate.jar "strings.txt"

echo Complete
pause