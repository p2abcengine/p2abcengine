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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

@org.junit.Ignore("Test requires a database setup.")
public class TestJdbcPersistentStorage {

  private static JdbcPersistentStorage ps;

  @BeforeClass
  public static void setup() {
    ps = new JdbcPersistentStorage(new LocalJdbcDatabase());
  }
  
  @AfterClass
  public static void clenup() {
    ps.shutdown();
  }

  @Test
  public void simpleTest() {
    for (SimpleParamTypes table : SimpleParamTypes.values()) {
      System.out.println(table);
      simpleTest(table);
    }
  }

  private void simpleTest(SimpleParamTypes table) {
    String prefix = UUID.randomUUID().toString();
    byte[] data = ("HelloWorld-" + prefix).getBytes();
    URI key = URI.create("test-" + prefix);

    ps.clearTableForTest(table);
    assertTrue(ps.insertItem(table, key, data));
    byte[] ret = ps.getItem(table, key);
    assertTrue(Arrays.equals(data, ret));
    byte[] newdata = ("HowAreYou-" + prefix).getBytes();
    assertTrue(ps.updateItem(table, key, newdata));
    ret = ps.getItem(table, key);
    assertTrue(Arrays.equals(newdata, ret));
    List<URI> l = ps.listItems(table);
    assertTrue(l.contains(key));
    assertTrue(ps.deleteItem(table, key));

    URI key2 = URI.create("test-2-" + prefix);
    ps.replaceItem(table, key2, data);
    ret = ps.getItem(table, key2);
    assertTrue(Arrays.equals(data, ret));
    ps.replaceItem(table, key2, newdata);
    ret = ps.getItem(table, key2);
    assertTrue(Arrays.equals(newdata, ret));
    ret = ps.getItemAndDelete(table, key2);
    assertTrue(Arrays.equals(newdata, ret));
    ret = ps.getItem(table, key2);
    assertNull(ret);
  }

  @Test
  public void simpleTokenTest() {
    simpleTokenTest(SimpleParamTypes.ISSUANCE_TOKEN, TokenTypes.ISSUANCE_TOKEN);
    simpleTokenTest(SimpleParamTypes.VERIFIER_TOKEN, TokenTypes.VERIFIER_TOKEN);
  }

  private void simpleTokenTest(SimpleParamTypes tokenTable, TokenTypes psTable) {
    String prefix = UUID.randomUUID().toString();
    byte[] data = ("HelloWorld-" + prefix).getBytes();
    byte[] psValue = ("PsValue-" + prefix).getBytes();
    URI key = URI.create("test-" + prefix);
    ps.clearTableForTest(tokenTable);
    ps.clearTableForTest(psTable);
    // Foreign key constraint
    assertFalse(ps.associatePseudonym(psTable, key, psValue));
    assertTrue(ps.insertItem(tokenTable, key, data));
    assertFalse(ps.isPseudonymInToken(psTable, psValue));
    assertTrue(ps.associatePseudonym(psTable, key, psValue));
    assertTrue(ps.isPseudonymInToken(psTable, psValue));
    assertTrue(ps.deleteItem(tokenTable, key));
    // Deletion should cascade to pseudonym
    assertFalse(ps.isPseudonymInToken(psTable, psValue));
  }

  @Test
  public void zeroLengthData() {
    SimpleParamTypes table = SimpleParamTypes.TEST_TABLE;
    String prefix = UUID.randomUUID().toString();
    byte[] data = new byte[0];
    URI key = URI.create("test-" + prefix);

    ps.clearTestTable();
    assertTrue(ps.insertItem(table, key, data));
    byte[] ret = ps.getItem(table, key);
    assertTrue(Arrays.equals(data, ret));
    assertTrue(ps.updateItem(table, key, data));
    ret = ps.getItem(table, key);
    assertTrue(Arrays.equals(data, ret));
    List<URI> l = ps.listItems(table);
    assertTrue(l.contains(key));
    assertTrue(ps.deleteItem(table, key));
  }

