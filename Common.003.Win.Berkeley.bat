

del /Q hy.common.berkeley.jar
del /Q hy.common.berkeley-sources.jar


call mvn clean package
cd .\target\classes

rd /s/q .\org\hy\common\berkeley\junit


jar cvfm hy.common.berkeley.jar META-INF/MANIFEST.MF META-INF org

copy hy.common.berkeley.jar ..\..
del /q hy.common.berkeley.jar
cd ..\..





cd .\src\main\java
xcopy /S ..\resources\* .
jar cvfm hy.common.berkeley-sources.jar META-INF\MANIFEST.MF META-INF org 
copy hy.common.berkeley-sources.jar ..\..\..
del /Q hy.common.berkeley-sources.jar
rd /s/q META-INF
cd ..\..\..

pause