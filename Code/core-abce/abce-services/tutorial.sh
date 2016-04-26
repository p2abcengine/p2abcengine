#!/bin/sh

#Stop script if an error occurs.
set -e
# Setup System Parameters.
echo "Setup System Parameters"
curl -X POST --header 'Content-Type: text/xml' 'http://localhost:9100/issuer/setupSystemParameters/?keyLength=1024' > systemparameters.xml

# Store credential specification at issuer.
echo "Store credential specification at issuer"
curl -X PUT --header 'Content-Type: text/xml' -d @tutorial-resources/credentialSpecificationVIPSoccerTicket.xml 'http://localhost:9100/issuer/storeCredentialSpecification/http%3A%2F%2FMyFavoriteSoccerTeam%2Ftickets%2Fvip' > storeCredentialSpecificationAtIssuerResponce.xml

# Store credential specification at user.
# This method is not specified in H2.2.
echo "Store credential specification at user"
curl -X PUT --header 'Content-Type: text/xml' -d @tutorial-resources/credentialSpecificationVIPSoccerTicket.xml 'http://localhost:9200/user/storeCredentialSpecification/http%3A%2F%2FMyFavoriteSoccerTeam%2Ftickets%2Fvip' > storeCredentialSpecificationAtUserResponce.xml

# Store credential specification at verifier.
# This method is not specified in H2.2.
echo "Store credential specification at verifier"
curl -X PUT --header 'Content-Type: text/xml' -d @tutorial-resources/credentialSpecificationVIPSoccerTicket.xml 'http://localhost:9300/verification/storeCredentialSpecification/http%3A%2F%2FMyFavoriteSoccerTeam%2Ftickets%2Fvip' > storeCredentialSpecificationAtVerifierResponce.xml

# Store System parameters at Revocation Authority.
# This method is not specified in H2.2.
echo "Store System parameters at Revocation Authority"
curl -X POST --header 'Content-Type: text/xml' -d @systemparameters.xml 'http://localhost:9500/revocation/storeSystemParameters/' > storeSystemParametersResponceAtRevocationAutority.xml

# Store System parameters at User.
# This method is not specified in H2.2.
echo "Store System parameters at User"
curl -X POST --header 'Content-Type: text/xml' -d @systemparameters.xml 'http://localhost:9200/user/storeSystemParameters/' > storeSystemParametersResponceAtUser.xml

# Store System parameters at verifier.
# This method is not specified in H2.2.
echo "Store System parameters at Verifier"
curl -X POST --header 'Content-Type: text/xml' -d @systemparameters.xml 'http://localhost:9300/verification/storeSystemParameters/' > storeSystemParametersResponceAtVerifier.xml

# Setup Revocation Authority Parameters.
echo "Setup Revocation Authority Parameters"
curl -X POST --header 'Content-Type: text/xml' -d @tutorial-resources/revocationReferences.xml 'http://localhost:9500/revocation/setupRevocationAuthorityParameters?keyLength=1024&uid=http%3A%2F%2Fticketcompany%2Frevocation' > revocationAuthorityParameters.xml

# Store Revocation Authority Parameters at issuer.
# This method is not specified in H2.2.
echo "Store Revocation Authority Parameters at issuer"
curl -X PUT --header 'Content-Type: text/xml' -d @revocationAuthorityParameters.xml 'http://localhost:9100/issuer/storeRevocationAuthorityParameters/http%3A%2F%2Fticketcompany%2Frevocation'  > storeRevocationAuthorityParameters.xml

# Store Revocation Authority Parameters at user.
# This method is not specified in H2.2.
echo "Store Revocation Authority Parameters at user"
curl -X PUT --header 'Content-Type: text/xml' -d @revocationAuthorityParameters.xml 'http://localhost:9200/user/storeRevocationAuthorityParameters/http%3A%2F%2Fticketcompany%2Frevocation'  > storeRevocationAuthorityParametersAtUserResponce.xml

