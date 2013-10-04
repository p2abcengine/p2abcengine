//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.cryptoEngine.user;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.xml.PseudonymWithMetadata;

/**
 * This class chooses the best pseudonym serializer.
 * @author enr
 *
 */
public class PseudonymSerializerBest extends AbstractPseudonymSerializer {

	private final CardStorage cardStorage;
	
	public PseudonymSerializerBest(CardStorage cardStorage){
		this.cardStorage = cardStorage;
	}
	
	@Override
	public CardStorage getCardStorage(){
		return this.cardStorage;
	}
	
  private List<PseudonymSerializer> getClasses() {
    List<PseudonymSerializer> cslist = new ArrayList<PseudonymSerializer>();
    cslist.add(new PseudonymSerializerXml(this.getCardStorage()));
    cslist.add(new PseudonymSerializerGzipXml(this.getCardStorage()));
    cslist.add(new PseudonymSerializerObject(this.getCardStorage()));
    cslist.add(new PseudonymSerializerObjectGzip(this.getCardStorage()));
    return cslist;
  }
  
  @Override
  public byte[] serializePseudonym(PseudonymWithMetadata pwm) {
	  if(pwm.getPseudonym().isExclusive()){
		  return this.serializeExclusivePseudonym(pwm);
	  }
	  
    byte[] best = null;
    for(PseudonymSerializer ps: getClasses()) {
      byte[] ser = ps.serializePseudonym(pwm);
      if(best == null || best.length > ser.length) {
        best = ser;
      }
    }
    return best;
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
	  
    int magicHeader = data[0];
    for(PseudonymSerializer ps: getClasses()) {
      if(magicHeader == ps.magicHeader()) {
        return ps.unserializePseudonym(data, pseudonymUID);
      }
    }
    throw new RuntimeException("Unable to unserialize the pseudonym");
  }

  @Override
  public int magicHeader() {
    return 64;
  }

}
