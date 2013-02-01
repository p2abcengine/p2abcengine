//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.smartcardManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

import com.ibm.zurich.idmx.api.RecipientInterface;
import com.ibm.zurich.idmx.dm.Credential;
import com.ibm.zurich.idmx.dm.CredentialCommitment;
import com.ibm.zurich.idmx.dm.StoredDomainPseudonym;
import com.ibm.zurich.idmx.dm.StoredPseudonym;
import com.ibm.zurich.idmx.dm.Values;
import com.ibm.zurich.idmx.dm.structure.CredentialStructure;
import com.ibm.zurich.idmx.issuance.AdvancedIssuanceSpec;
import com.ibm.zurich.idmx.issuance.AdvancedIssuer;
import com.ibm.zurich.idmx.issuance.AdvancedRecipient;
import com.ibm.zurich.idmx.issuance.IssuanceSpec;
import com.ibm.zurich.idmx.issuance.Issuer;
import com.ibm.zurich.idmx.issuance.Message;
import com.ibm.zurich.idmx.issuance.Recipient;
import com.ibm.zurich.idmx.key.IssuerKeyPair;
import com.ibm.zurich.idmx.key.IssuerPublicKey;
import com.ibm.zurich.idmx.showproof.Proof;
import com.ibm.zurich.idmx.showproof.ProofSpec;
import com.ibm.zurich.idmx.showproof.Prover;
import com.ibm.zurich.idmx.showproof.ProverInput;
import com.ibm.zurich.idmx.showproof.Verifier;
import com.ibm.zurich.idmx.showproof.VerifierInput;
import com.ibm.zurich.idmx.smartcard.IdemixSmartcardHelper;
import com.ibm.zurich.idmx.utils.GroupParameters;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;
import com.ibm.zurich.idmx.utils.SystemParameters;
import com.ibm.zurich.idmx.utils.XMLSerializer;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcard.CredentialBases;
import eu.abc4trust.smartcard.RSAKeyPair;
import eu.abc4trust.smartcard.RSASignatureSystemTest;
import eu.abc4trust.smartcard.Smartcard;
import eu.abc4trust.smartcard.SmartcardBlob;
import eu.abc4trust.smartcard.SmartcardStatusCode;
import eu.abc4trust.smartcard.SoftwareSmartcard;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.util.XmlUtils;


@SuppressWarnings("deprecation")
public class SmartcardManagerTest {

    private IssuerKeyPair ik = null;
    private GroupParameters gp = null;
    private static final int pin = 1243;
    private static final URI issuerUri = URI.create("issuer");
    private static final URI nameOfSmartcard = URI.create("theUltimateSmartcard");
    private static final URI nameOfSecret = URI.create("secret://sample-1234");
    private static final URI credNameOnSmartcard = URI.create("MyCredential");

    private static final short deviceID = 42;

    public static final BigInteger ATT_VALUE_4 = BigInteger.valueOf(1313);
    public static final BigInteger ATT_VALUE_3 = BigInteger.valueOf(1314);
    public static final BigInteger ATT_VALUE_2 = BigInteger.valueOf(1315);
    public static final BigInteger ATT_VALUE_1 = BigInteger.valueOf(1316);
    public static final BigInteger ATT_VALUE_5 = BigInteger.valueOf(1317);
    public static final BigInteger ATT_VALUE_6 = BigInteger.valueOf(1318);
    private static final String CRED_STRUCT_WITH_SMARTCARD = "http://CredStructWithSmartcard.xml";
    private static final String CRED_STRUCT_WITH_SMARTCARD_IV = "http://CredStructScIV.xml";
    private static final String CRED_STRUCT_WITH_SMARTCARD_IV_NOUV = "http://CredStructScIVNoUV.xml";
    private static final String ISSUER_PK_PARAMS = "http://www.issuer.com/ipk.xml";
    private static final URI ipkId = URI.create(ISSUER_PK_PARAMS);

