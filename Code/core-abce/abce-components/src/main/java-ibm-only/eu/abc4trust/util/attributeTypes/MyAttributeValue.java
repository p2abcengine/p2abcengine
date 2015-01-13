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

import java.math.BigInteger;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import eu.abc4trust.util.attributeEncoding.MyAttributeEncoding;
import eu.abc4trust.util.attributeEncoding.MyAttributeEncodingFactory;


public abstract class MyAttributeValue {
  
  private final EnumAllowedValues allowedValues;
  
  public MyAttributeValue(/*Nullable*/ EnumAllowedValues allowedValues) {
    if(allowedValues != null && allowedValues.getAllowedValues().size() > 0) {
      this.allowedValues = allowedValues;
      if(!isEnumeration()) {
        throw new RuntimeException("This type is not an ennumeration, but EnumAllowedValues specified.");
      }
    } else {
      // Don't allow empty allowedValues
      this.allowedValues = null;
      if(isEnumeration()) {
        throw new RuntimeException("This type is an ennumeration, but no EnumAllowedValues specified.");
      }
    }
  }
  
  abstract protected boolean isEquals(MyAttributeValue lhs);

  abstract protected boolean isLess(MyAttributeValue myAttributeValue);

  private boolean isNotEquals(MyAttributeValue lhs) {
    return !isEquals(lhs);
  }

  private boolean isLessOrEqual(MyAttributeValue lhs) {
    return isLess(lhs) || isEquals(lhs);
  }
  
  /**
   * If this attribute has an associated encoding, then return the attribute encoded as an
   * integer. Else return null.
   * @return The cryptographic integer encoding of this attribute, or NULL.
   */
  public BigInteger getIntegerValueOrNull() {
    if(this instanceof MyAttributeEncoding) {
      return ((MyAttributeEncoding)this).getIntegerValue();
    } else {
      return null;
    }
  }
  
  /**
   * Return the attribute value encoded as an integer under the specified encoding
   * @param encoding
   * @return
   */
  public BigInteger getIntegerValueUnderEncoding(URI encoding) {
    return recodeAttribute(encoding, allowedValues).getIntegerValueOrNull();
  }
  
  /**
   * Returns the attribute value encoded as an integer under the same encoding
   * as the values given in parameter.
   * If none of the values in the parameter set have an encoding, or if they have
   * several different encodings, an exception is thrown.
   * @param values
   * @return
   */
  public BigInteger getCompatibleIntegerValue(Set<MyAttributeValue> values) {
    Set<URI> encodings = new HashSet<URI>();
    for(MyAttributeValue value: values) {
      URI encoding = value.getEncodingOrNull();
      if(encoding != null) {
        encodings.add(encoding);
      }
    }
    if(encodings.size() == 1) {
      return getIntegerValueUnderEncoding(encodings.iterator().next());
    } else if(encodings.size() == 0) {
      throw new RuntimeException("No encoding detected");
    } else {
      throw new RuntimeException("Too many encodings detected");
    }
  }
  
  /**
   * Return a attribute value with the same value as this one, but with a different encoding
   * @param newEncoding
   * @return
   */
  public MyAttributeValue recodeAttribute(URI newEncoding, /*Nullable*/ EnumAllowedValues eav) {
    return MyAttributeEncodingFactory.parseValueFromEncoding(newEncoding, getValueAsObject(), eav);
  }
  
  private boolean compatible(MyAttributeValue lhs) {
    boolean res;
    if (this instanceof MyAttributeEncoding && lhs instanceof MyAttributeEncoding) {
      res = this.getClass().equals(lhs.getClass());
    } else if(this instanceof MyAttributeEncoding) {
      res =  lhs.getClass().isInstance(this);
    } else if(lhs instanceof MyAttributeEncoding) {
      res =  this.getClass().isInstance(lhs);
    } else {
      res =  this.getClass().equals(lhs.getClass());
    }
    return res;
  }

  public boolean isCompatibleAndEquals(MyAttributeValue lhs) {
    return compatible(lhs) && isEquals(lhs);
  }

  public boolean isCompatibleAndNotEquals(MyAttributeValue lhs) {
    return compatible(lhs) && isNotEquals(lhs);
  }

  public boolean isCompatibleAndLess(MyAttributeValue lhs) {
    return compatible(lhs) && isLess(lhs);
  }

  public boolean isCompatibleAndLessOrEqual(MyAttributeValue lhs) {
    return compatible(lhs) && isLessOrEqual(lhs);
  }
  
  abstract public Object getValueAsObject();
  
  public URI getEncodingOrNull() {
    if(this instanceof MyAttributeEncoding) {
      return ((MyAttributeEncoding)this).getEncoding();
    } else {
      return null;
    }
  }
  
  @Override
  public String toString(){
	  return this.getValueAsObject().toString();
  }
  
  public final EnumAllowedValuesWithIndexer getAllowedValues() {
    if(getIndexer() == null) {
      if(allowedValues != null) {
        throw new RuntimeException("no indexer specified, but allowed values are specified.");
      }
      return null;
    } else {
      return new EnumAllowedValuesWithIndexer(getIndexer(), allowedValues.getAllowedValues());
    }
  }
  
  public final boolean isEnumeration() {
    return (getIndexer() != null);
  }
  
  protected EnumIndexer getIndexer() {
    return null;
  }
}
