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
import java.util.Calendar;
import java.util.List;

public interface PersistentStorage {

  /**
   * Shuts the all the connections and/or connection pool which was used to create connections. The
   * caller must invoke this method before the application finishes.
   */
  void shutdown();

  /**
   * Inserts a key/value pair into the given table of the database. Returns true if insertion
   * suceeded.
   * 
   * @param table
   * @param key
   * @param value
   * @return
   */
  boolean insertItem(SimpleParamTypes table, URI key, byte[] value);

  /**
   * Inserts a key/value pair into the given table of the database, or replace the value if the key
   * already exists.
   * 
   * This method should return:
   * 1, if it is replacing nothing (i.e. there was no item beforehand).
   * -1, if it tried to replace nothing and did not succeed (i.e. there was not item beforehand and
   *   insertion failed without an exception been thrown)
   * 2, if it is replacing something
   * -2, if it tried to replace something and did not succeed with insertion, but succeeded with deletion  
   * 
   * 
   * @param table
   * @param key
   * @param value
   * @return 
   */
  int replaceItem(SimpleParamTypes revAuthSecretKey, URI historyUID, byte[] writeAsBytes);

  /**
   * Returns a value given a key from the given table of the database, or return null.
   * 
   * @param table
   * @param key
   * @return
   */
  byte[] getItem(SimpleParamTypes table, URI key);

  /**
   * Returns a value given a key from the given table of the database, or return null. The item is
   * then deleted.
   * 
   * @param table
   * @param key
   * @return
   */
  byte[] getItemAndDelete(SimpleParamTypes table, URI key);

  /**
   * Deletes a given key/value pair from the given table of the database. Return true if the given
   * key existed.
   * 
   * @param table
   * @param key
   * @return
   */
  boolean deleteItem(SimpleParamTypes table, URI key);

  /**
   * Updates the given key/value pair in the given table in the database. If the key does not exist,
   * do not insert anything and return false.
   * 
   * @param table
   * @param key
   * @param value
   */
  boolean updateItem(SimpleParamTypes table, URI key, byte[] value);

  /**
   * Returns a list of keys in the given table in the database.
   * 
   * @param table
   * @return
   */
  List<URI> listItems(SimpleParamTypes table);

  /**
   * Associates a given pseudonym value with an issuance or verification token id.
   * 
   * @param table
   * @param tokenId
   * @param pseudonymValue
   * @return
   */
  boolean associatePseudonym(TokenTypes table, URI tokenId, byte[] pseudonymValue);

  /**
   * Returns true if the given pseudonym value was associated with the given issuance or
   * verification token id.
   * 
   * @param table
   * @param pseudonymValue
   * @return
   */
  boolean isPseudonymInToken(TokenTypes table, byte[] pseudonymValue);


  /**
   * Inserts a key/value pair into the given table of the database. Returns true if the insertion
   * succeeded.
   * 
   * @param table
   * @param key
   * @param value
   */
  boolean insertCredential(URI key, String username, URI issuer, URI credSpec, byte[] value);

  /**
   * Returns a value given a key from the given table of the database, or return null.
   * 
   * @param table
   * @param key
   * @return
   */
  byte[] getCredential(URI key, String username);

  /**
   * Deletes a given key/value pair from the given table of the database. Return true if the given
   * key existed.
   * 
   * @param table
   * @param key
   * @return
   */
  boolean deleteCredential(URI key, String username);

  /**
   * Updates the given key/value pair in the given table in the database. If the key does not exist,
   * do not insert anything and return false.
   * 
   * @param table
   * @param key
   * @param value
   */
  boolean updateCredential(URI key, String username, byte[] value);

  /**
   * Returns a list of keys in the given table in the database.
   * 
   * @param table
   * @return
   */
  List<URI> listCredentials(String username);

  /**
   * Returns a list of keys in the given table in the database.
   * 
   * @param table
   * @return
   */
  List<URI> listCredentials(String username, List<URI> issuer, List<URI> credSpec);

  /**
   * Inserts a key/value pair into the given table of the database. Returns true if the insertion
   * succeeded.
   * 
   * @param table
   * @param key
   * @param value
   */
  boolean insertSecret(URI key, String username, byte[] value);

  /**
   * Returns a value given a key from the given table of the database, or return null.
   * 
   * @param table
   * @param key
   * @return
   */
  byte[] getSecret(URI key, String username);

  /**
   * Deletes a given key/value pair from the given table of the database. Return true if the given
   * key existed.
   * 
   * @param table
   * @param key
   * @return
   */
  boolean deleteSecret(URI key, String username);

  /**
   * Updates the given key/value pair in the given table in the database. If the key does not exist,
   * do not insert anything and return false.
   * 
   * @param table
   * @param key
   * @param value
   */
  boolean updateSecret(URI key, String username, byte[] value);

  /**
   * Returns a list of keys in the given table in the database.
   * 
   * @param table
   * @return
   */
  List<URI> listSecrets(String username);

  /**
   * Inserts a key/value pair into the given table of the database. Returns true if the insertion
   * succeeded.
   * 
   * @param table
   * @param key
   * @param value
   */
  boolean insertPseudonym(URI key, String username, String scope, boolean isExclusive,
      byte[] pseudonymValue, byte[] value);

  /**
   * Returns a value given a key from the given table of the database, or return null.
   * 
   * @param table
   * @param key
   * @return
   */
  byte[] getPseudonym(URI key, String username);

  /**
   * Deletes a given key/value pair from the given table of the database. Return true if the given
   * key existed.
   * 
   * @param table
   * @param key
   * @return
   */
  boolean deletePseudonym(URI key, String username);

  /**
   * Updates the given key/value pair in the given table in the database. If the key does not exist,
   * do not insert anything and return false.
   * 
   * @param table
   * @param key
   * @param value
   */
  boolean updatePseudonym(URI key, String username, byte[] value);

  /**
   * Returns a list of keys in the given table in the database.
   * 
   * @param table
   * @return
   */
  List<URI> listPseudonyms(String username);

  /**
   * Returns a list of keys in the given table in the database.
   * 
   * @param table
   * @return
   */
  List<URI> listPseudonyms(String username, String scope);

  /**
   * Returns a list of keys in the given table in the database.
   * 
   * @param table
   * @return
   */
  List<URI> listPseudonyms(String username, String scope, boolean isExclusive);

  /**
   * Returns a list of keys in the given table in the database.
   * 
   * @param table
   * @return
   */
  List<URI> listPseudonyms(String username, byte[] pseudonymValue);

  /**
   * Inserts a key/value pair into the given table of the database. Returns true if the insertion
   * succeeded.
   * 
   * @param table
   * @param key
   * @param value
   */
  boolean insertRevocationInformation(URI key, URI rev_auth, Calendar created, byte[] value);

  /**
   * Returns a value given a key from the given table of the database, or return null.
   * 
   * @param table
   * @param key
   * @return
   */
  byte[] getRevocationInformation(URI key, URI rev_auth);

  /**
   * Returns a value given a key from the given table of the database, or return null.
   * 
   * @param table
   * @param key
   * @return
   */
  byte[] getLatestRevocationInformation(URI rev_auth);
}