    @Before
    public void setUp() {
        IssuerPublicKey ipk = (IssuerPublicKey) Parser.getInstance().parse(this.getResource("ipk.xml"));
        StructureStore.getInstance().add(ISSUER_PK_PARAMS, ipk);
        this.ik = (IssuerKeyPair) Parser.getInstance().parse(this.getResource("isk.xml"));
        SystemParameters sp = (SystemParameters) Parser.getInstance().parse(this.getResource("sp.xml"));
        StructureStore.getInstance().add("http://www.zurich.ibm.com/security/idmx/v2/sp.xml", sp);
        this.gp = (GroupParameters) Parser.getInstance().parse(this.getResource("gp.xml"));
        StructureStore.getInstance().add("http://www.zurich.ibm.com/security/idmx/v2/gp.xml", this.gp);
        CredentialStructure cs = (CredentialStructure) Parser.getInstance().parse(this.getResource("CredStructWithSmartcard.xml"));
        StructureStore.getInstance().add(CRED_STRUCT_WITH_SMARTCARD, cs);
        CredentialStructure cs2 = (CredentialStructure) Parser.getInstance().parse(this.getResource("CredStructScIV.xml"));
        StructureStore.getInstance().add(CRED_STRUCT_WITH_SMARTCARD_IV, cs2);
        CredentialStructure cs3 = (CredentialStructure) Parser.getInstance().parse(this.getResource("CredStructScIVNoUV.xml"));
        StructureStore.getInstance().add(CRED_STRUCT_WITH_SMARTCARD_IV_NOUV, cs3);
    }

    private InputSource getResource(String filename) {
        return new InputSource(this.getClass().getResourceAsStream("/eu/abc4trust/sampleXml/idemix/" + filename));
    }

    private Smartcard getSmartcardForTest() {
        eu.abc4trust.smartcard.SystemParameters sp = new eu.abc4trust.smartcard.SystemParameters();
        // A 257.9-bit safe prime
        sp.p = this.gp.getCapGamma();
        sp.subgroupOrder = this.gp.getRho();
        sp.g = this.gp.getG();
        sp.deviceSecretSizeBytes = 256 / 8;
        sp.signatureNonceLengthBytes = 80 / 8;
        sp.zkChallengeSizeBytes = 256 / 8;
        sp.zkStatisticalHidingSizeBytes = 80 / 8;
        sp.zkNonceSizeBytes = 128 / 8;

        /*
         * We need a consistent random number generator, so that the card secret is the same from
         * test to test (you will need to update CredOnSmartcard.xml) if you change the randomness.
         */
        Random prng = new Random(42);
        Smartcard theSmartcard = new SoftwareSmartcard(prng);
        RSAKeyPair rootKey = RSASignatureSystemTest.getSigningKeyForTest();
        theSmartcard.init(pin, sp, rootKey, deviceID);
        SmartcardBlob deviceUriBlob = new SmartcardBlob();
        try {
			deviceUriBlob.blob = nameOfSmartcard.toASCIIString().getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
		}
        theSmartcard.storeBlob(pin, Smartcard.device_name, deviceUriBlob);

        BigInteger p = this.ik.getPrivateKey().getP();
        BigInteger q = this.ik.getPrivateKey().getQ();
        BigInteger n = p.multiply(q);
        BigInteger R0 = this.ik.getPublicKey().getCapR()[0];
        BigInteger S = this.ik.getPublicKey().getCapS();
        CredentialBases cb = new CredentialBases(R0, S, n);

        theSmartcard.getNewNonceForSignature();
        assertEquals(theSmartcard.addIssuerParameters(pin, rootKey, issuerUri, cb), SmartcardStatusCode.OK);
        return theSmartcard;
    }

    private AbcSmartcardManager getSmartcardManagerWithSecret() throws Exception {
        Secret s =
                (Secret) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/smartcard/sampleSecret.xml"), true);
        IssuerParameters ip =
                (IssuerParameters) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/smartcard/sampleIssuerKey.xml"), true);
        KeyManager km = EasyMock.createMock(KeyManager.class);
        CredentialManager cm = EasyMock.createMock(CredentialManager.class);
        AbcSmartcardManager smartcardManager = new AbcSmartcardManager(cm, km, new CardStorage());
        EasyMock.expect(km.getIssuerParameters(ip.getParametersUID())).andReturn(ip).anyTimes();
        EasyMock.expect(cm.getCredentialDescription(EasyMock.notNull(URI.class))).andReturn(null);
        EasyMock.expect(cm.getSecret(nameOfSecret)).andReturn(s);
        EasyMock.replay(km);
        EasyMock.replay(cm);
        return smartcardManager;
    }

