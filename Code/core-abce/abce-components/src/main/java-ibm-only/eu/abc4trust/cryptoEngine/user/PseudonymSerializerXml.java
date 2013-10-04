//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.cryptoEngine.user;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;

import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * This class serializes pseudonym by their XML value.
 * 
 * @author enr
 */
public class PseudonymSerializerXml extends AbstractPseudonymSerializer {

	private final CardStorage cardStorage;
	
	public PseudonymSerializerXml(CardStorage cardStorage){
		this.cardStorage = cardStorage;
	}
	
	@Override
	public CardStorage getCardStorage(){
		return this.cardStorage;
	}
	
  @Override
  public byte[] serializePseudonym(PseudonymWithMetadata pwm) {
	  if(pwm.getPseudonym().isExclusive()){
		  return this.serializeExclusivePseudonym(pwm);
	  }
	  
    try {
      ByteArrayOutputStream ser = new ByteArrayOutputStream();
      ser.write(magicHeader());

      ObjectFactory of = new ObjectFactory();
      ByteArrayOutputStream xml = XmlUtils.toXmlAsBaos(of.createPseudonymWithMetadata(pwm), true);
      ser.write(xml.toByteArray());

      return ser.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PseudonymWithMetadata unserializePseudonym(byte[] data, URI pseudonymUID) {
	  PseudonymWithMetadata pwm;
	  try{
		  pwm = this.unserializeExclusivePseudonym(data, pseudonymUID);
		  if(pwm != null){
			  return pwm;
		  }
	  }catch(Exception e){
		  //Not scope-exclusive. Trying normal pseudonym
	  }
	  
    try {
      ByteArrayInputStream bais = new ByteArrayInputStream(data);
      int header = bais.read();
      if (header != magicHeader()) {
        throw new RuntimeException("Cannot unserialize this pseudonym: header was " + header
            + " expected header " + magicHeader());
      }
      return (PseudonymWithMetadata) XmlUtils.getObjectFromXML(bais, true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int magicHeader() {
    return 65;
  }

}