  @Test
  public void largeBinaryData() {
    SimpleParamTypes table = SimpleParamTypes.TEST_TABLE;
    String prefix = UUID.randomUUID().toString();
    byte[] data = new byte[1024 * 512];
    Random r = new Random();
    r.nextBytes(data);
    URI key = URI.create("test-" + prefix);

    ps.clearTestTable();
    assertTrue(ps.insertItem(table, key, data));
    byte[] ret = ps.getItem(table, key);
    assertTrue(Arrays.equals(data, ret));
    r.nextBytes(data);
    assertTrue(ps.updateItem(table, key, data));
    ret = ps.getItem(table, key);
    assertTrue(Arrays.equals(data, ret));
    List<URI> l = ps.listItems(table);
    assertTrue(l.contains(key));
    assertTrue(ps.deleteItem(table, key));
  }

  @Test
  public void utf8Key() {
    SimpleParamTypes table = SimpleParamTypes.TEST_TABLE;
    String prefix = UUID.randomUUID().toString();
    byte[] data = ("HelloWorld-" + prefix).getBytes();
    // Doesn't work for Japanese (3 byte per character) UTF8 strings
    // URI key =
    // URI.create("test-"+prefix+"\u53e4\u6c60\u3084\u86d9\u98db\u3073\u8fbc\u3080\u6c34\u306e\u97f3");
    URI key =
        URI.create("test-" + prefix + "I\u00f1t\u00ebrn\u00e2ti\u00f4n\u00e0liz\u00e6ti\u00f8n");

    ps.clearTestTable();
    assertTrue(ps.insertItem(table, key, data));
    byte[] ret = ps.getItem(table, key);
    assertTrue(Arrays.equals(data, ret));
    byte[] newdata = ("HowAreYou-" + prefix).getBytes();
    assertTrue(ps.updateItem(table, key, newdata));
    ret = ps.getItem(table, key);
    assertTrue(Arrays.equals(newdata, ret));
    List<URI> l = ps.listItems(table);
    assertTrue(l.contains(key));
    assertTrue(ps.deleteItem(table, key));
  }

  @Test
  public void testDuplicateKey() {
    SimpleParamTypes table = SimpleParamTypes.TEST_TABLE;
    String prefix = UUID.randomUUID().toString();
    byte[] data = ("HelloWorld-" + prefix).getBytes();
    URI key = URI.create("test-" + prefix);

    ps.clearTestTable();
    assertTrue(ps.insertItem(table, key, data));
    byte[] ret = ps.getItem(table, key);
    assertTrue(Arrays.equals(data, ret));
    assertFalse(ps.insertItem(table, key, data));
    assertTrue(ps.deleteItem(table, key));
  }

  @Test
  public void testDeleteWithoutItem() {
    SimpleParamTypes table = SimpleParamTypes.TEST_TABLE;
    String prefix = UUID.randomUUID().toString();
    URI key = URI.create("test-" + prefix);

    ps.clearTestTable();
    assertFalse(ps.deleteItem(table, key));
  }

  @Test
  public void testGetWithoutItem() {
    SimpleParamTypes table = SimpleParamTypes.TEST_TABLE;
    String prefix = UUID.randomUUID().toString();
    URI key = URI.create("test-" + prefix);

    ps.clearTestTable();
    assertNull(ps.getItem(table, key));
  }

  @Test
  public void testUpdateWithoutItem() {
    SimpleParamTypes table = SimpleParamTypes.TEST_TABLE;
    String prefix = UUID.randomUUID().toString();
    byte[] data = ("HelloWorld-" + prefix).getBytes();
    URI key = URI.create("test-" + prefix);

    ps.clearTestTable();
    assertFalse(ps.updateItem(table, key, data));
    assertNull(ps.getItem(table, key));
    assertTrue(ps.insertItem(table, key, data));
    byte[] ret = ps.getItem(table, key);
    assertTrue(Arrays.equals(data, ret));
    assertTrue(ps.deleteItem(table, key));
  }

  @Test
  public void testEmptyList() {
    SimpleParamTypes table = SimpleParamTypes.TEST_TABLE;

    ps.clearTestTable();
    List<URI> ret = ps.listItems(table);
    assertEquals(0, ret.size());
  }