    @Test
    public void testIdemixIssuanceWithSmartcard() {
        CardStorage storage = new CardStorage();
        AbcSmartcardManager smartcardManager = new AbcSmartcardManager(null, null, storage);
        IdemixSmartcardHelper smartcardHelperForIssuer = new AbcSmartcardHelper();

        storage.addSmartcard(this.getSmartcardForTest(), pin);
        smartcardManager.allocateCredential(nameOfSmartcard, credNameOnSmartcard, issuerUri, false);

        // create the issuance specification
        IssuanceSpec issuanceSpec = new IssuanceSpec(
                URI.create(ISSUER_PK_PARAMS), URI.create(CRED_STRUCT_WITH_SMARTCARD));

        // get the values for the recipient
        Values valuesRecipient = new Values(this.ik.getPublicKey()
                .getGroupParams().getSystemParams());
        valuesRecipient.add("attr4", ATT_VALUE_4);
        valuesRecipient.add("attr1", ATT_VALUE_1);
        valuesRecipient.add("attr6", ATT_VALUE_6);
        valuesRecipient.add("attr2", ATT_VALUE_2);
        valuesRecipient.add("attr3", ATT_VALUE_3);
        valuesRecipient.add("attr5", ATT_VALUE_5);

        // get the values for the issuer
        Values valuesIssuer = new Values(this.ik.getPublicKey().getGroupParams().getSystemParams());
        valuesIssuer.add("attr1", ATT_VALUE_1);
        valuesIssuer.add("attr2", ATT_VALUE_2);
        valuesIssuer.add("attr6", ATT_VALUE_6);

        // run the issuance protocol.
        Issuer issuer = new Issuer(this.ik, issuanceSpec, valuesIssuer);

        RecipientInterface recipient = new Recipient(issuanceSpec, smartcardManager, valuesRecipient);

        Message msgToRecipient1 = issuer.round0();
        assertNotNull(msgToRecipient1);

        Message msgToIssuer1 = recipient.round1(msgToRecipient1, nameOfSmartcard, credNameOnSmartcard);
        assertNotNull(msgToIssuer1);

        Message msgToRecipient2 = issuer.round2(msgToIssuer1, smartcardHelperForIssuer);
        assertNotNull(msgToRecipient2);

        Credential cred = recipient.round3(msgToRecipient2);
        assertNotNull(cred);

        // Uncomment if you would like to update the credential for the next test:
        // XMLSerializer.getInstance().serialize(cred, URI.create("file:///tmp/a.xml"));
    }

    @Test
    public final void testIdemixPresentationWithSmartcard() throws Exception {
        /*
         * This test assumes that the smartcard was initilized in the same state as
         * when the file CredOnSmartcard.xml was created.
         * If this test fails, it might be worthwile to update CredOnSmartcard.xml
         */
        String proofSpec = "ProofSpecSmartcard.xml";

        List<CredConfig> c = new ArrayList<CredConfig>();
        c.add(new CredConfig("CredOnSmartcard.xml", "firstCred", nameOfSmartcard,
                credNameOnSmartcard, issuerUri));

        this.testPresentationIdemix(proofSpec, c, null, null);
    }

