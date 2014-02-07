//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
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

package eu.abc4trust.ri.service.inspector;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import eu.abc4trust.ri.servicehelper.inspector.InspectorHelper;
import eu.abc4trust.util.attributeEncoding.MyAttributeEncodingFactory;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;

/**
 */
@Path("/inspector")
public class InspectService {


  private final URI[] inspectorPublicKeyUIDs = {URI.create("urn:soderhamn:inspectorpk")};
  ObjectFactory of = new ObjectFactory();

  public InspectService() {}

  public void initInspectorHelper(String testcase) throws Exception {
    System.out.println("InspectService");
    String fileStoragePrefix;
    String systemParametersResource; 
    if (new File("target").exists()) {
      fileStoragePrefix = "target/inspector_";
      systemParametersResource = "target/issuer_idemix_system_params_bridged";
    } else {
      fileStoragePrefix = "integration-test-inspector/target/inspector_";
      systemParametersResource = "integration-test-inspector/target/issuer_idemix_system_params_bridged";
    }

    String[] credSpecResourceList =
        {"/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml"};

    InspectorHelper.resetInstance();
    InspectorHelper.initInstance(fileStoragePrefix, fileStoragePrefix, systemParametersResource, inspectorPublicKeyUIDs,
        credSpecResourceList);
  }

  @GET()
  @Path("/init/{testcase}")
  @Produces(MediaType.TEXT_PLAIN)
  public String init(@PathParam("testcase") final String testcase) throws Exception {
    System.out.println("inspector service.init - for testcase : " + testcase);
    initInspectorHelper(testcase);
    return "OK";
  }


  /**
   */
  @POST()
  @Path("/inspect")
  @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
  @Produces(MediaType.TEXT_PLAIN)
  public String inspectToken(@QueryParam("issuedValue") String issuedValue,
      final JAXBElement<PresentationToken> presentationToken) throws Exception {

    try {
      System.out.println("InspectService : ===================================================");
      System.out.println("InspectService : issuedValue : " + issuedValue + " : " + presentationToken);
//      System.out.println("Inspect PresentationToken " + XmlUtils.toXml(presentationToken));


      List<Attribute> atts = InspectorHelper.getInstance().inspect(presentationToken.getValue());
      if(atts != null) {
        System.out.println("- inspected attributes : " + atts);
        for(Attribute a : atts) {
          
          System.out.println("--- " + a.getAttributeUID() + " : " + a.getAttributeValue().getClass() + ": " + a.getAttributeValue() + " " + a.getAttributeDescription().getType() + " : " + a.getAttributeDescription().getDataType() + " : " + a.getAttributeDescription().getEncoding());
          MyAttributeValue value = MyAttributeEncodingFactory.recoverValueFromBigInteger(a.getAttributeDescription().getEncoding(), (BigInteger)a.getAttributeValue(), null);
          System.out.println("--- " + value );
          if(issuedValue.equals(value.getValueAsObject().toString())) {
            // every thing ok!
          } else {
            throw new IllegalStateException("Issued AttributeValue must match issued value - issued " + issuedValue + " - inspected : " + value); 
          }
        }
      } else {
        System.out.println("- inspected attributes is null : " + atts);
      }

      return "OK";
    } catch (Exception e) {
      System.err.println("- got Exception from ABCE Engine :  " + e);
      e.printStackTrace();
      throw new Exception("Failed : " + e);
    }
    
  }

}
