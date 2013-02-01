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

package eu.abc4trust.abce.internal.revocation;

import java.net.URI;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorEvent;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorHistory;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorState;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorWitness;
import com.ibm.zurich.idmx.showproof.accumulator.ValueHasBeenRevokedException;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.XMLSerializer;

import eu.abc4trust.cryptoEngine.revauth.AccumCryptoEngineRevAuthImpl;
import eu.abc4trust.revocationProxy.RevocationMessageType;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.RevocationInformation;

public class RevocationUtility {

    public static AccumulatorState getState(
            RevocationInformation revocationInformation) {
        Element str = (Element) revocationInformation.getCryptoParams()
                .getAny().get(0);
        AccumulatorState state = (AccumulatorState) Parser.getInstance().parse(
                str);
        return state;
    }

    public static void updateNonRevocationEvidence(NonRevocationEvidence nre,
            RevocationInformation revInfo) {
        Parser xmlParser = Parser.getInstance();
        Element witnessElement = (Element) nre.getCryptoParams().getAny()
                .get(0);
        AccumulatorWitness w1 = (AccumulatorWitness) xmlParser
                .parse(witnessElement);

        List<Object> cryptoEvidence = revInfo.getCryptoParams().getAny();
        
        Element historyElement = (Element) cryptoEvidence.get(1);
        AccumulatorHistory history = (AccumulatorHistory) xmlParser
                .parse(historyElement);

        boolean check = true;
        for (AccumulatorEvent accumulatorEvent : history) {
            try {
            	if(w1.getState().getEpoch() >= accumulatorEvent.getNewEpoch()){ // The witness has already been updated with this event
            		continue;
            	}
                w1 = AccumulatorWitness.updateWitness(w1, accumulatorEvent,
                        check);
            } catch (ValueHasBeenRevokedException ex) {
                // We don't care at this point.
                System.out.println("Value has been revoked.");
                break;
            } 
        }

        XMLSerializer xmlSerializer = XMLSerializer.getInstance();
        nre.getCryptoParams().getAny()
        .set(0, xmlSerializer.serializeAsElement(w1));
        nre.setEpoch(w1.getState().getEpoch());

        nre.setCreated(AccumCryptoEngineRevAuthImpl.getNow());
        nre.setExpires(AccumCryptoEngineRevAuthImpl.getExpirationDate());
    }
    
    public static Element serializeRevocationMessageType(RevocationMessageType rmt) {
        return createW3DomElement("RevocationMessageType", rmt.toString());
    }
    public static Element serializeRevocationInfoUid(URI revInfoUid) {
        return createW3DomElement("RevocationInfoUid", revInfoUid.toString());
    }
    public static Element serializeEpoch(Integer epoch) {
        return createW3DomElement("Epoch", epoch.toString());
    }
    public static RevocationMessageType unserializeRevocationMessageType(Element element) {
      RevocationMessageType rmt = RevocationMessageType.valueOf(element.getTextContent());
      return rmt;
    }
    public static URI unserializeRevocationInfoUid(Element element) {
        URI revInfoUid = URI.create(element.getTextContent());
        return revInfoUid;
    }
    public static Integer unserializeEpoch(Element element) {
        Integer epoch = new Integer(element.getTextContent());
        return epoch;
    }
    
    private static Element createW3DomElement(String elementName, String value) {
        Element element;
        try {
          element = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement(elementName);
        } catch (DOMException e) {
            throw new IllegalStateException("This should always work!",e);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("This should always work!",e);
        }
        element.setTextContent(value);
        return element;
    }

}
