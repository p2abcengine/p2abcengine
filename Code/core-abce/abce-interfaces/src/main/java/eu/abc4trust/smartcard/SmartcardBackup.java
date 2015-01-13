//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*                                                                   *
//* This file is licensed under the Apache License, Version 2.0 (the  *
//* "License"); you may not use this file except in compliance with   *
//* the License. You may obtain a copy of the License at:             *
//*   http://www.apache.org/licenses/LICENSE-2.0                      *
//* Unless required by applicable law or agreed to in writing,        *
//* software distributed under the License is distributed on an       *
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY            *
//* KIND, either express or implied.  See the License for the         *
//* specific language governing permissions and limitations           *
//* under the License.                                                *
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
 *
 */
public class SmartcardBackup {
  public Map<URI, byte[]> blobstore;
  //public Map<URI, BigInteger> credentialFragments;
  //public BigInteger devicePublicKey;
  public URI deviceUri;
  public short deviceID;
  public byte[] macDevice;
  public byte[] macCounters;
  public byte[] IV;
  public Map<Byte, byte[]> macCredentials;
  
  
  SmartcardBackup() {
    this.blobstore = new HashMap<URI, byte[]>();
    //credentialFragments = new HashMap<URI, BigInteger>();
    this.macCredentials = new HashMap<Byte, byte[]>();
  }
  
  public void serialize(File f){
	  try {		  
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		if(this.macCounters != null){
			writer.write("!null");
			writer.newLine();
			writer.write(Base64.encodeBytes(this.macCounters));
		}else{
			writer.write("null");
		}
		writer.newLine();
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
		  String useCounters = reader.readLine();
		  if(useCounters.equals("null")){
			  backup.macCounters = null;
		  }else{
			  byte[] macCounters = Base64.decode(reader.readLine());
			  backup.macCounters = macCounters; 
		  }		  
		  reader.close();
		  return backup;
	  }catch(IOException e){
		  e.printStackTrace();
		  return null;
	  }
  }
}
