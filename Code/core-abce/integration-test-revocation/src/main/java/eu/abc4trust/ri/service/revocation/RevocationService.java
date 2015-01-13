//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

package eu.abc4trust.ri.service.revocation;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.revocation.RevocationHelper;
import eu.abc4trust.ri.servicehelper.revocation.RevocationHelper.RevocationReferences;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeList;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.NonRevocationEvidenceUpdate;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * class RevocationService This is a demo implementation. This particular service will either accept
 * or fail all RevocationMessage requests in a number of rounds. It is expected that 3d party
 * providers implement this interface and do what needs to be done
 */
@Path("/")
public class RevocationService {
  private final Logger log = Logger.getLogger(RevocationService.class.getName());

  public static final URI soderhamnRevocationAuthority = URI
      .create("urn:soderhamn:revocationauthority:default");
  public static final URI patrasRevocationAuthority = URI
      .create("urn:patras:revocationauthority:default");

  private ObjectFactory of = new ObjectFactory();

  public RevocationService() {
    System.out.println("RevocationService created");
  }

  public void initRevocationHelper(String testcase) throws Exception {
    System.out.println("RevocationService - initHelper : " + testcase);
    String folderName;
    if (new File("target").exists()) {
      folderName = "target";

    } else {
      folderName = "integration-test-revocation/target";
    }
    String fileStoragePrefix = folderName + "/revocation_";
    String systemParametersResource = folderName + "/issuer_" + AbstractHelper.SYSTEM_PARAMS_XML_NAME;
    SystemParameters systemParams = FileSystem.loadXmlFromResource(systemParametersResource);

    List<IssuerParameters> issuerParamsList =
        FileSystem.findAndLoadXmlResourcesInDir(folderName, "issuer_params");

    String[] credSpecResourceList;

    URI revocationInfoReference =
        URI.create("http://localhost:9094/integration-test-revocation/revocation/getrevocationinformation");
    URI nonRevocationEvidenceReference =
        URI.create("http://localhost:9094/integration-test-revocation/revocation/generatenonrevocationevidence");
    URI nonRevocationUpdateReference =
        URI.create("http://localhost:9094/integration-test-revocation/revocation/generatenonrevocationevidenceupdate");;
    RevocationReferences revocationReferences;

    boolean soderhamn = "soderhamn".equals(testcase);
    if (false && soderhamn) {
      revocationReferences =
          new RevocationReferences(soderhamnRevocationAuthority, revocationInfoReference,
              nonRevocationEvidenceReference, nonRevocationUpdateReference);
      credSpecResourceList =
          new String[] {"/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml"};
    } else {
      revocationReferences =
          new RevocationReferences(patrasRevocationAuthority, revocationInfoReference,
              nonRevocationEvidenceReference, nonRevocationUpdateReference);
      credSpecResourceList =
          new String[] {"/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversityWithRevocation.xml"};
    }
    List<CredentialSpecification> credSpecList =
        FileSystem.loadXmlListFromResources(credSpecResourceList);

    RevocationHelper.resetInstance();
    RevocationHelper.initInstance(fileStoragePrefix, fileStoragePrefix, systemParams,
        issuerParamsList, credSpecList, revocationReferences);
  }

  @GET()
  @Path("/init/{testcase}")
  @Produces(MediaType.TEXT_PLAIN)
  public String init(@PathParam("testcase") final String testcase) throws Exception {
    System.out.println("revocation service.init - for testcase : " + testcase);
    initRevocationHelper(testcase);
    return "OK";
  }

  @POST()
  @Path("/revocation/generatenonrevocationevidence/{revParUid}")
  @Produces(MediaType.TEXT_XML)
  public JAXBElement<NonRevocationEvidence> generateNonRevocationEvidence(
      @PathParam("revParUid") final URI revParUid, JAXBElement<AttributeList> attributeList)
      throws Exception {
    this.log.info("RevocationService - generatenonrevocationevidence");

    this.validateRevocationParametersUid(revParUid);

    List<Attribute> attributes = attributeList.getValue().getAttributes();
    RevocationAbcEngine engine = RevocationHelper.getInstance().engine;
    NonRevocationEvidence revInfo = engine.generateNonRevocationEvidence(revParUid, attributes);

    return this.of.createNonRevocationEvidence(revInfo);
  }

