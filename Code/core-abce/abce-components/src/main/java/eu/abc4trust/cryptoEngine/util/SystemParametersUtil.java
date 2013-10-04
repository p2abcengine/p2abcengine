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

package eu.abc4trust.cryptoEngine.util;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import com.ibm.zurich.idmx.utils.GroupParameters;
import com.ibm.zurich.idmx.utils.StructureStore;
import com.ibm.zurich.idmx.utils.XMLSerializer;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.bridging.StaticGroupParameters;
import eu.abc4trust.cryptoEngine.idemix.user.IdemixCryptoEngineUserImpl;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixConstants;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixSystemParameters;
import eu.abc4trust.cryptoEngine.uprove.util.UProveSerializer;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SmartcardSystemParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class SystemParametersUtil {

    private static final Logger logger = Logger
            .getLogger(SystemParametersUtil.class.toString());

    private static final ObjectFactory of = new ObjectFactory();

    public SystemParameters generatePilotSystemParameters_WithIdemixSpecificKeySize_1024() {
        return SystemParametersUtil
                .generatePilotSystemParameters_WithIdemixSpecificKeySize(1024,
                        2048, 50);
    }

    public SystemParameters generatePilotSystemParameters_WithIdemixSpecificKeySize_1536() {
        return SystemParametersUtil
                .generatePilotSystemParameters_WithIdemixSpecificKeySize(1536,
                        2048, 50);
    }

    public SystemParameters generatePilotSystemParameters_WithIdemixSpecificKeySize_2048() {
        return SystemParametersUtil
                .generatePilotSystemParameters_WithIdemixSpecificKeySize(2048,
                        2048, 50);
    }
    public static SystemParameters generatePilotSystemParameters_WithIdemixSpecificKeySize(
            int idemixKeyLength, int uproveKeyLength) {
        return SystemParametersUtil
                .generatePilotSystemParameters_WithIdemixSpecificKeySize(idemixKeyLength,
                        uproveKeyLength, 50);
    }

    public static SystemParameters generatePilotSystemParameters_WithIdemixSpecificKeySize(
            int idemixKeyLength, int uproveKeyLength, int uproveNumberOfTokens) {
        // ok - we have to generate them from scratch...
        SystemParameters systemParameters = new ObjectFactory().createSystemParameters();
        systemParameters.setVersion("1.0");

        // IDEMIX PART!

        // Generate system parameters.
        com.ibm.zurich.idmx.utils.SystemParameters.SET_L_PHI_80 = true;
        com.ibm.zurich.idmx.utils.SystemParameters sp =
                com.ibm.zurich.idmx.utils.SystemParameters
                .generateSystemParametersFromRsaModulusSize(idemixKeyLength);

        StructureStore.getInstance().add(IdemixConstants.systemParameterId, sp);

        // use static group parameters
        GroupParameters gp = getGroupParameters();

        systemParameters.getAny().add(sp);
        systemParameters.getAny().add(gp);

        // UPROVE PART!

        // GROUP OID specifies KEY_LENGHT and must be setup according to UProve
        // documentation
        String UPROVE_GROUP_OID = "1.3.6.1.4.1.311.75.1.1.1";

        systemParameters.getAny().add(
                new UProveSerializer().createKeyLengthElement(uproveKeyLength));
        systemParameters.getAny().add(new UProveSerializer().createGroupOIDElement(UPROVE_GROUP_OID));
        systemParameters.getAny().add(new UProveSerializer().createNumberOfTokensElement(uproveNumberOfTokens));

        return systemParameters;
    }

    private static GroupParameters getGroupParameters() {
        // GroupParameters gp = (GroupParameters) Parser
        // .getInstance()
        // .parse(new InputSource(
        // SystemParametersUtil.class
        // .getResourceAsStream("/eu/abc4trust/systemparameters/bridged-groupParameters.xml")));

//        GroupParameters gp = com.ibm.zurich.idmx.utils.GroupParameters
//                .generateGroupParams(URI
//                        .create(IdemixConstants.systemParameterId));
    	GroupParameters gp = StaticGroupParameters.getGroupParameters();
        return gp;
    }

    public static SystemParameters serialize(
            SystemParameters systemParameters) {
        SystemParameters newSystemParameters = new ObjectFactory()
        .createSystemParameters();
        newSystemParameters.setVersion("1.0");

        XMLSerializer xmlSerializer = XMLSerializer
                .getInstance();

        List<Object> systemParametersContent = systemParameters.getAny();

        com.ibm.zurich.idmx.utils.SystemParameters sp = (com.ibm.zurich.idmx.utils.SystemParameters) systemParametersContent
                .get(0);
        Element spAsElement = xmlSerializer.serializeAsElement(sp);
        newSystemParameters.getAny().add(spAsElement);

        GroupParameters gp = (GroupParameters) systemParameters.getAny().get(1);
        Element gpAsElement = xmlSerializer.serializeAsElement(gp);
        newSystemParameters.getAny().add(gpAsElement);

        // UPROVE Parameters - all of type DOM W3 Element - and can be transfered directly!
        for(int i=2; i<systemParametersContent.size(); i++) {
            newSystemParameters.getAny().add(systemParametersContent.get(i));
        }
        return newSystemParameters;
    }

    public static String printSystemParameters(
            com.ibm.zurich.idmx.utils.SystemParameters sp) {
        XMLSerializer xmlSerializer = com.ibm.zurich.idmx.utils.XMLSerializer
                .getInstance();

        Element spAsElement = xmlSerializer.serializeAsElement(sp);

        Document document = spAsElement.getOwnerDocument();
        DOMImplementationLS domImplLS = (DOMImplementationLS) document
                .getImplementation();
        LSSerializer serializer = domImplLS.createLSSerializer();
        String str = serializer.writeToString(spAsElement);
        return str;
    }

    public static SmartcardSystemParameters createSmartcardSystemParameters(
            SystemParameters sysParams) {


        try {
            IdemixSystemParameters idemixSystemParameters = IdemixCryptoEngineUserImpl
                    .loadIdemixSystemParameters(sysParams);
            // This will throw illegal state exception if not found.
            GroupParameters gp = idemixSystemParameters.getGroupParameters();

            if (gp.getSystemParams() == null) {
                throw new RuntimeException(
                        "System parameters are not correctly set up");
            }


            SmartcardSystemParameters scSysParams = new SmartcardSystemParameters();

            BigInteger p = gp.getCapGamma();
            BigInteger g = gp.getG();
            BigInteger subgroupOrder = gp.getRho();
            int zkChallengeSizeBytes = 256 / 8;
            int zkStatisticalHidingSizeBytes = 80 / 8;
            int deviceSecretSizeBytes = 256 / 8;
            int signatureNonceLengthBytes = 128 / 8;
            int zkNonceSizeBytes = 256 / 8;
            int zkNonceOpeningSizeBytes = 256 / 8;

            scSysParams.setPrimeModulus(p);
            scSysParams.setGenerator(g);
            scSysParams.setSubgroupOrder(subgroupOrder);
            scSysParams.setZkChallengeSizeBytes(zkChallengeSizeBytes);
            scSysParams
            .setZkStatisticalHidingSizeBytes(zkStatisticalHidingSizeBytes);
            scSysParams.setDeviceSecretSizeBytes(deviceSecretSizeBytes);
            scSysParams.setSignatureNonceLengthBytes(signatureNonceLengthBytes);
            scSysParams.setZkNonceSizeBytes(zkNonceSizeBytes);
            scSysParams.setZkNonceOpeningSizeBytes(zkNonceOpeningSizeBytes);
            return scSysParams;
        } catch (CryptoEngineException ex1) {
            throw new RuntimeException(
                    "Could not initialize the Idemix StructureStore");
        } catch (RuntimeException ex) {
            logger.info("Did you forget to add the system parameters the Idemix Struture store? ("
                    + ex.getMessage() + ")");
            throw ex;
        }
    }

    public static String getHashOfSystemParameters(
            SystemParameters systemParameters) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");

            JAXBElement<SystemParameters> element = of
                    .createSystemParameters(systemParameters);
            String xml = XmlUtils.toNormalizedXML(element);
            md.update(xml.getBytes(Charset.forName("UTF-8")));
            byte[] mdbytes = md.digest();

            // convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (byte mdbyte : mdbytes) {
                sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16)
                        .substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
