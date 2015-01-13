#!/bin/sh

export MAVEN_OPTS='-Xmx2024m -Xms256m -XX:MaxPermSize=1024m'

#Stop script if an error occurs
set -e

echo "Running: mvn -P issuance-service clean install -DskipTests"
mvn -P issuance-service clean install -DskipTests

echo "Running: mvn -P selfcontained-issuance-service install -DskipTests"
mvn -P selfcontained-issuance-service install -DskipTests

echo "Running: mvn -P user-service install -DskipTests"
mvn -P user-service install -DskipTests

echo "Running: mvn -P selfcontained-user-service install -DskipTests"
mvn -P selfcontained-user-service install -DskipTests

echo "Running: mvn -P verification-service clean install -DskipTests"
mvn -P verification-service install -DskipTests

echo "Running: mvn -P selfcontained-verification-service clean install -DskipTests"
mvn -P selfcontained-verification-service install -DskipTests

echo "Running: mvn -P inspection-service clean install -DskipTests"
mvn -P inspection-service install -DskipTests

echo "Running: mvn -P selfcontained-inspection-service clean install -DskipTests"
mvn -P selfcontained-inspection-service install -DskipTests

echo "Running: mvn -P revocation-service clean install -DskipTests"
mvn -P revocation-service install -DskipTests

echo "Running: mvn -P selfcontained-revocation-service clean install -DskipTests"
mvn -P selfcontained-revocation-service install -DskipTests

echo "Running: mvn -P identity-service install -DskipTests"
mvn -P identity-service install -DskipTests

echo "Running: mvn -P selfcontained-identity-serivce install -DskipTests"
mvn -P selfcontained-identity-service install -DskipTests