  private void validateRevocationParametersUid(final URI revParUid) throws Exception {
    if (revParUid == null) {
      throw new Exception("Revocation Parameters UID is null!");
    }
  }

  @POST()
  @Path("/revocation/generatenonrevocationevidenceupdate/{revParUid}")
  @Produces(MediaType.TEXT_XML)
  public JAXBElement<NonRevocationEvidenceUpdate> generateNonRevocationEvidenceUpdate(
      @PathParam("revParUid") final URI revParUid, @QueryParam("epoch") final int epoch)
      throws Exception {
    this.log.info("RevocationService - generatenonrevocationevidenceupdate");

    this.validateRevocationParametersUid(revParUid);

    RevocationAbcEngine engine = RevocationHelper.getInstance().engine;
    NonRevocationEvidenceUpdate revInfo =
        engine.generateNonRevocationEvidenceUpdate(revParUid, epoch);

    return this.of.createNonRevocationEvidenceUpdate(revInfo);
  }

  @GET()
  @Path("/revocation/getrevocationinformation/{revParUid}")
  @Produces(MediaType.TEXT_XML)
  public JAXBElement<RevocationInformation> getRevocationInformation(
      @PathParam("revParUid") final URI revParUid) throws Exception {
    this.log.info("RevocationService - getrevocationinformation " + revParUid);

    this.validateRevocationParametersUid(revParUid);

    RevocationAbcEngine engine = RevocationHelper.getInstance().engine;
    RevocationInformation revInfo = engine.updateRevocationInformation(revParUid);
    return this.of.createRevocationInformation(revInfo);
  }

  @POST()
  @Path("/revocation/revokeAttribute/{revParUid}")
  // @Produces(MediaType.APPLICATION_XML)
  public JAXBElement<RevocationInformation> revoke(@PathParam("revParUid") final URI revParUid,
      final JAXBElement<Attribute> in_jaxb) throws Exception {
    System.out.println("=========== R E V O K E ===========");
    Attribute in = in_jaxb.getValue();
    System.out.println("revoke attribute! " + revParUid + " " + in.getAttributeUID() + " : "
        + in.getAttributeValue());
    System.out.println("XML " + XmlUtils.toXml(of.createAttribute(in), false));
    List<Attribute> attributes = new ArrayList<Attribute>();
    attributes.add(in);
    RevocationInformation ri = RevocationHelper.getInstance().engine.revoke(revParUid, attributes);
    System.out.println("RevocationInformation : " + ri + " : " + ri.getRevocationInformationUID());
    return of.createRevocationInformation(ri);
  }

  @POST()
  @Path("/revocation/revoke/{revParUid}")
  @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
  @Produces(MediaType.TEXT_XML)
  public JAXBElement<RevocationInformation> revokeList(@PathParam("revParUid") final URI revParUid,
      final JAXBElement<AttributeList> in_jaxb) throws Exception {
    this.log.info("RevocationService - revoke");
    AttributeList in = in_jaxb.getValue();
    
    this.validateRevocationParametersUid(revParUid);

    List<Attribute> attributes = in.getAttributes();
    RevocationAbcEngine engine = RevocationHelper.getInstance().engine;
    RevocationInformation ri = engine.revoke(revParUid, attributes);

    return this.of.createRevocationInformation(ri);
  }

  @GET()
  @Path("/revocation/updaterevocationinformation/{revParUid}")
  public JAXBElement<RevocationInformation> updateRevocationInformation(
      @PathParam("revParUid") final URI revParUid) throws Exception {
    this.log.info("RevocationService - updaterevocationinformation");

    this.validateRevocationParametersUid(revParUid);

    RevocationAbcEngine engine = RevocationHelper.getInstance().engine;
    RevocationInformation revInfo = engine.updateRevocationInformation(revParUid);

    return this.of.createRevocationInformation(revInfo);
  }

}
