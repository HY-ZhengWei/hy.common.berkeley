#!/bin/sh

mvn deploy:deploy-file -Dfile=hy.common.berkeley.jar                              -DpomFile=./src/META-INF/maven/org/hy/common/berkeley/pom.xml -DrepositoryId=thirdparty -Durl=http://HY-ZhengWei:1481/repository/thirdparty
mvn deploy:deploy-file -Dfile=hy.common.berkeley-sources.jar -Dclassifier=sources -DpomFile=./src/META-INF/maven/org/hy/common/berkeley/pom.xml -DrepositoryId=thirdparty -Durl=http://HY-ZhengWei:1481/repository/thirdparty