    private IssuanceConfig getIssuanceConfigForTest() {
        String tempName = "toBeIssued";
        URI credStruct = URI.create(CRED_STRUCT_WITH_SMARTCARD);
        URI newCredNameOnSc = URI.create("MyNewCred");

        Values valuesRecipient;
        valuesRecipient = new Values(this.ik.getPublicKey().getGroupParams().getSystemParams());
        valuesRecipient.add("attr4", BigInteger.valueOf(4000));
        valuesRecipient.add("attr1", BigInteger.valueOf(1000));
        valuesRecipient.add("attr6", ATT_VALUE_1);
        valuesRecipient.add("attr2", BigInteger.valueOf(2000));
        valuesRecipient.add("attr3", BigInteger.valueOf(3000));
        valuesRecipient.add("attr5", ATT_VALUE_4);

        Values valuesIssuer = new Values(this.ik.getPublicKey().getGroupParams().getSystemParams());
        valuesIssuer.add("attr1", BigInteger.valueOf(1000));
        valuesIssuer.add("attr2", BigInteger.valueOf(2000));
        valuesIssuer.add("attr6", ATT_VALUE_1);

        IssuanceConfig issConf = new IssuanceConfig(tempName, credStruct, issuerUri,
                nameOfSmartcard, newCredNameOnSc, valuesRecipient, valuesIssuer);
        return issConf;
    }

    @Test
    public final void testIdemixIssuance() throws Exception {
        String proofSpec = "ProofSpecAdvIss.xml";
        IssuanceConfig issConf = this.getIssuanceConfigForTest();

        this.testAdvancedIssuanceIdemix(issConf, proofSpec, null, null, null);
    }

    @Test
    public final void testIdemixIssuanceSecret() throws Exception {
        String proofSpec = "ProofSpecAdvIss.xml";
        IssuanceConfig issConf = this.getIssuanceConfigForTest();
        issConf.nameOfSmartcard = nameOfSecret;
        issConf.issuerOnSmartcard = URI.create(ISSUER_PK_PARAMS);

        this.testAdvancedIssuanceIdemix(issConf, proofSpec, null, null, null);
    }

    @Test
    public final void testIdemixIssuanceCoNymDomNymSecret() throws Exception {
        // There are 2 smartcards in this test

        String proofSpec = "ProofSpecAdvIssCoNymDnSecret.xml";
        IssuanceConfig issConf = this.getIssuanceConfigForTest();
        issConf.nameOfSmartcard = nameOfSecret;
        issConf.issuerOnSmartcard = URI.create(ISSUER_PK_PARAMS);

        List<CredConfig> cred = new ArrayList<CredConfig>();
        cred.add(new CredConfig("CredOnSmartcard.xml", "firstCred", nameOfSmartcard, credNameOnSmartcard, issuerUri));

        List<NymConfig> nym = new ArrayList<NymConfig>();
        nym.add(new NymConfig("Nym_Hello.xml", "Hello"));
        nym.add(new NymConfig("Nym_Secret.xml", "Secret"));
        List<NymConfig> domnym = new ArrayList<NymConfig>();
        domnym.add(new NymConfig("DomNym_EmployeeCorner.xml", "employeeCorner"));
        domnym.add(new NymConfig("DomNym_Cafeteria.xml", "cafeteria"));
        domnym.add(new NymConfig("DomNym_GameRoom.xml", "gameRoom"));

        this.testAdvancedIssuanceIdemix(issConf, proofSpec, cred, nym, domnym);
    }

    @Test
    public final void testFailedIssuanceCoNymDomNymSecret() throws Exception {
        // This should fail since we use 2 smartcards instead of 1.

        String proofSpec = "ProofSpecAdvIssCoNymDn.xml";
        IssuanceConfig issConf = this.getIssuanceConfigForTest();
        issConf.nameOfSmartcard = nameOfSecret;
        issConf.issuerOnSmartcard = URI.create(ISSUER_PK_PARAMS);

        List<CredConfig> cred = new ArrayList<CredConfig>();
        cred.add(new CredConfig("CredOnSmartcard.xml", "firstCred", nameOfSmartcard, credNameOnSmartcard, issuerUri));

        List<NymConfig> nym = new ArrayList<NymConfig>();
        nym.add(new NymConfig("Nym_Hello.xml", "Hello"));
        nym.add(new NymConfig("Nym_World.xml", "World"));
        nym.add(new NymConfig("Nym_Secret.xml", "Secret"));
        List<NymConfig> domnym = new ArrayList<NymConfig>();
        domnym.add(new NymConfig("DomNym_EmployeeCorner.xml", "employeeCorner"));
        domnym.add(new NymConfig("DomNym_Cafeteria.xml", "cafeteria"));
        domnym.add(new NymConfig("DomNym_GameRoom.xml", "gameRoom"));

        try {
            this.testAdvancedIssuanceIdemix(issConf, proofSpec, cred, nym, domnym);
            fail("Proof should not verify");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Proof does not verify") || e.getMessage().contains("Incorrect T-value"));
        }
    }

