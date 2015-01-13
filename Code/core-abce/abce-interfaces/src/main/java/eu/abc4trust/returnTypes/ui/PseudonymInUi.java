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

package eu.abc4trust.returnTypes.ui;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;

@XmlType(namespace = "http://abc4trust.eu/wp2/abcschemav1.0")
public class PseudonymInUi {
  @XmlID
  @XmlAttribute
  public String uri;
  
  @XmlElement
  public Pseudonym pseudonym;
  
  @XmlElement
  public PseudonymMetadata metadata;
  
  
  public PseudonymInUi() {
  }
  
  public PseudonymInUi(PseudonymWithMetadata pwm) {
    this.uri = pwm.getPseudonym().getPseudonymUID().toString();
    this.pseudonym = pwm.getPseudonym();
    this.metadata = pwm.getPseudonymMetadata();
  }

  @Override
  public String toString() {
    return "PseudonymInUi [uri=" + uri + ", pseudonym=" + pseudonym + ", metadata=" + metadata
        + "]";
  }
}
