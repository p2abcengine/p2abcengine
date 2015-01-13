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
package eu.abc4trust.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ByteSerializer {
  public static Object readFromBytes(byte[] item) {
    if (item == null) {
      return null;
    }
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(item);) {
      try (ObjectInputStream objectInput = new ObjectInputStream(byteArrayInputStream);) {
        return objectInput.readObject();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static  byte[] writeAsBytes(Object o) {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();) {
      try (ObjectOutputStream objectOutput = new ObjectOutputStream(byteArrayOutputStream);) {
        objectOutput.writeObject(o);
        return byteArrayOutputStream.toByteArray();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