# Store Revocation Authority Parameters at verifier.
# This method is not specified in H2.2.
echo "Store Revocation Authority Parameters at verifier"
curl -X PUT --header 'Content-Type: text/xml' -d @revocationAuthorityParameters.xml 'http://localhost:9300/verification/storeRevocationAuthorityParameters/http%3A%2F%2Fticketcompany%2Frevocation'  > storeRevocationAuthorityParametersAtVerifierResponce.xml

##

# Store System parameters at Inspector.
# This method is not specified in H2.2.
echo "Store System parameters at inspector"
curl -X POST --header 'Content-Type: text/xml' -d @systemparameters.xml 'http://localhost:9400/inspector/storeSystemParameters/' > storeSystemParametersResponceAtInspector.xml

# Store credential specification at Inspector.
# This method is not specified in H2.2.
echo "Store credential specification at inspector"
curl -X PUT --header 'Content-Type: text/xml' -d @tutorial-resources/credentialSpecificationVIPSoccerTicket.xml 'http://localhost:9400/inspector/storeCredentialSpecification/http%3A%2F%2FMyFavoriteSoccerTeam%2Ftickets%2Fvip' > storeCredentialSpecificationAtInspectorResponce.xml


# Generate Inspector Public Key
# This method is not specified in H2.2.
echo "Generating Inspector Public Key"
curl -X POST --header 'Content-Type: text/xml' 'http://localhost:9400/inspector/setupInspectorPublicKey?keyLength=1024&cryptoMechanism=idemix&uid=http%3A%2F%2Fticketcompany%2Finspection' > inspectorPublicKey.xml

# Store Inspector Public Key at user.
# This method is not specified in H2.2.
echo "Store Inspector Public Key at user"
curl -X PUT --header 'Content-Type: text/xml' -d @inspectorPublicKey.xml 'http://localhost:9200/user/storeInspectorPublicKey/http%3A%2F%2Fticketcompany%2Finspection'  > storeInspectorPublicKeyAtUserResponce.xml

# Store Inspector Public Key at verifier.
# This method is not specified in H2.2.
echo "Store Inspector Public Key at verifier"
curl -X PUT --header 'Content-Type: text/xml' -d @inspectorPublicKey.xml 'http://localhost:9300/verification/storeInspectorPublicKey/http%3A%2F%2Fticketcompany%2Finspection'  > storeInspectorPublicKeyAtVerifierResponce.xml


##

# Setup issuer parameters.
echo "Setup issuer parameters"
curl -X POST --header 'Content-Type: text/xml' -d @tutorial-resources/issuerParametersInput.xml 'http://localhost:9100/issuer/setupIssuerParameters/' > issuerParameters.xml


# Store Issuer Parameters at user.
# This method is not specified in H2.2.
echo "Store Issuer Parameters at user"
curl -X PUT --header 'Content-Type: text/xml' -d @issuerParameters.xml 'http://localhost:9200/user/storeIssuerParameters/http%3A%2F%2Fticketcompany%2FMyFavoriteSoccerTeam%2Fissuance%3Aidemix'  > storeIssuerParametersAtUser.xml

# Store Issuer Parameters at verifier.
# This method is not specified in H2.2.
echo "Store Issuer Parameters at verifier"
curl -X PUT --header 'Content-Type: text/xml' -d @issuerParameters.xml 'http://localhost:9300/verification/storeIssuerParameters/http%3A%2F%2Fticketcompany%2FMyFavoriteSoccerTeam%2Fissuance%3Aidemix'  > storeIssuerParametersAtVerifier.xml

# Create smartcard at user.
# This method is not specified in H2.2.
echo "Create smartcard at user"
curl -X POST --header 'Content-Type: text/xml' 'http://localhost:9200/user/createSmartcard/http%3A%2F%2Fticketcompany%2FMyFavoriteSoccerTeam%2Fissuance%3Aidemix'

