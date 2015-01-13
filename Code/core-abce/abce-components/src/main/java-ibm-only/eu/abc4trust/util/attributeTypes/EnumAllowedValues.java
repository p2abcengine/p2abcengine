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

package eu.abc4trust.util.attributeTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.abc4trust.xml.AttributeDescription;

public class EnumAllowedValues {
  private final List<String> allowedValues;
  private final Map<String, Integer> positions;
  
  public EnumAllowedValues() {
    allowedValues = new ArrayList<String>();
    positions = new HashMap<String, Integer>();
  }
  
  public EnumAllowedValues(AttributeDescription desc) {
    this();
    if(desc.getAllowedValue() != null) {
      for(Object o: desc.getAllowedValue()) {
        addAllowedValue(o.toString());
      }
    }
  }
  
  public EnumAllowedValues(List<String> allowedValues) {
    this();
    for(String s: allowedValues) {
      addAllowedValue(s);
    }
  }
  
  public void addAllowedValue(String value) {
    if(positions.containsKey(value)) {
      throw new RuntimeException("Duplicate enum value");
    }
    positions.put(value, allowedValues.size());
    allowedValues.add(value);
  }
  
  public boolean isAllowedValue(String value) {
    return positions.containsKey(value);
  }
  
  public int getPosition(String value) {
    return positions.get(value);
  }
  
  public List<String> getAllowedValues() {
    return Collections.unmodifiableList(allowedValues);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((allowedValues == null) ? 0 : allowedValues.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    EnumAllowedValues other = (EnumAllowedValues) obj;
    if (allowedValues == null) {
      if (other.allowedValues != null) return false;
    } else if (!allowedValues.equals(other.allowedValues)) return false;
    return true;
  }
}