    @Test
    public final void testIdemixIssuanceWithIssuerAttributes() throws Exception {
        String proofSpec = "ProofSpecAdvIssIV.xml";

        IssuanceConfig issConf = null;
        {
            String tempName = "toBeIssued";
            URI credStruct = URI.create(CRED_STRUCT_WITH_SMARTCARD_IV);
            URI newCredNameOnSc = URI.create("MyNewCred");

            Values valuesRecipient;
            valuesRecipient = new Values(this.ik.getPublicKey().getGroupParams().getSystemParams());
            valuesRecipient.add("attr4", BigInteger.valueOf(404)); // set by user
            valuesRecipient.add("attr1", BigInteger.valueOf(101)); // common
            valuesRecipient.add("attr2", BigInteger.valueOf(202)); // common
            valuesRecipient.add("attr3", BigInteger.valueOf(303)); // set by user

            Values valuesIssuer = new Values(this.ik.getPublicKey().getGroupParams().getSystemParams());
            valuesIssuer.add("attr1", BigInteger.valueOf(101)); // common
            valuesIssuer.add("attr2", BigInteger.valueOf(202)); // common
            valuesIssuer.add("attr6", BigInteger.valueOf(606)); // set by issuer
            valuesIssuer.add("attr5", BigInteger.valueOf(505)); // set by issuer

            issConf = new IssuanceConfig(tempName, credStruct, issuerUri,
                    nameOfSmartcard, newCredNameOnSc, valuesRecipient, valuesIssuer);
        }

        this.testAdvancedIssuanceIdemix(issConf, proofSpec, null, null, null);
    }

    @Test
    public final void testIdemixIssuanceWithIssuerAttributesNoUserAtts() throws Exception {
        String proofSpec = "ProofSpecAdvIssIVNoUV.xml";

        IssuanceConfig issConf = null;
        {
            String tempName = "toBeIssued";
            URI credStruct = URI.create(CRED_STRUCT_WITH_SMARTCARD_IV_NOUV);
            URI newCredNameOnSc = URI.create("MyNewCred");

            Values valuesRecipient;
            valuesRecipient = new Values(this.ik.getPublicKey().getGroupParams().getSystemParams());

            Values valuesIssuer = new Values(this.ik.getPublicKey().getGroupParams().getSystemParams());
            valuesIssuer.add("attr1", BigInteger.valueOf(1)); // set by issuer
            valuesIssuer.add("attr2", BigInteger.valueOf(2)); // set by issuer
            valuesIssuer.add("attr3", BigInteger.valueOf(3)); // set by issuer
            valuesIssuer.add("attr4", BigInteger.valueOf(4)); // set by issuer
            valuesIssuer.add("attr5", BigInteger.valueOf(5)); // set by issuer
            valuesIssuer.add("attr6", BigInteger.valueOf(6)); // set by issuer

            issConf = new IssuanceConfig(tempName, credStruct, issuerUri,
                    nameOfSmartcard, newCredNameOnSc, valuesRecipient, valuesIssuer);
        }

        this.testAdvancedIssuanceIdemix(issConf, proofSpec, null, null, null);
    }

    @Test
    public final void testIdemixIssuanceCarryOver() throws Exception {
        String proofSpec = "ProofSpecAdvIssCarryOver.xml";
        IssuanceConfig issConf = this.getIssuanceConfigForTest();

        List<CredConfig> cred = new ArrayList<CredConfig>();
        cred.add(new CredConfig("CredOnSmartcard.xml", "firstCred", nameOfSmartcard, credNameOnSmartcard, issuerUri));

        this.testAdvancedIssuanceIdemix(issConf, proofSpec, cred, null, null);
    }