# Init issuance protocol (first step for the issuer).
echo "Init issuance protocol"
curl -X POST --header 'Content-Type: text/xml' -d @tutorial-resources/issuancePolicyAndAttributes.xml 'http://localhost:9100/issuer/initIssuanceProtocol/' > issuanceMessageAndBoolean.xml

# Extract issuance message.
curl -X POST --header 'Content-Type: text/xml' -d @issuanceMessageAndBoolean.xml 'http://localhost:9200/user/extractIssuanceMessage/' > firstIssuanceMessage.xml

# First issuance protocol step (first step for the user).
echo "First issuance protocol step for the user"
curl -X POST --header 'Content-Type: text/xml' -d @firstIssuanceMessage.xml 'http://localhost:9200/user/issuanceProtocolStep/' > issuanceReturn.xml

echo "Select first usable identity"
curl -X POST --header 'Content-Type: text/xml' -d @issuanceReturn.xml 'http://localhost:9600/identity/issuance/' > uiIssuanceReturn.xml

# First issuance protocol step - UI (first step for the user).
echo "Second issuance protocol step (first step for the user)"
curl -X POST --header 'Content-Type: text/xml' -d @uiIssuanceReturn.xml 'http://localhost:9200/user/issuanceProtocolStepUi/' > secondIssuanceMessage.xml

# Second issuance protocol step (second step for the issuer).
echo "Second issuance protocol step (second step for the issuer)"
curl -X POST --header 'Content-Type: text/xml' -d @secondIssuanceMessage.xml 'http://localhost:9100/issuer/issuanceProtocolStep/' > thirdIssuanceMessageAndBoolean.xml

# Extract issuance message.
curl -X POST --header 'Content-Type: text/xml' -d @thirdIssuanceMessageAndBoolean.xml 'http://localhost:9200/user/extractIssuanceMessage/' > thirdIssuanceMessage.xml

# Third issuance protocol step (second step for the user).
echo "Third issuance protocol step (second step for the user)"
curl -X POST --header 'Content-Type: text/xml' -d @thirdIssuanceMessage.xml 'http://localhost:9200/user/issuanceProtocolStep/' > fourthIssuanceMessageAndBoolean.xml

# Create presentation policy alternatives.
# This method is not specified in H2.2.
echo "Create presentation policy alternatives"
curl -X GET --header 'Content-Type: text/xml' -d @tutorial-resources/presentationPolicyAlternatives.xml 'http://localhost:9300/verification/createPresentationPolicy?applicationData=testData' > presentationPolicyAlternatives.xml

# Create presentation UI return.
# This method is not specified in H2.2.
echo "Create presentation UI return"
curl -X POST --header 'Content-Type: text/xml' -d @presentationPolicyAlternatives.xml 'http://localhost:9200/user/createPresentationToken/' > uiPresentationArguments.xml

echo "Select first posisble identity"
curl -X POST --header 'Content-Type: text/xml' -d @uiPresentationArguments.xml 'http://localhost:9600/identity/presentation' > uiPresentationReturn.xml
  
# Create presentation token.
# This method is not specified in H2.2.
echo "Create presentation token"
curl -X POST --header 'Content-Type: text/xml' -d @uiPresentationReturn.xml 'http://localhost:9200/user/createPresentationTokenUi/' > presentationToken.xml

# Setup presentationPolicyAlternativesAndPresentationToken.xml.

presentationPolicy=`cat presentationPolicyAlternatives.xml | sed 's@<?xml version[^>]*?>@@' | sed 's/^.*<abc:PresentationPolicyAlternatives xmlns:xs="http:\/\/www.w3.org\/2001\/XMLSchema" xmlns:abc="http:\/\/abc4trust.eu\/wp2\/abcschemav1.0" xmlns:idmx="http:\/\/zurich.ibm.com" xmlns:xsi="http:\/\/www.w3.org\/2001\/XMLSchema-instance" Version="1.0">//'`

