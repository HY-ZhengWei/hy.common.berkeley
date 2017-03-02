#!/bin/sh

cd ./bin

rm -R ./org/hy/common/berkeley/junit

jar cvfm hy.common.berkeley.jar MANIFEST.MF LICENSE org

cp hy.common.berkeley.jar ..
rm hy.common.berkeley.jar
cd ..

