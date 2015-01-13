//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

package eu.abc4trust.ui.idSelection;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.abc4trust.exceptions.IdentitySelectionException;
import eu.abc4trust.returnTypes.ObjectFactoryReturnTypes;
import eu.abc4trust.returnTypes.UiIssuanceArguments;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.xml.util.XmlUtils;

public class IdentitySelectionUiPrinter implements IdentitySelectionUi {

  private final IdentitySelectionUi is;
  private static final String PATH = "target/outputXmlUi/";

  @Inject
  public IdentitySelectionUiPrinter(@Named("RealIdSelector") IdentitySelectionUi is) {
    this.is = is;
  }

  @Override
  public UiPresentationReturn selectPresentationTokenDescription(UiPresentationArguments args) throws IdentitySelectionException {
    BigInteger r = new BigInteger(30, new SecureRandom());
    String filename1 = PATH + "ids-p-" + r + "-q";
    String filename2 = PATH + "ids-p-" + r + "-r";

    try {
      (new File(PATH)).mkdir();
      ByteArrayOutputStream xml = XmlUtils.toXmlAsBaos(ObjectFactoryReturnTypes.wrap(args), false);
      OutputStream out = new FileOutputStream(filename1);
      xml.writeTo(out);
      out.close();
      System.out.println("Stored id selector UI call  at " + filename1);
      
      // Try to read the object back
      FileInputStream fis = new FileInputStream(filename1);
      UiPresentationArguments serArg =
          (UiPresentationArguments) XmlUtils.getObjectFromXML(fis, false);
      fis.close();
      assert(serArg != null);
      
    } catch (Exception e) {
      System.err.println("Could not store id selector UI call  at " + filename1);
    }

    UiPresentationReturn ret = is.selectPresentationTokenDescription(args);

    try {
      ByteArrayOutputStream xml = XmlUtils.toXmlAsBaos(ObjectFactoryReturnTypes.wrap(ret), false);
      OutputStream out = new FileOutputStream(filename2);
      xml.writeTo(out);
      out.close();
      System.out.println("Stored id selector UI return  at " + filename2);
      
      // Try to read the object back
      FileInputStream fis = new FileInputStream(filename2);
      UiPresentationReturn serArg =
          (UiPresentationReturn) XmlUtils.getObjectFromXML(fis, false);
      fis.close();
      assert(serArg != null);
      
    } catch (Exception e) {
      System.err.println("Could not store id selector UI return  at " + filename2);
    }

    return ret;
  }


  @Override
  public UiIssuanceReturn selectIssuanceTokenDescription(UiIssuanceArguments args) throws IdentitySelectionException {
    BigInteger r = new BigInteger(30, new SecureRandom());
    String filename1 = PATH + "ids-i-" + r + "-q";
    String filename2 = PATH + "ids-i-" + r + "-r";

    try {
      (new File(PATH)).mkdir();
      ByteArrayOutputStream xml = XmlUtils.toXmlAsBaos(ObjectFactoryReturnTypes.wrap(args), false);
      OutputStream out = new FileOutputStream(filename1);
      xml.writeTo(out);
      out.close();
      System.out.println("Stored id selector UI issuance call  at " + filename1);
      
      // Try to read the object back
      FileInputStream fis = new FileInputStream(filename1);
      UiIssuanceArguments serArg =
          (UiIssuanceArguments) XmlUtils.getObjectFromXML(fis, false);
      fis.close();
      assert(serArg != null);
      
    } catch (Exception e) {
      System.err.println("Could not store id selector UI issuance call  at " + filename1);
      System.err.println(e.getMessage());
    }

    UiIssuanceReturn ret = is.selectIssuanceTokenDescription(args);

    try {
      ByteArrayOutputStream xml = XmlUtils.toXmlAsBaos(ObjectFactoryReturnTypes.wrap(ret), false);
      OutputStream out = new FileOutputStream(filename2);
      xml.writeTo(out);
      out.close();
      System.out.println("Stored id selector UI issuance return  at " + filename2);
      
      // Try to read the object back
      FileInputStream fis = new FileInputStream(filename2);
      UiIssuanceReturn serArg =
          (UiIssuanceReturn) XmlUtils.getObjectFromXML(fis, false);
      fis.close();
      assert(serArg != null);
    } catch (Exception e) {
      System.err.println("Could not store id selector UI issuance return  at " + filename2);
    }

    return ret;
  }

}
