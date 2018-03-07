

cd .\bin


rd /s/q .\org\hy\common\berkeley\junit


jar cvfm hy.common.berkeley.jar MANIFEST.MF META-INF org

copy hy.common.berkeley.jar ..
del /q hy.common.berkeley.jar
cd ..





cd .\src
jar cvfm hy.common.berkeley-sources.jar MANIFEST.MF META-INF org 
copy hy.common.berkeley-sources.jar ..
del /q hy.common.berkeley-sources.jar
cd ..
