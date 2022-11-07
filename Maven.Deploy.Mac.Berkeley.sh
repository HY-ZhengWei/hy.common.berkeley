#!/bin/sh

mvn install:install-file -Dfile=hy.common.berkeley.jar                              -DpomFile=./src/main/resources/META-INF/maven/cn.openapis/hy.common.berkeley/pom.xml
mvn install:install-file -Dfile=hy.common.berkeley-sources.jar -Dclassifier=sources -DpomFile=./src/main/resources/META-INF/maven/cn.openapis/hy.common.berkeley/pom.xml

mvn deploy:deploy-file   -Dfile=hy.common.berkeley.jar                              -DpomFile=./src/main/resources/META-INF/maven/cn.openapis/hy.common.berkeley/pom.xml -DrepositoryId=thirdparty -Durl=http://HY-ZhengWei:8081/repository/thirdparty
mvn deploy:deploy-file   -Dfile=hy.common.berkeley-sources.jar -Dclassifier=sources -DpomFile=./src/main/resources/META-INF/maven/cn.openapis/hy.common.berkeley/pom.xml -DrepositoryId=thirdparty -Durl=http://HY-ZhengWei:8081/repository/thirdparty