    @Test
    public final void testIdemixIssuanceCarryOverNymDomNym() throws Exception {
        String proofSpec = "ProofSpecAdvIssCoNymDn.xml";
        IssuanceConfig issConf = this.getIssuanceConfigForTest();

        List<CredConfig> cred = new ArrayList<CredConfig>();
        cred.add(new CredConfig("CredOnSmartcard.xml", "firstCred", nameOfSmartcard, credNameOnSmartcard, issuerUri));

        List<NymConfig> nym = new ArrayList<NymConfig>();
        nym.add(new NymConfig("Nym_Hello.xml", "Hello"));
        nym.add(new NymConfig("Nym_World.xml", "World"));
        List<NymConfig> domnym = new ArrayList<NymConfig>();
        domnym.add(new NymConfig("DomNym_EmployeeCorner.xml", "employeeCorner"));
        domnym.add(new NymConfig("DomNym_Cafeteria.xml", "cafeteria"));

        this.testAdvancedIssuanceIdemix(issConf, proofSpec, cred, nym, domnym);
    }

    @Test
    public final void testIdemixPresentationWithNym() throws Exception {
        String proofSpec = "ProofSpecNym.xml";

        List<NymConfig> n = new ArrayList<NymConfig>();
        n.add(new NymConfig("Nym_Hello.xml", "Hello"));

        this.testPresentationIdemix(proofSpec, null, n, null);
    }

    @Test
    public final void testIdemixPresentationWithDomNym() throws Exception {
        String proofSpec = "ProofSpecDomNym.xml";

        List<NymConfig> n = new ArrayList<NymConfig>();
        n.add(new NymConfig("DomNym_EmployeeCorner.xml", "employeeCorner"));

        this.testPresentationIdemix(proofSpec, null, null, n);
    }

    @Test
    public final void testIdemixPresentationWithSmartcardNymDomnym() throws Exception {
        /*
         * This test assumes that the smartcard was initilized in the same state as
         * when the file CredOnSmartcard.xml was created.
         * If this test fails, it might be worthwile to update CredOnSmartcard.xml
         */
        String proofSpec = "ProofSpecCredNymDomnym.xml";

        List<CredConfig> cred = new ArrayList<CredConfig>();
        cred.add(new CredConfig("CredOnSmartcard.xml", "firstCred", nameOfSmartcard, credNameOnSmartcard, issuerUri));
        List<NymConfig> nym = new ArrayList<NymConfig>();
        nym.add(new NymConfig("Nym_Hello.xml", "Hello"));
        List<NymConfig> domnym = new ArrayList<NymConfig>();
        domnym.add(new NymConfig("DomNym_EmployeeCorner.xml", "employeeCorner"));

        this.testPresentationIdemix(proofSpec, cred, nym, domnym);
    }

    private void testPresentationIdemix(String proofSpecLocation,
            List<CredConfig> creds,
            List<NymConfig> nyms, List<NymConfig> domNyms)
                    throws Exception{
        List<SmartcardConfig> sc = new ArrayList<SmartcardConfig>();
        sc.add(new SmartcardConfig(this.getSmartcardForTest(), pin));

        this.testPresentationIdemix(proofSpecLocation, sc, creds, nyms, domNyms);
    }

    private void testAdvancedIssuanceIdemix(IssuanceConfig issConf, String proofSpecLocation,
            List<CredConfig> creds,
            List<NymConfig> nyms, List<NymConfig> domNyms)
                    throws Exception{
        List<SmartcardConfig> sc = new ArrayList<SmartcardConfig>();
        sc.add(new SmartcardConfig(this.getSmartcardForTest(), pin));

        this.testAdvancedIssuanceIdemix(issConf, proofSpecLocation, sc, creds, nyms, domNyms);
    }

    private ProverInput setupProver(AbcSmartcardManager scm, List<SmartcardConfig> sc,
            List<CredConfig> c, List<NymConfig> nyms, List<NymConfig> domNyms) {
        return this.setupProver(scm, sc, c, nyms, domNyms, null);
    }