  @Test
  public void simpleTestCredential() {
    String prefix = UUID.randomUUID().toString();
    byte[] data = ("HelloWorld-" + prefix).getBytes();
    URI key = URI.create("test-" + prefix);
    URI issuer = URI.create("theissuer");
    URI credspec = URI.create("thecredspec");
    String user = "Alice-" + prefix;
    String otherUser = "Bob-" + prefix;

    ps.clearTableForTest(JdbcPersistentStorage.CREDENTIAL_TABLE);
    assertTrue(ps.insertCredential(key, user, issuer, credspec, data));
    assertNull(ps.getCredential(key, otherUser));
    byte[] ret = ps.getCredential(key, user);
    assertTrue(Arrays.equals(data, ret));
    byte[] newdata = ("HowAreYou-" + prefix).getBytes();
    assertFalse(ps.updateCredential(key, otherUser, newdata));
    ret = ps.getCredential(key, user);
    assertTrue(Arrays.equals(data, ret));
    assertTrue(ps.updateCredential(key, user, newdata));
    ret = ps.getCredential(key, user);
    assertTrue(Arrays.equals(newdata, ret));
    assertFalse(ps.deleteCredential(key, otherUser));
    List<URI> l = ps.listCredentials(user);
    assertTrue(l.contains(key));
    l = ps.listCredentials(otherUser);
    assertEquals(0, l.size());
    assertTrue(ps.deleteCredential(key, user));
  }

  @Test
  public void simpleTestSecret() {
    String prefix = UUID.randomUUID().toString();
    byte[] data = ("HelloWorld-" + prefix).getBytes();
    URI key = URI.create("test-" + prefix);
    String user = "Alice-" + prefix;
    String otherUser = "Bob-" + prefix;

    ps.clearTableForTest(JdbcPersistentStorage.SECRET_TABLE);
    assertTrue(ps.insertSecret(key, user, data));
    assertNull(ps.getSecret(key, otherUser));
    byte[] ret = ps.getSecret(key, user);
    assertTrue(Arrays.equals(data, ret));
    byte[] newdata = ("HowAreYou-" + prefix).getBytes();
    assertFalse(ps.updateSecret(key, otherUser, newdata));
    ret = ps.getSecret(key, user);
    assertTrue(Arrays.equals(data, ret));
    assertTrue(ps.updateSecret(key, user, newdata));
    ret = ps.getSecret(key, user);
    assertTrue(Arrays.equals(newdata, ret));
    assertFalse(ps.deleteSecret(key, otherUser));
    List<URI> l = ps.listSecrets(user);
    assertTrue(l.contains(key));
    l = ps.listSecrets(otherUser);
    assertEquals(0, l.size());
    assertTrue(ps.deleteSecret(key, user));
  }

  @Test
  public void simpleTestPseudonym() {
    String prefix = UUID.randomUUID().toString();
    byte[] data = ("HelloWorld-" + prefix).getBytes();
    URI key = URI.create("test-" + prefix);
    String user = "Alice-" + prefix;
    String otherUser = "Bob-" + prefix;
    byte[] psValue = ("HelloWorld-" + prefix).getBytes();
    String scope = "test-scope-" + prefix;
    boolean exclusive = false;

    ps.clearTableForTest(JdbcPersistentStorage.PSEUDONYM_TABLE);
    assertTrue(ps.insertPseudonym(key, user, scope, exclusive, psValue, data));
    assertNull(ps.getPseudonym(key, otherUser));
    byte[] ret = ps.getPseudonym(key, user);
    assertTrue(Arrays.equals(data, ret));
    byte[] newdata = ("HowAreYou-" + prefix).getBytes();
    assertFalse(ps.updatePseudonym(key, otherUser, newdata));
    ret = ps.getPseudonym(key, user);
    assertTrue(Arrays.equals(data, ret));
    assertTrue(ps.updatePseudonym(key, user, newdata));
    ret = ps.getPseudonym(key, user);
    assertTrue(Arrays.equals(newdata, ret));
    assertFalse(ps.deletePseudonym(key, otherUser));
    List<URI> l = ps.listPseudonyms(user);
    assertTrue(l.contains(key));
    l = ps.listPseudonyms(otherUser);
    assertEquals(0, l.size());
    assertTrue(ps.deletePseudonym(key, user));
  }

