#!/bin/sh

#Stop script if an error occurs
set -e

rm -rf target/user_*
rm -rf tmp/issuer/issuer_*
rm -rf tmp/verifier/abce-services
rm -rf tmp/revocation/revocation_storage
rm -rf tmp/user/user_storage
rm -rf tmp/user/abce-services
rm -rf tmp/*.xml

mkdir -p tmp
mkdir -p tmp/issuer
mkdir -p tmp/user
mkdir -p tmp/verifier
mkdir -p tmp/inspector
mkdir -p tmp/revocation
mkdir -p tmp/identity

cp -R tutorial-resources tmp/

cp target/selfcontained-issuance-service.war tmp/issuer
cp target/selfcontained-user-service.war tmp/user
cp target/selfcontained-verification-service.war tmp/verifier
cp target/selfcontained-inspection-service.war tmp/inspector
cp target/selfcontained-revocation-service.war tmp/revocation
cp target/selfcontained-identity-service.war tmp/identity
cp tutorial.sh tmp/
