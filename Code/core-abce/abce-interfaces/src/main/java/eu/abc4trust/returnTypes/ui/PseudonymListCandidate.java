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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.abc4trust.returnTypes.ui.adapters.PseudonymAdapter;

@XmlType(namespace = "http://abc4trust.eu/wp2/abcschemav1.0")
public class PseudonymListCandidate {

  @XmlAttribute
  public int candidateId;
  
  @XmlElementWrapper
  @XmlElement(name="pseudonym")
  @XmlJavaTypeAdapter(PseudonymAdapter.class)
  public List<PseudonymInUi> pseudonyms;
  
  
  public PseudonymListCandidate() {
    this.pseudonyms = new ArrayList<PseudonymInUi>();
  }


  @Override
  public String toString() {
    return "PseudonymListCandidate [candidateId=" + candidateId + ", pseudonyms=" + pseudonyms
        + "]";
  }
}
