

cd .\bin

rd /s/q .\org\hy\common\berkeley\junit

jar cvfm hy.common.berkeley.jar MANIFEST.MF LICENSE org

copy hy.common.berkeley.jar ..
del /q hy.common.berkeley.jar
cd ..
