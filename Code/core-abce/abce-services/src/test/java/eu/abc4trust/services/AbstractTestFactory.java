//* Licensed Materials - Property of                                  *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
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

package eu.abc4trust.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuancePolicyAndAttributes;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.util.XmlUtils;

public abstract class AbstractTestFactory {

  protected Builder getHttpBuilder(String string, String baseUrl) {
    Client client = Client.create();
    Builder resource = client.resource(baseUrl + string)
        .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML);
    return resource;
  }

  public IssuancePolicyAndAttributes loadIssuancePolicyAndAttributes(
                                                                     String issuancePolicyAndAttributesFile) throws IOException,
                                                                     JAXBException, SAXException {
    InputStream is = FileSystem
        .getInputStream(issuancePolicyAndAttributesFile);
    IssuancePolicyAndAttributes issuancePolicyAndAttributes = (IssuancePolicyAndAttributes) XmlUtils
        .getObjectFromXML(is, false);
    return issuancePolicyAndAttributes;
  }

  public CredentialSpecification getCredentialSpecificationForTest(){
    try{
      return FileSystem.loadXmlFromResource("src/test/resources/credentialSpecificationSimpleIdentitycard.xml");
    }catch(Exception e){
      return null;
    }
  }

  public PresentationPolicyAlternatives getPresentationPolicyAlternativesForTest(){
    try{
      return FileSystem.loadXmlFromResource("src/test/resources/presentationPolicySimpleIdentitycard.xml");
    }catch(Exception e){
      return null;
    }
  }
  
  public PresentationPolicyAlternatives getPresentationPolicyAlternativesForInspectionTest(){
    try{
      return FileSystem.loadXmlFromResource("src/test/resources/presentationPolicySimpleIdentitycardInspection.xml");
    }catch(Exception e){
      return null;
    }
  }

  public IssuancePolicyAndAttributes getIssuancePolicyAndAttributesForTest(){
    try{
      return FileSystem.loadXmlFromResource("src/test/resources/issuancePolicyAndAttributes.xml");
    }catch(Exception e){
      return null;
    }
  }

  public void deleteStorageDirectory() {
    File directory1 = new File("target" + File.separatorChar
      + "issuer_storage");
    File directory2 = new File("issuer_storage");
    
    File directory3 = new File("target" + File.separatorChar
    	      + "revocation_storage");
    File directory4 = new File("revocation_storage");
    	    

    this.delete(directory1);
    this.delete(directory2);
    this.delete(directory3);
    this.delete(directory4);
    
    directory1.mkdir();
    directory2.mkdir();
    directory3.mkdir();
    directory4.mkdir();
  }

  public void deleteResourcesDirectory() {
    File directory1 = new File("target" + File.separatorChar
      + "issuer_resources");
    File directory2 = new File("issuer_resources");

    this.delete(directory1);
    this.delete(directory2);

    directory1.mkdir();
    directory2.mkdir();
  }

  public void deleteVerifierStorage() {
    File directory1 = new File("target" + File.separatorChar
      + "verifier_storage");

    this.delete(directory1);

    directory1.mkdir();
  }
  
  private void delete(File directory) {
    if (directory.exists()) {
      this.deleteBody(directory);
    }

  }

  private void deleteBody(File file) {
    if (file.isDirectory()) {

      // directory is empty, then delete it
      if (file.list().length == 0) {

        file.delete();
        System.out.println("Directory is deleted : "
            + file.getAbsolutePath());

      } else {

        // list all the directory contents
        String files[] = file.list();

        for (String temp : files) {
          // construct the file structure
          File fileDelete = new File(file, temp);

          // recursive delete
          this.deleteBody(fileDelete);
        }

        // check the directory again, if empty then delete it
        if (file.list().length == 0) {
          file.delete();
          System.out.println("Directory is deleted : "
              + file.getAbsolutePath());
        }
      }

    } else {
      // if file, then delete it
      file.delete();
      System.out.println("File is deleted : " + file.getAbsolutePath());
    }
  }
}