  @Test
  public void simpleTestRevInfo() {
    String prefix = UUID.randomUUID().toString();
    byte[] data1 = ("HelloWorld-1-" + prefix).getBytes();
    byte[] data2 = ("HelloWorld-2-" + prefix).getBytes();
    URI key = URI.create("test-" + prefix);
    URI revAuth1 = URI.create("test-ra1-" + prefix);
    URI revAuth2 = URI.create("test-ra2-" + prefix);
    Calendar cal = Calendar.getInstance();

    ps.clearTableForTest(JdbcPersistentStorage.REV_INFO_TABLE);
    assertTrue(ps.insertRevocationInformation(key, revAuth1, cal, data1));
    byte[] ret = ps.getRevocationInformation(key, revAuth1);
    assertTrue(Arrays.equals(data1, ret));
    assertTrue(ps.insertRevocationInformation(key, revAuth2, cal, data2));
    ret = ps.getRevocationInformation(key, revAuth2);
    assertTrue(Arrays.equals(data2, ret));
    ret = ps.getRevocationInformation(key, revAuth1);
    assertTrue(Arrays.equals(data1, ret));
  }

  @Test
  public void testCredentialFilter() {

    class MiniCred {
      final byte[] data;
      final URI key;
      final URI issuer;
      final URI credspec;
      final String user;

      MiniCred(String prefix, URI issuer, URI credspec, String user, int i) {
        this.key = URI.create("test-" + i + prefix);
        this.issuer = issuer;
        this.credspec = credspec;
        this.user = user;
        this.data = ("HelloWorld-" + i + prefix).getBytes();
      }

      void insert() {
        assertTrue(ps.insertCredential(key, user, issuer, credspec, data));
      }

      void delete() {
        assertTrue(ps.deleteCredential(key, user));
      }
    }

    ps.clearTableForTest(JdbcPersistentStorage.CREDENTIAL_TABLE);

    String prefix = UUID.randomUUID().toString();
    List<URI> issuers = Arrays.asList(URI.create("I0"), URI.create("I1"), URI.create("I2"));
    List<URI> credSpecs = Arrays.asList(URI.create("CS0"), URI.create("CS1"), URI.create("CS2"));
    List<MiniCred> creds = new ArrayList<MiniCred>();
    String user = "Alice-" + prefix;
    String otherUser = "Bob-" + prefix;
    for (int i = 0; i < 9; ++i) {
      MiniCred c = new MiniCred(prefix, issuers.get(i % 3), credSpecs.get(i / 3), user, i);
      creds.add(c);
      c.insert();
    }
    for (int i = 0; i < 9; ++i) {
      MiniCred c = new MiniCred(prefix, issuers.get(i % 3), credSpecs.get(i / 3), otherUser, i);
      creds.add(c);
      c.insert();
    }

    List<URI> filterIssuers;
    List<URI> filterCs;
    List<URI> res;

    // Filter 0: zero issuers, zero cs
    filterIssuers = Collections.emptyList();
    filterCs = Collections.emptyList();
    res = ps.listCredentials(user, filterIssuers, filterCs);
    assertEquals(0, res.size());

    // Filter 1: one issuer, one cs
    filterIssuers = Collections.singletonList(issuers.get(1));
    filterCs = Collections.singletonList(credSpecs.get(1));
    res = ps.listCredentials(user, filterIssuers, filterCs);
    assertEquals(1, res.size());
    assertEquals(creds.get(4).key, res.get(0));

    // Filter 2: two issuers, two cs
    filterIssuers = Arrays.asList(issuers.get(0), issuers.get(2));
    filterCs = Arrays.asList(credSpecs.get(1), credSpecs.get(2));
    res = ps.listCredentials(user, filterIssuers, filterCs);
    assertEquals(4, res.size());
    assertTrue(res.contains(creds.get(3).key));
    assertTrue(res.contains(creds.get(5).key));
    assertTrue(res.contains(creds.get(6).key));
    assertTrue(res.contains(creds.get(8).key));

    // Filter 3: all issuers, all cs
    filterIssuers = issuers;
    filterCs = credSpecs;
    res = ps.listCredentials(user, filterIssuers, filterCs);
    assertEquals(9, res.size());
    for (int i = 0; i < 9; ++i) {
      assertTrue(res.contains(creds.get(i).key));
    }

    // Filter 4: multiple copies of one issuer, one cs
    filterIssuers = Arrays.asList(issuers.get(1), issuers.get(1));
    filterCs = Arrays.asList(credSpecs.get(1), credSpecs.get(1));
    res = ps.listCredentials(user, filterIssuers, filterCs);
    assertEquals(1, res.size());
    assertEquals(creds.get(4).key, res.get(0));


    for (MiniCred c : creds) {
      c.delete();
    }
  }

