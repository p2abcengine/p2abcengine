//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

package eu.abc4trust.returnTypes;

import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.IssuanceMessage;

public class IssuMsgOrCredDesc {

  public IssuanceMessage im;
  public CredentialDescription cd;
  
  public IssuMsgOrCredDesc() {
    this.im = null;
    this.cd = null;
  }
  
  public IssuMsgOrCredDesc(IssuanceMessage im) {
    this.im = im;
    this.cd = null;
  }
  
  public IssuMsgOrCredDesc(IssuanceReturn ir) {
    if(ir.uia != null) {
      throw new RuntimeException("Cannot convert IssuanceReturn->IssuMsgOrCredDesc containing UiIssuanceArguments");
    }
    this.im = ir.im;
    this.cd = ir.cd;
  }

  public IssuMsgOrCredDesc(CredentialDescription credentialDescription) {
    this.cd = credentialDescription;
    this.im = null;
  }
}
