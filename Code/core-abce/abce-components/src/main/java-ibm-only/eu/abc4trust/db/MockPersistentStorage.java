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
package eu.abc4trust.db;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MockPersistentStorage implements PersistentStorage {

  private final Map<SimpleParamTypes, Map<URI, byte[]>> storage;
  private final Map<TokenTypes, Map<ByteArray, URI>> nymsInToken;
  private final Map<String, Map<URI, MiniCred>> creds;
  private final Map<String, Map<URI, MiniNym>> nyms;
  private final Map<String, Map<URI, byte[]>> secrets;
  private final Map<URI, Map<URI, MiniRevInfo>> revInfos;

  public MockPersistentStorage() {
    storage = new HashMap<SimpleParamTypes, Map<URI, byte[]>>();
    for (SimpleParamTypes type : SimpleParamTypes.values()) {
      storage.put(type, new HashMap<URI, byte[]>());
    }
    nymsInToken = new HashMap<TokenTypes, Map<ByteArray, URI>>();
    for (TokenTypes type : TokenTypes.values()) {
      nymsInToken.put(type, new HashMap<ByteArray, URI>());
    }
    creds = new HashMap<>();
    nyms = new HashMap<>();
    secrets = new HashMap<>();
    revInfos = new HashMap<>();
  }

  @Override
  public void shutdown() {
    //Does nothing
  }
  
  @Override
  public boolean insertItem(SimpleParamTypes table, URI key, byte[] value) {
    value = Arrays.copyOf(value, value.length);
    if (storage.get(table).containsKey(key)) {
      return false;
    }
    storage.get(table).put(key, value);
    return true;
  }
  
  @Override
  public int replaceItem(SimpleParamTypes table, URI key, byte[] value) {
    value = Arrays.copyOf(value, value.length);
    storage.get(table).put(key, value);
    return 1;
  }

  @Override
  public byte[] getItem(SimpleParamTypes table, URI key) {
    byte[] item = storage.get(table).get(key);
    if (item != null) {
      item = Arrays.copyOf(item, item.length);
    }
    return item;
  }
  
  @Override
  public byte[] getItemAndDelete(SimpleParamTypes table, URI key) {
    byte[] item = storage.get(table).remove(key);
    if (item != null) {
      item = Arrays.copyOf(item, item.length);
    }
    return item;
  }

  @Override
  public boolean deleteItem(SimpleParamTypes table, URI key) {
    return storage.get(table).remove(key) != null;
  }

  @Override
  public boolean updateItem(SimpleParamTypes table, URI key, byte[] value) {
    value = Arrays.copyOf(value, value.length);
    if (!storage.get(table).containsKey(key)) {
      return false;
    }
    storage.get(table).put(key, value);
    return true;
  }

  @Override
  public List<URI> listItems(SimpleParamTypes table) {
    return new ArrayList<URI>(storage.get(table).keySet());
  }

  private SimpleParamTypes correspondingTable(TokenTypes table) {
    switch (table) {
      case ISSUANCE_TOKEN:
        return SimpleParamTypes.ISSUANCE_TOKEN;
      case VERIFIER_TOKEN:
        return SimpleParamTypes.VERIFIER_TOKEN;
      default:
        throw new RuntimeException("Unknown token type");

    }
  }

  @Override
  public boolean associatePseudonym(TokenTypes table, URI tokenId, byte[] pseudonymValue) {
    ByteArray val = new ByteArray(pseudonymValue);
    if (!storage.get(correspondingTable(table)).containsKey(tokenId)) {
      return false;
    }
    nymsInToken.get(table).put(val, tokenId);
    return true;
  }

  @Override
  public boolean isPseudonymInToken(TokenTypes table, byte[] pseudonymValue) {
    URI tokenId = nymsInToken.get(table).get(new ByteArray(pseudonymValue));
    if (null == tokenId) {
      return false;
    } else {
      return storage.get(correspondingTable(table)).containsKey(tokenId);
    }
  }

  @Override
  public boolean insertCredential(URI key, String username, URI issuer, URI credSpec, byte[] value) {
    value = Arrays.copyOf(value, value.length);
    if (!creds.containsKey(username)) {
      creds.put(username, new HashMap<URI, MiniCred>());
    }
    if (creds.get(username).containsKey(key)) {
      return false;
    }
    MiniCred c = new MiniCred(issuer, credSpec, value);
    creds.get(username).put(key, c);
    return true;
  }

  @Override
  public byte[] getCredential(URI key, String username) {
    if (!creds.containsKey(username)) {
      return null;
    } else {
      MiniCred item = creds.get(username).get(key);
      if (item != null) {
        return Arrays.copyOf(item.data, item.data.length);
      } else {
        return null;
      }
    }
  }

  @Override
  public boolean deleteCredential(URI key, String username) {
    if (!creds.containsKey(username)) {
      return false;
    }
    return creds.get(username).remove(key) != null;
  }

  @Override
  public boolean updateCredential(URI key, String username, byte[] value) {
    value = Arrays.copyOf(value, value.length);
    if (!creds.containsKey(username)) {
      return false;
    }
    MiniCred c = creds.get(username).get(key);
    if (c == null) {
      return false;
    }
    c.data = value;
    return true;
  }

  @Override
  public List<URI> listCredentials(String username) {
    if (!creds.containsKey(username)) {
      return Collections.emptyList();
    } else {
      return new ArrayList<URI>(creds.get(username).keySet());
    }
  }

  @Override
  public List<URI> listCredentials(String username, List<URI> issuerList, List<URI> credSpecList) {
    if (!creds.containsKey(username)) {
      return Collections.emptyList();
    } else {
      Set<URI> issuers = new HashSet<>(issuerList);
      Set<URI> credSpecs = new HashSet<>(credSpecList);
      List<URI> ret = new ArrayList<>();
      for (Entry<URI, MiniCred> entry : creds.get(username).entrySet()) {
        if (issuers.contains(entry.getValue().issuer)
            && credSpecs.contains(entry.getValue().credSpec)) {
          ret.add(entry.getKey());
        }
      }
      return ret;
    }
  }

  @Override
  public boolean insertSecret(URI key, String username, byte[] value) {
    value = Arrays.copyOf(value, value.length);
    if (!secrets.containsKey(username)) {
      secrets.put(username, new HashMap<URI, byte[]>());
    }
    if (secrets.get(username).containsKey(key)) {
      return false;
    }
    secrets.get(username).put(key, value);
    return true;
  }

  @Override
  public byte[] getSecret(URI key, String username) {
    if (!secrets.containsKey(username)) {
      return null;
    } else {
      byte[] item = secrets.get(username).get(key);
      if (item != null) {
        item = Arrays.copyOf(item, item.length);
      }
      return item;
    }
  }

  @Override
  public boolean deleteSecret(URI key, String username) {
    if (!secrets.containsKey(username)) {
      return false;
    }
    return secrets.get(username).remove(key) != null;
  }

  @Override
  public boolean updateSecret(URI key, String username, byte[] value) {
    value = Arrays.copyOf(value, value.length);
    if (!secrets.containsKey(username)) {
      return false;
    }
    if (!secrets.get(username).containsKey(key)) {
      return false;
    }
    secrets.get(username).put(key, value);
    return true;
  }

  @Override
  public List<URI> listSecrets(String username) {
    if (!secrets.containsKey(username)) {
      return Collections.emptyList();
    } else {
      return new ArrayList<URI>(secrets.get(username).keySet());
    }
  }

  @Override
  public boolean insertPseudonym(URI key, String username, String scope, boolean exclusive,
      byte[] pseudonymValue, byte[] data) {
    data = Arrays.copyOf(data, data.length);
    if (!nyms.containsKey(username)) {
      nyms.put(username, new HashMap<URI, MiniNym>());
    }
    if (nyms.get(username).containsKey(key)) {
      return false;
    }
    MiniNym c = new MiniNym(scope, pseudonymValue, data, exclusive);
    nyms.get(username).put(key, c);
    return true;
  }

  @Override
  public byte[] getPseudonym(URI key, String username) {
    if (!nyms.containsKey(username)) {
      return null;
    } else {
      MiniNym item = nyms.get(username).get(key);
      if (item != null) {
        return Arrays.copyOf(item.data, item.data.length);
      } else {
        return null;
      }
    }
  }

  @Override
  public boolean deletePseudonym(URI key, String username) {
    if (!nyms.containsKey(username)) {
      return false;
    }
    return nyms.get(username).remove(key) != null;
  }

  @Override
  public boolean updatePseudonym(URI key, String username, byte[] value) {
    value = Arrays.copyOf(value, value.length);
    if (!nyms.containsKey(username)) {
      return false;
    }
    MiniNym c = nyms.get(username).get(key);
    if (c == null) {
      return false;
    }
    c.data = value;
    return true;
  }

  @Override
  public List<URI> listPseudonyms(String username) {
    if (!nyms.containsKey(username)) {
      return Collections.emptyList();
    } else {
      return new ArrayList<URI>(nyms.get(username).keySet());
    }
  }

  @Override
  public List<URI> listPseudonyms(String username, String scope) {
    if (!nyms.containsKey(username)) {
      return Collections.emptyList();
    } else {
      List<URI> ret = new ArrayList<>();
      for (Entry<URI, MiniNym> entry : nyms.get(username).entrySet()) {
        if (scope.equals(entry.getValue().scope)) {
          ret.add(entry.getKey());
        }
      }
      return ret;
    }
  }

  @Override
  public List<URI> listPseudonyms(String username, String scope, boolean isExclusive) {
    if (!nyms.containsKey(username)) {
      return Collections.emptyList();
    } else {
      List<URI> ret = new ArrayList<>();
      for (Entry<URI, MiniNym> entry : nyms.get(username).entrySet()) {
        if (scope.equals(entry.getValue().scope) && entry.getValue().exclusive == isExclusive) {
          ret.add(entry.getKey());
        }
      }
      return ret;
    }
  }

  @Override
  public List<URI> listPseudonyms(String username, byte[] pseudonymValue) {
    if (!nyms.containsKey(username)) {
      return Collections.emptyList();
    } else {
      List<URI> ret = new ArrayList<>();
      for (Entry<URI, MiniNym> entry : nyms.get(username).entrySet()) {
        if (Arrays.equals(pseudonymValue, entry.getValue().value)) {
          ret.add(entry.getKey());
        }
      }
      return ret;
    }
  }

  @Override
  public boolean insertRevocationInformation(URI key, URI rev_auth, Calendar created, byte[] data) {
    data = Arrays.copyOf(data, data.length);
    created = (Calendar)created.clone();
    if (!revInfos.containsKey(rev_auth)) {
      revInfos.put(rev_auth, new HashMap<URI, MiniRevInfo>());
    }
    if (revInfos.get(rev_auth).containsKey(key)) {
      return false;
    }
    MiniRevInfo c = new MiniRevInfo(data, created);
    revInfos.get(rev_auth).put(key, c);
    return true;
  }

  @Override
  public byte[] getRevocationInformation(URI key, URI rev_auth) {
    if (!revInfos.containsKey(rev_auth)) {
      return null;
    } else {
      MiniRevInfo item = revInfos.get(rev_auth).get(key);
      if (item != null) {
        return Arrays.copyOf(item.data, item.data.length);
      } else {
        return null;
      }
    }
  }

  @Override
  public byte[] getLatestRevocationInformation(URI rev_auth) {
    if (!revInfos.containsKey(rev_auth)) {
      return null;
    } else {
      Entry<URI, MiniRevInfo> best = null;
      for (Entry<URI, MiniRevInfo> entry : revInfos.get(rev_auth).entrySet()) {
        if(best==null) {
          best = entry;
        } else if(entry.getValue().created.after(best.getValue().created)) {
          best = entry;
        }
      }
      if(best == null) {
        return null;
      } else {
        return Arrays.copyOf(best.getValue().data, best.getValue().data.length);
      }
    }
  }


  private class MiniCred {
    final URI issuer;
    final URI credSpec;
    byte[] data;

    public MiniCred(URI issuer, URI credSpec, byte[] data) {
      this.issuer = issuer;
      this.credSpec = credSpec;
      this.data = data;
    }
  }


  private class MiniNym {
    final String scope;
    final byte[] value;
    byte[] data;
    final boolean exclusive;

    public MiniNym(String scope, byte[] value, byte[] data, boolean exclusive) {
      this.scope = scope;
      this.value = value;
      this.data = data;
      this.exclusive = exclusive;
    }
  }


  private class MiniRevInfo {
    final byte[] data;
    final Calendar created;

    public MiniRevInfo(byte[] data, Calendar created) {
      this.data = data;
      this.created = created;
    }
  }

  private class ByteArray {
    private final byte[] data;

    public ByteArray(byte[] pseudonymValue) {
      data = Arrays.copyOf(pseudonymValue, pseudonymValue.length);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(data);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      ByteArray other = (ByteArray) obj;
      if (!Arrays.equals(data, other.data)) return false;
      return true;
    }
  }
}
