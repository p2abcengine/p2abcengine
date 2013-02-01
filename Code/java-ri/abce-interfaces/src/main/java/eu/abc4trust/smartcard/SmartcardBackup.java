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

package eu.abc4trust.smartcard;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;


/**
 * Backup of the attendance data on the smartcard, protected by a message authentication code.
 * @author enr
 *
 */
public class SmartcardBackup {
  public Map<URI, SmartcardBlob> blobstore;
  //public Map<URI, BigInteger> credentialFragments;
  //public BigInteger devicePublicKey;
  public URI deviceUri;
  public short deviceID;
  public byte[] macDevice;
  public byte[] macCounters;
  public Map<Byte, byte[]> macCredentials;
  
  
  SmartcardBackup() {
    this.blobstore = new HashMap<URI, SmartcardBlob>();
    //credentialFragments = new HashMap<URI, BigInteger>();
    this.macCredentials = new HashMap<Byte, byte[]>();
  }
  
  public void serialize(File f){
	  try {		  
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		
		writer.write(""+this.blobstore.size());
		writer.newLine();
		for(URI uri : this.blobstore.keySet()){
			writer.write(uri.toASCIIString());
			writer.newLine();
			writer.write(Base64.encodeBytes(this.blobstore.get(uri).blob));
			writer.newLine();
		}
		
		writer.write(this.deviceUri.toASCIIString());
		writer.newLine();
		writer.write(""+this.deviceID);
		writer.newLine();
		writer.write(Base64.encodeBytes(this.macDevice));
		writer.newLine();
		if(this.macCounters != null){
			writer.write(Base64.encodeBytes(this.macCounters));
		}
		writer.newLine();
		
		writer.write(""+this.macCredentials.size());
		writer.newLine();
		for(Byte b: this.macCredentials.keySet()){
			writer.write(""+b);
			writer.newLine();
			writer.write(Base64.encodeBytes(this.macCredentials.get(b)));
			writer.newLine();
		}
		writer.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  public static SmartcardBackup deserialize(File f){	  
	  try{
		  SmartcardBackup backup = new SmartcardBackup();
		  BufferedReader reader = new BufferedReader(new FileReader(f));
		  
		  int blobSize = Integer.parseInt(""+reader.readLine());
		  for(int i = 0; i < blobSize; i++){
			  SmartcardBlob blob = new SmartcardBlob();
			  URI uri = URI.create(new String(reader.readLine()));
			  blob.blob = Base64.decode(reader.readLine());
			  backup.blobstore.put(uri, blob);			  
		  }
		  
		  URI deviceUri = URI.create(reader.readLine());
		  backup.deviceUri = deviceUri;
		  short deviceID = Short.parseShort(""+reader.readLine());
		  backup.deviceID = deviceID;
		  
		  byte[] macDevice = Base64.decode(reader.readLine());
		  backup.macDevice = macDevice;
		  byte[] macCounters = Base64.decode(reader.readLine());
		  backup.macCounters = macCounters; 
		   
		  int credentialSize = Integer.parseInt(reader.readLine());
		  for(int i = 0; i < credentialSize; i++){
			  byte b = (byte)Integer.parseInt(""+reader.readLine());
			  byte[] cred = Base64.decode(reader.readLine());
			  backup.macCredentials.put(b, cred);
		  }
		  reader.close();
		  return backup;
	  }catch(IOException e){
		  e.printStackTrace();
		  return null;
	  }
  }
}