presentationToken=`cat presentationToken.xml | sed 's@<?xml version[^>]*?>@@' | sed 's/^.*<abc:PresentationToken xmlns:xs="http:\/\/www.w3.org\/2001\/XMLSchema" xmlns:abc="http:\/\/abc4trust.eu\/wp2\/abcschemav1.0" xmlns:idmx="http:\/\/zurich.ibm.com" xmlns:xsi="http:\/\/www.w3.org\/2001\/XMLSchema-instance" Version="3.0.0">//'`
# echo "${presentationPolicy}"
# echo "${presentationToken}"
echo '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' > presentationPolicyAlternativesAndPresentationToken.xml
echo '<abc:PresentationPolicyAlternativesAndPresentationToken xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:abc="http://abc4trust.eu/wp2/abcschemav1.0" Version="1.0">' >> presentationPolicyAlternativesAndPresentationToken.xml
echo "${presentationPolicy}" >> presentationPolicyAlternativesAndPresentationToken.xml
#echo '</PresentationPolicyAlternatives>' >> presentationPolicyAlternativesAndPresentationToken.xml
#echo '<abc:PresentationToken>' >> presentationPolicyAlternativesAndPresentationToken.xml
echo "${presentationToken}" >> presentationPolicyAlternativesAndPresentationToken.xml
#echo '</PresentationToken>' >> presentationPolicyAlternativesAndPresentationToken.xml
echo '</abc:PresentationPolicyAlternativesAndPresentationToken>' >> presentationPolicyAlternativesAndPresentationToken.xml
  
# Verify presentation token against presentation policy.
echo "Verify presentation token against presentation policy"
# This method is not specified in H2.2.
curl -X POST --header 'Content-Type: text/xml' -d @presentationPolicyAlternativesAndPresentationToken.xml 'http://localhost:9300/verification/verifyTokenAgainstPolicy/' > presentationTokenDescription.xml

#
# Inspect presentation token.
echo "Inspect presentation token"
curl -X POST --header 'Content-Type: text/xml' -d @presentationToken.xml 'http://localhost:9400/inspector/inspect/' > inspectResult.xml


#
# Find revocation handle and revoke the credential.
echo "Constructing revocation request"
revocationhandle=`cat fourthIssuanceMessageAndBoolean.xml | sed 's/^.*revocationhandle" DataType="xs:integer" Encoding="urn:abc4trust:1.0:encoding:integer:unsigned"\/><abc:AttributeValue xsi:type="xs:integer">//' | sed 's/<.*//'`
echo "${revocationhandle}"

echo '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<abc:AttributeList xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:abc="http://abc4trust.eu/wp2/abcschemav1.0" 
    xmlns:idmx="http://zurich.ibm.com"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <abc:Attributes>
      <abc:AttributeUID>urn:abc4trust:1.0:attribute/NOT_USED</abc:AttributeUID>
      <abc:AttributeDescription Type="http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"
           DataType="xs:integer" 
           Encoding="urn:abc4trust:1.0:encoding:integer:unsigned"/>
      <abc:AttributeValue xsi:type="xs:integer">' >> revocationAttributeList.xml
echo "${revocationhandle}" >> revocationAttributeList.xml
echo '</abc:AttributeValue></abc:Attributes></abc:AttributeList>' >> revocationAttributeList.xml

echo "Calling revocation authority"
curl -X POST --header 'Content-Type: text/xml' -d @revocationAttributeList.xml 'http://localhost:9500/revocation/revoke/http%3A%2F%2Fticketcompany%2Frevocation' > revokeReply.xml

#
# Check if the previous policy can be satisfied after revocation. Should return false.

echo "Checking if policy can be satisfied."
curl -X POST --header 'Content-Type: text/xml' -d @presentationPolicyAlternatives.xml 'http://localhost:9200/user/canBeSatisfied/' > canBeSatisfied.xml






