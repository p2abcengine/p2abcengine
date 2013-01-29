//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.cryptoEngine.user;

import java.util.ArrayList;
import java.util.List;

import eu.abc4trust.xml.PseudonymWithMetadata;

/**
 * This class chooses the best pseudonym serializer.
 * @author enr
 *
 */
public class PseudonymSerializerBest implements PseudonymSerializer {

  private List<PseudonymSerializer> getClasses() {
    List<PseudonymSerializer> cslist = new ArrayList<PseudonymSerializer>();
    cslist.add(new PseudonymSerializerXml());
    cslist.add(new PseudonymSerializerGzipXml());
    cslist.add(new PseudonymSerializerObject());
    cslist.add(new PseudonymSerializerObjectGzip());
    return cslist;
  }
  
  @Override
  public byte[] serializePseudonym(PseudonymWithMetadata cred) {
    byte[] best = null;
    for(PseudonymSerializer ps: getClasses()) {
      byte[] ser = ps.serializePseudonym(cred);
      if(best == null || best.length > ser.length) {
        best = ser;
      }
    }
    return best;
  }

  @Override
  public PseudonymWithMetadata unserializePseudonym(byte[] data) {
    int magicHeader = data[0];
    for(PseudonymSerializer ps: getClasses()) {
      if(magicHeader == ps.magicHeader()) {
        return ps.unserializePseudonym(data);
      }
    }
    throw new RuntimeException("Unable to unserialize the credential");
  }

  @Override
  public int magicHeader() {
    return 64;
  }

}
