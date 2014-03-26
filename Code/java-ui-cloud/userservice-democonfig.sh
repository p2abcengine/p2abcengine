#!/bin/sh

RESOURCES="eu.abc4trust.ri.ui.user/src/main/resources/fiware-demo"

#Stop script if an error occurs.
set -e

# Store credential specification at user.
echo "Store credential specification at user"
curl -X PUT --header 'Content-Type: text/xml' -d @$RESOURCES/credentialSpecificationFiwareUser.xml 'http://localhost:9200/user/storeCredentialSpecification/urn%3Afiware%3Acredspec%3AcredIdm' > storeCredentialSpecificationAtUserResponse.xml

# Store System parameters at User.
echo "Store System parameters at User"
curl -X POST --header 'Content-Type: text/xml' -d @$RESOURCES/system_params_bridged.xml 'http://localhost:9200/user/storeSystemParameters/' > storeSystemParametersResponceAtUser.xml

# Store Issuer Parameters at user.
echo "Store Issuer Parameters at user"
curl -X PUT --header 'Content-Type: text/xml' -d @$RESOURCES/issuer_params_nsn_correct.xml 'http://localhost:9200/user/storeIssuerParameters/urn%3Anokiasiemensnetworks%3AcredIdm%3Aidemix' > storeIssuerParametersAtUser.xml

# Create smartcard at user.
echo "Create smartcard at user"
curl -X POST --header 'Content-Type: text/xml' 'http://localhost:9200/user/createSmartcard/urn%3Anokiasiemensnetworks%3AcredIdm%3Aidemix'