  @Test
  public void pseudonymSearch() {

    class MiniNym {
      final byte[] data;
      final URI key;
      final byte[] value;
      final String scope;
      final String user;
      final boolean exclusive;

      MiniNym(String prefix, String scope, String user, boolean exclusive, int i) {
        this.key = URI.create("test-" + i + prefix);
        this.scope = scope;
        this.exclusive = exclusive;
        this.user = user;
        this.data = ("HelloWorld-" + i + prefix).getBytes();
        this.value = ("value-" + i + prefix).getBytes();
      }

      void insert() {
        assertTrue(ps.insertPseudonym(key, user, scope, exclusive, value, data));
      }

      void delete() {
        assertTrue(ps.deletePseudonym(key, user));
      }
    }

    ps.clearTableForTest(JdbcPersistentStorage.PSEUDONYM_TABLE);

    String prefix = UUID.randomUUID().toString();
    List<String> users = Arrays.asList("Alice-" + prefix, "Bob" + prefix);
    List<String> scopes = Arrays.asList("S1", "S2", "S3");
    List<MiniNym> nyms = new ArrayList<>();
    for (int i = 0; i < 12; ++i) {
      MiniNym nym = new MiniNym(prefix, scopes.get(i % 3), users.get(i / 6), (i / 3) % 2 == 0, i);
      nym.insert();
      nyms.add(nym);
    }
    List<URI> res;

    res = ps.listPseudonyms(users.get(1));
    assertEquals(6, res.size());
    for (int i = 6; i < 12; ++i) {
      assertTrue(res.contains(nyms.get(i).key));
    }

    res = ps.listPseudonyms(users.get(0), nyms.get(3).value);
    assertEquals(1, res.size());
    assertEquals(res.get(0), nyms.get(3).key);

    res = ps.listPseudonyms(users.get(1), nyms.get(3).value);
    assertEquals(0, res.size());

    res = ps.listPseudonyms(users.get(0), scopes.get(2));
    assertEquals(2, res.size());
    assertTrue(res.contains(nyms.get(2).key));
    assertTrue(res.contains(nyms.get(5).key));

    res = ps.listPseudonyms(users.get(0), scopes.get(2), true);
    assertEquals(1, res.size());
    assertEquals(res.get(0), nyms.get(2).key);

    res = ps.listPseudonyms(users.get(0), scopes.get(2), false);
    assertEquals(1, res.size());
    assertEquals(res.get(0), nyms.get(5).key);

    for (MiniNym nym : nyms) {
      nym.delete();
    }
  }

  @Test
  public void testLatestRevocationInformation() {
    String prefix = UUID.randomUUID().toString();
    byte[] data1 = ("HelloWorld-1-" + prefix).getBytes();
    byte[] data2 = ("HelloWorld-2-" + prefix).getBytes();
    URI key1 = URI.create("test-1-" + prefix);
    URI key2 = URI.create("test-2-" + prefix);
    URI revAuth = URI.create("test-ra1-" + prefix);
    Calendar cal1 = Calendar.getInstance();
    Calendar cal2 = Calendar.getInstance();
    cal1.add(Calendar.DAY_OF_MONTH, 1);

    ps.clearTableForTest(JdbcPersistentStorage.REV_INFO_TABLE);
    assertTrue(ps.insertRevocationInformation(key1, revAuth, cal1, data1));
    assertTrue(ps.insertRevocationInformation(key2, revAuth, cal2, data2));
    byte[] ret = ps.getLatestRevocationInformation(revAuth);
    assertTrue(Arrays.equals(data1, ret));
    ps.clearTableForTest(JdbcPersistentStorage.REV_INFO_TABLE);
  }
}
