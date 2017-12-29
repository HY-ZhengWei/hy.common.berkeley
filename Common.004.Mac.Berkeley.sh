#!/bin/sh

cd ./bin

rm -R ./org/hy/common/berkeley/junit

jar cvfm hy.common.berkeley.jar MANIFEST.MF META-INF org

cp hy.common.berkeley.jar ..
rm hy.common.berkeley.jar
cd ..

