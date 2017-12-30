#!/bin/sh

mvn deploy:deploy-file -Dfile=hy.common.berkeley.jar -DpomFile=./src/META-INF/maven/org/hy/common/berkeley/pom.xml -DrepositoryId=thirdparty -Durl=http://218.21.3.19:9015/nexus/content/repositories/thirdparty