    private ProverInput setupProver(AbcSmartcardManager smartcardManager,
            List<SmartcardConfig> cards,
            List<CredConfig> creds,
            List<NymConfig> nyms, List<NymConfig> domNyms,
            IssuanceConfig issConf) {
        for(SmartcardConfig scc: cards) {
            smartcardManager.addSmartcard(scc.card, scc.pin);
        }

        ProverInput input = new ProverInput();
        input.smartcardManager = smartcardManager;
        if (creds != null) {
            for(CredConfig c: creds) {
                smartcardManager.allocateCredential(c.card, c.nameOnCard, c.issuerName, false);

                Credential cred = (Credential) Parser.getInstance().parse(this.getResource(c.filename));
                String fullTempName = cred.getFullTemporaryNameForProof(c.tempName);
                input.credentials.put(fullTempName, cred);
            }
        }
        if (nyms != null) {
            for(NymConfig nym: nyms) {
                StoredPseudonym sp =
                        (StoredPseudonym) Parser.getInstance().parse(this.getResource(nym.filename));
                input.pseudonyms.put(nym.tempName, sp);

                // Compute expected values
                nym.expectedValue = smartcardManager.computePseudonym(sp.getSmartcardUri(), this.gp.getH(), sp.getRandomizer());
            }
        }
        if (domNyms != null) {
            for(NymConfig nym: domNyms) {
                StoredDomainPseudonym sp =
                        (StoredDomainPseudonym) Parser.getInstance().parse(this.getResource(nym.filename));
                input.domainPseudonyms.put(nym.tempName, sp);

                // Compute expected values
                nym.expectedValue = smartcardManager.computeScopeExclusivePseudonym(sp.getSmartcardUri(), sp.getScope());
            }
        }
        if (issConf != null) {
            CredentialCommitment credComm =
                    new CredentialCommitment(ipkId, issConf.credStructUri, issConf.valuesRecipient,
                            smartcardManager, issConf.nameOnSmartcard, issConf.nameOfSmartcard);
            input.credentialCommitments.put(issConf.tempNameOfNewCredential, credComm);
        }

        return input;
    }

    private void testPresentationIdemix(String proofSpecLocation, List<SmartcardConfig> cards,
            List<CredConfig> creds,
            List<NymConfig> nyms, List<NymConfig> domNyms) throws Exception {
        ProofSpec spec = (ProofSpec) Parser.getInstance().parse(this.getResource(proofSpecLocation));
        BigInteger nonce = Verifier.generateNonce(spec.getGroupParams().getSystemParams());

        Proof p;
        // Prover
        {
            // AbcSmartcardManager smartcardManager = new AbcSmartcardManager();
            AbcSmartcardManager smartcardManager = this.getSmartcardManagerWithSecret();
            ProverInput proverInput = this.setupProver(smartcardManager, cards, creds, nyms, domNyms);
            p = new Prover(proverInput, spec, nonce).buildProof();
            assertNotNull(p);
        }

        // Verifier
        {
            VerifierInput verifierInput = new VerifierInput();
            verifierInput.smartcardHelper = new AbcSmartcardHelper();
            Verifier verifier = new Verifier(spec, p, nonce, verifierInput);
            assertTrue(verifier.verify());

            // Check nyms
            if (nyms != null) {
                for (NymConfig nym: nyms) {
                    BigInteger actual = verifier.getRevealedPseudonymValue(nym.tempName);
                    assertEquals(nym.expectedValue, actual);
                }
            }
            if (domNyms != null) {
                for (NymConfig nym: domNyms) {
                    BigInteger actual = verifier.getRevealedDomNymValue(nym.tempName);
                    assertEquals(nym.expectedValue, actual);
                }
            }
        }
    }

