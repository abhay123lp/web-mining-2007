#!/bin/sh
WEBAPP_DIR=${HOME}/opt/apps/tomcat/webapps
pushd .
ant clean jar
cd WebApp
ant clean war
cd build
cp -p mem.war ${WEBAPP_DIR}/
cd ${WEBAPP_DIR}
rm -rf mem
popd
