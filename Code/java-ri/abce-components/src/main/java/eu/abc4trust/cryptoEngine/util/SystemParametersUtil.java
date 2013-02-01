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

import org.xml.sax.InputSource;

import com.ibm.zurich.idmx.utils.GroupParameters;
import com.ibm.zurich.idmx.utils.Parser;

import eu.abc4trust.cryptoEngine.uprove.util.UProveSerializer;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;

public class SystemParametersUtil {

    public SystemParameters generatePilotSystemParameters() {
        int IDEMIX_KEY_LENGTH = 1536;
        return generatePilotSystemParameters_WithIdemixSpecificKeySize(IDEMIX_KEY_LENGTH);
    }

    public SystemParameters generatePilotSystemParameters_WithIdemixSpecificKeySize_1024() {
        return generatePilotSystemParameters_WithIdemixSpecificKeySize(1024);
    }
    public SystemParameters generatePilotSystemParameters_WithIdemixSpecificKeySize_1536() {
        return generatePilotSystemParameters_WithIdemixSpecificKeySize(1536);
    }
    public SystemParameters generatePilotSystemParameters_WithIdemixSpecificKeySize_2048() {
        return generatePilotSystemParameters_WithIdemixSpecificKeySize(2048);
    }
    public SystemParameters generatePilotSystemParameters_WithIdemixSpecificKeySize(int idemixKeyLength) {
        // ok - we have to generate them from scratch...
        SystemParameters systemParameters = new ObjectFactory().createSystemParameters();
        systemParameters.setVersion("1.0");
    
        // IDEMIX PART!
    
        // generate system parameters
        com.ibm.zurich.idmx.utils.SystemParameters.SET_L_PHI_80 = true;
        com.ibm.zurich.idmx.utils.SystemParameters sp =
            com.ibm.zurich.idmx.utils.SystemParameters
                .generateSystemParametersFromRsaModulusSize(idemixKeyLength);
        // use static group parameters
        GroupParameters gp =
            (GroupParameters) Parser
                .getInstance()
                .parse(
                    new InputSource(
                        SystemParametersUtil.class
                            .getResourceAsStream("/eu/abc4trust/systemparameters/bridged-groupParameters.xml")));
    
        systemParameters.getAny().add(sp);
        systemParameters.getAny().add(gp);
    
        // UPROVE PART! - GROUP OID specifies KEY_LENGHT and must be setup according to UProve documentation
        String UPROVE_GROUP_OID = "1.3.6.1.4.1.311.75.1.1.1";
        int UPROVE_KEY_LENGTH = 2048;
    
        systemParameters.getAny().add(new UProveSerializer().createKeyLengthElement(UPROVE_KEY_LENGTH));
        systemParameters.getAny().add(new UProveSerializer().createGroupOIDElement(UPROVE_GROUP_OID));
    
        return systemParameters;
    }
}
