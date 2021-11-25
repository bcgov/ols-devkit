#/!bin/sh
cd `dirname $0`
rm -rf geomark-war/target/geomark-war-*.war
mkdir -p geomark-docker-dev/target/
rm -rf geomark-docker-dev/target/pub#geomark.war

mvn -B install -DskipTests -Dmaven.source.skip -Dmaven.javadoc.skip=true

cd geomark-docker-dev/
mkdir -p target
cp ../geomark-war/target/geomark-war-*.war target/pub#geomark.war

docker-compose down
docker-compose up --build -d