    private void testAdvancedIssuanceIdemix(IssuanceConfig issConf, String proofSpecLocation,
            List<SmartcardConfig> cards, List<CredConfig> creds,
            List<NymConfig> nyms, List<NymConfig> domNyms)
                    throws Exception {

        ProofSpec proofSpec = (ProofSpec) Parser.getInstance().parse(this.getResource(proofSpecLocation));
        assertNotNull(proofSpec);
        AdvancedIssuanceSpec issuanceSpec = new AdvancedIssuanceSpec(ipkId,
                issConf.credStructUri, issConf.tempNameOfNewCredential, proofSpec);

        //AbcSmartcardManager smartcardManager = new AbcSmartcardManager();
        AbcSmartcardManager smartcardManager = this.getSmartcardManagerWithSecret();

        // Setup Prover
        ProverInput proverInput = this.setupProver(smartcardManager, cards, creds, nyms, domNyms, issConf);
        smartcardManager.allocateCredential(issConf.nameOfSmartcard, issConf.nameOnSmartcard,
                issConf.issuerOnSmartcard, false);

        // run the issuance protocol.
        VerifierInput verifierInput = new VerifierInput();
        verifierInput.smartcardHelper = new AbcSmartcardHelper();
        AdvancedIssuer issuer =
                new AdvancedIssuer(this.ik);

        Message msgToRecipient1 = issuer.round0();
        System.out.println(XMLSerializer.getInstance().serialize(msgToRecipient1));
        assertNotNull(msgToRecipient1);

        AdvancedRecipient recipient =
                new AdvancedRecipient(issuanceSpec, proverInput, issConf.valuesRecipient);
        Message msgToIssuer1 = recipient.round1(msgToRecipient1);
        System.out.println(XMLSerializer.getInstance().serialize(msgToIssuer1));
        assertNotNull(msgToIssuer1);


        Message msgToRecipient2 = issuer.round2(msgToIssuer1, issuanceSpec, issConf.valuesIssuer, verifierInput);
        System.out.println(XMLSerializer.getInstance().serialize(msgToRecipient2));
        assertNotNull(msgToRecipient2);

        Credential cred =
                recipient.round3(msgToRecipient2, issConf.nameOfSmartcard, issConf.nameOnSmartcard);
        assertNotNull(cred);

        System.out.println(cred.toStringPretty());

        // Check nyms
        if (nyms != null) {
            for (NymConfig nym: nyms) {
                BigInteger actual = issuer.getRevealedPseudonymValue(nym.tempName);
                assertEquals(nym.expectedValue, actual);
            }
        }
        if (domNyms != null) {
            for (NymConfig nym: domNyms) {
                BigInteger actual = issuer.getRevealedDomNymValue(nym.tempName);
                assertEquals(nym.expectedValue, actual);
            }
        }
    }

    class SmartcardConfig {
        public final Smartcard card;
        public final int pin;
        public SmartcardConfig(Smartcard card, int pin) {
            this.card = card;
            this.pin = pin;
        }
    }
    class CredConfig {
        public final String filename;
        public final String tempName;
        public final URI card;
        public final URI nameOnCard;
        public final URI issuerName;
        public CredConfig(String filename, String tempName, URI cardName, URI nameOnCard, URI issuerName) {
            this.filename = filename;
            this.tempName = tempName;
            this.card = cardName;
            this.nameOnCard = nameOnCard;
            this.issuerName = issuerName;
        }
    }

    class NymConfig {
        public final String filename;
        public final String tempName;
        public BigInteger expectedValue;
        public NymConfig(String filename, String tempName) {
            this.filename = filename;
            this.tempName = tempName;
        }
    }

    class IssuanceConfig {
        public String tempNameOfNewCredential;
        public URI credStructUri;
        public URI issuerOnSmartcard;
        public URI nameOfSmartcard;
        public URI nameOnSmartcard;
        public Values valuesRecipient;
        public Values valuesIssuer;

        public IssuanceConfig(String tempNameOfNewCredential, URI credStructUri,
                URI issuerOnSmartcard, URI nameOfSmartcard, URI nameOnSmartcard,
                Values valuesRecipient, Values valuesIssuer) {
            this.tempNameOfNewCredential = tempNameOfNewCredential;
            this.credStructUri = credStructUri;
            this.issuerOnSmartcard = issuerOnSmartcard;
            this.nameOfSmartcard = nameOfSmartcard;
            this.nameOnSmartcard = nameOnSmartcard;
            this.valuesRecipient = valuesRecipient;
            this.valuesIssuer = valuesIssuer;
        }
    }

}