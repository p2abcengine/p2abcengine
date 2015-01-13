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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.inject.Inject;

public class JdbcPersistentStorage implements PersistentStorage {

  DatabaseConnectionFactory dbConnFactory;

  @Inject
  public JdbcPersistentStorage(DatabaseConnectionFactory dbConnFactory) {
    this.dbConnFactory = dbConnFactory;
  }
  
  @Override
  public void shutdown() {
    this.dbConnFactory.shutdown();
  }

  @Override
  public boolean insertItem(SimpleParamTypes table, URI key, byte[] value) {
    String check = "SELECT id FROM " + tableName(table) + " WHERE id = ?";
    String command = "INSERT INTO  " + tableName(table) + " (id,data) VALUES (?, ?)";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(check);) {
        statement.setString(1, key.toString());
        final ResultSet rs = statement.executeQuery();
        if (rs.next()) {
          return false;
        }
      }
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, key.toString());
        statement.setBytes(2, value);
        int nrows = statement.executeUpdate();
        return nrows == 1;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int replaceItem(SimpleParamTypes table, URI key, byte[] value) {
    String delete = "DELETE FROM " + tableName(table) + " WHERE id = ?";
    String command = "INSERT INTO  " + tableName(table) + " (id,data) VALUES (?, ?)";
    final boolean isDelete;
    final boolean isInsert;
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(delete);) {
        statement.setString(1, key.toString());
        isDelete = statement.executeUpdate() == 1;
      }
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, key.toString());
        statement.setBytes(2, value);
        isInsert = statement.executeUpdate() == 1;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    if (isDelete) {
    	if (isInsert) {
    		return 2;
    	}
    	return -2;
    } else {
    	if (isInsert) {
    		return 1;
    	}
        return -1;    	
    }
  }

  @Override
  public byte[] getItem(SimpleParamTypes table, URI key) {
    String command = "SELECT data FROM  " + tableName(table) + "  WHERE id = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, key.toString());
        try (ResultSet rs = statement.executeQuery();) {
          if (rs.next()) {
            return rs.getBytes(1);
          } else {
            return null;
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public byte[] getItemAndDelete(SimpleParamTypes table, URI key) {
    byte[] ret;
    String command1 = "SELECT data FROM  " + tableName(table) + "  WHERE id = ? FOR UPDATE";
    String command2 = "DELETE FROM  " + tableName(table) + "  WHERE id = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      connect.setAutoCommit(false);
      try (PreparedStatement statement = connect.prepareStatement(command1);) {
        statement.setString(1, key.toString());
        try (ResultSet rs = statement.executeQuery();) {
          if (rs.next()) {
            ret = rs.getBytes(1);
          } else {
            ret = null;
          }
        }
      }
      try (PreparedStatement statement = connect.prepareStatement(command2);) {
        statement.setString(1, key.toString());
        int nrows = statement.executeUpdate();
        if (nrows != 1) {
          return null;
        }
      }
      connect.commit();
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean deleteItem(SimpleParamTypes table, URI key) {
    String command = "DELETE FROM  " + tableName(table) + "  WHERE id = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, key.toString());
        int nrows = statement.executeUpdate();
        return nrows == 1;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean updateItem(SimpleParamTypes table, URI key, byte[] value) {
    String command = "UPDATE  " + tableName(table) + "  SET data = ? WHERE id = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setBytes(1, value);
        statement.setString(2, key.toString());
        int nrows = statement.executeUpdate();
        return nrows == 1;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<URI> listItems(SimpleParamTypes table) {
    String command = "SELECT id FROM  " + tableName(table) + " ";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        try (ResultSet rs = statement.executeQuery();) {
          List<URI> result = new ArrayList<URI>();
          while (rs.next()) {
            result.add(URI.create(rs.getString(1)));
          }
          return result;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean associatePseudonym(TokenTypes table, URI tokenId, byte[] pseudonymValue) {
    String check =
        "SELECT id FROM " + tableName(SimpleParamTypes.valueOf(table.toString())) + " WHERE id = ?";
    String command = "INSERT INTO  " + tableName(table) + " (pseudonym,tokenid) VALUES (?, ?)";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(check);) {
        statement.setString(1, tokenId.toString());
        final ResultSet rs = statement.executeQuery();
        if (!rs.next()) {
          return false;
        }
      }
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setBytes(1, pseudonymValue);
        statement.setString(2, tokenId.toString());
        int nrows = statement.executeUpdate();
        return nrows == 1;
      }
    } catch (SQLIntegrityConstraintViolationException e) {
      System.err.println(e.getMessage());
      return false;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isPseudonymInToken(TokenTypes table, byte[] pseudonymValue) {
    String command = "SELECT COUNT(*) FROM  " + tableName(table) + "  WHERE pseudonym = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setBytes(1, pseudonymValue);
        try (ResultSet rs = statement.executeQuery();) {
          rs.next();
          int nrows = rs.getInt(1);
          return nrows == 1;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean insertCredential(URI key, String username, URI issuer, URI credSpec, byte[] value) {
    String check =
        "SELECT id, username FROM " + CREDENTIAL_TABLE + " WHERE id = ? AND username = ?";
    String command =
        "INSERT INTO  " + CREDENTIAL_TABLE
            + " (id,data,username,issuer,credspec) VALUES (?, ?, ?, ?, ?)";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(check);) {
        statement.setString(1, key.toString());
        statement.setString(2, username);
        final ResultSet rs = statement.executeQuery();
        if (rs.next()) {
          return false;
        }
      }
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, key.toString());
        statement.setBytes(2, value);
        statement.setString(3, username);
        statement.setString(4, issuer.toString());
        statement.setString(5, credSpec.toString());
        int nrows = statement.executeUpdate();
        return nrows == 1;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public byte[] getCredential(URI key, String username) {
    String command = "SELECT data FROM  " + CREDENTIAL_TABLE + "  WHERE id = ? AND username = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, key.toString());
        statement.setString(2, username);
        try (ResultSet rs = statement.executeQuery();) {
          if (rs.next()) {
            return rs.getBytes(1);
          } else {
            return null;
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean deleteCredential(URI key, String username) {
    String command = "DELETE FROM  " + CREDENTIAL_TABLE + "  WHERE id = ? AND username = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, key.toString());
        statement.setString(2, username);
        int nrows = statement.executeUpdate();
        return nrows == 1;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean updateCredential(URI key, String username, byte[] value) {
    String command = "UPDATE  " + CREDENTIAL_TABLE + "  SET data = ? WHERE id = ? AND username = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setBytes(1, value);
        statement.setString(2, key.toString());
        statement.setString(3, username);
        int nrows = statement.executeUpdate();
        return nrows == 1;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<URI> listCredentials(String username) {
    String command = "SELECT id FROM  " + CREDENTIAL_TABLE + "  where username = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, username);
        try (ResultSet rs = statement.executeQuery();) {
          List<URI> result = new ArrayList<URI>();
          while (rs.next()) {
            result.add(URI.create(rs.getString(1)));
          }
          return result;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public List<URI> listCredentials(String username, List<URI> issuers, List<URI> credSpecs) {
    StringBuilder selectStmt = new StringBuilder();
    selectStmt.append("SELECT id FROM  " + CREDENTIAL_TABLE + " ");
    // constrain username
    selectStmt.append("WHERE username = ? ");
    // constrain issuer
    if (issuers != null && issuers.size() > 0) {
      selectStmt.append("AND issuer IN (");
      for (int i = 0; i < issuers.size(); i++) {
        if (i == 0)
          selectStmt.append("?");
        else
          selectStmt.append(",?");
      }
      selectStmt.append(") ");
    }
    // constrain credspec
    if (credSpecs != null && credSpecs.size() > 0) {
      selectStmt.append("AND credspec IN (");
      for (int i = 0; i < credSpecs.size(); i++) {
        if (i == 0)
          selectStmt.append("?");
        else
          selectStmt.append(",?");
      }
      selectStmt.append(")");
    }

    try (Connection connect = dbConnFactory.getConnection()) {
      // Actually list the credentials
      try (PreparedStatement prepStmt = connect.prepareStatement(selectStmt.toString())) {
        int i = 1;
        prepStmt.setString(i++, username);
        for (URI issuer : issuers) {
          prepStmt.setString(i++, issuer.toString());
        }
        for (URI credspec : credSpecs) {
          prepStmt.setString(i++, credspec.toString());
        }

        try (ResultSet rs = prepStmt.executeQuery()) {
          List<URI> result = new ArrayList<URI>();
          while (rs.next()) {
            result.add(URI.create(rs.getString(1)));
          }
          return result;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean insertSecret(URI key, String username, byte[] value) {
    String check = "SELECT username, id FROM " + SECRET_TABLE + " WHERE username = ? AND id = ?";
    String command = "INSERT INTO  " + SECRET_TABLE + " (id,data,username) VALUES (?, ?, ?)";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(check);) {
        statement.setString(1, key.toString());
        statement.setString(2, username);
        final ResultSet rs = statement.executeQuery();
        if (rs.next()) {
          return false;
        }
      }
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, key.toString());
        statement.setBytes(2, value);
        statement.setString(3, username);
        int nrows = statement.executeUpdate();
        return nrows == 1;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public byte[] getSecret(URI key, String username) {
    String command = "SELECT data FROM  " + SECRET_TABLE + "  WHERE id = ? AND username = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, key.toString());
        statement.setString(2, username);
        try (ResultSet rs = statement.executeQuery();) {
          if (rs.next()) {
            return rs.getBytes(1);
          } else {
            return null;
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean deleteSecret(URI key, String username) {
    String command = "DELETE FROM  " + SECRET_TABLE + "  WHERE id = ? AND username = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, key.toString());
        statement.setString(2, username);
        int nrows = statement.executeUpdate();
        return nrows == 1;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean updateSecret(URI key, String username, byte[] value) {
    String command = "UPDATE  " + SECRET_TABLE + "  SET data = ? WHERE id = ? AND username = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setBytes(1, value);
        statement.setString(2, key.toString());
        statement.setString(3, username);
        int nrows = statement.executeUpdate();
        return nrows == 1;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<URI> listSecrets(String username) {
    String command = "SELECT id FROM  " + SECRET_TABLE + "  WHERE username = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, username);
        try (ResultSet rs = statement.executeQuery();) {
          List<URI> result = new ArrayList<URI>();
          while (rs.next()) {
            result.add(URI.create(rs.getString(1)));
          }
          return result;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean insertPseudonym(URI key, String username, String scope, boolean isExclusive,
      byte[] pseudonymValue, byte[] value) {
    String check = "SELECT id, username FROM " + PSEUDONYM_TABLE + " WHERE id = ? AND username = ?";
    String command =
        "INSERT INTO  " + PSEUDONYM_TABLE + " (id,data,username,scope,exclusive,pseudonym)"
            + "VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(check);) {
        statement.setString(1, key.toString());
        statement.setString(2, username);
        final ResultSet rs = statement.executeQuery();
        if (rs.next()) {
          return false;
        }
      }
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, key.toString());
        statement.setBytes(2, value);
        statement.setString(3, username);
        statement.setString(4, scope.toString());
        statement.setBoolean(5, isExclusive);
        statement.setBytes(6, pseudonymValue);
        int nrows = statement.executeUpdate();
        return nrows == 1;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public byte[] getPseudonym(URI key, String username) {
    String command = "SELECT data FROM  " + PSEUDONYM_TABLE + "  WHERE id = ? AND username = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, key.toString());
        statement.setString(2, username);
        try (ResultSet rs = statement.executeQuery();) {
          if (rs.next()) {
            return rs.getBytes(1);
          } else {
            return null;
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean deletePseudonym(URI key, String username) {
    String command = "DELETE FROM  " + PSEUDONYM_TABLE + "  WHERE id = ? AND username = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, key.toString());
        statement.setString(2, username);
        int nrows = statement.executeUpdate();
        return nrows == 1;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean updatePseudonym(URI key, String username, byte[] value) {
    String command = "UPDATE  " + PSEUDONYM_TABLE + "  SET data = ? WHERE id = ? AND username = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setBytes(1, value);
        statement.setString(2, key.toString());
        statement.setString(3, username);
        int nrows = statement.executeUpdate();
        return nrows == 1;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<URI> listPseudonyms(String username) {
    String command = "SELECT id FROM  " + PSEUDONYM_TABLE + "  WHERE username = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, username);
        try (ResultSet rs = statement.executeQuery();) {
          List<URI> result = new ArrayList<URI>();
          while (rs.next()) {
            result.add(URI.create(rs.getString(1)));
          }
          return result;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<URI> listPseudonyms(String username, String scope) {
    String command = "SELECT id FROM  " + PSEUDONYM_TABLE + "  WHERE username = ? AND scope = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, username);
        statement.setString(2, scope.toString());
        try (ResultSet rs = statement.executeQuery();) {
          List<URI> result = new ArrayList<URI>();
          while (rs.next()) {
            result.add(URI.create(rs.getString(1)));
          }
          return result;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<URI> listPseudonyms(String username, String scope, boolean isExclusive) {
    String command =
        "SELECT id FROM  " + PSEUDONYM_TABLE
            + "  WHERE username = ? AND scope = ? AND exclusive = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, username);
        statement.setString(2, scope.toString());
        statement.setBoolean(3, isExclusive);
        try (ResultSet rs = statement.executeQuery();) {
          List<URI> result = new ArrayList<URI>();
          while (rs.next()) {
            result.add(URI.create(rs.getString(1)));
          }
          return result;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<URI> listPseudonyms(String username, byte[] pseudonymValue) {
    String command =
        "SELECT id FROM  " + PSEUDONYM_TABLE + "  WHERE username = ? AND pseudonym = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, username);
        statement.setBytes(2, pseudonymValue);
        try (ResultSet rs = statement.executeQuery();) {
          List<URI> result = new ArrayList<URI>();
          while (rs.next()) {
            result.add(URI.create(rs.getString(1)));
          }
          return result;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean insertRevocationInformation(URI key, URI rev_auth, Calendar created, byte[] value) {
    String check = "SELECT id, rev_auth FROM " + REV_INFO_TABLE + " WHERE id = ? AND rev_auth = ?";
    String command =
        "INSERT INTO  " + REV_INFO_TABLE + " (id,data,rev_auth,created) VALUES (?, ?, ?, ?)";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(check);) {
        statement.setString(1, key.toString());
        statement.setString(2, rev_auth.toString());
        final ResultSet rs = statement.executeQuery();
        if (rs.next()) {
          return false;
        }
      }
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, key.toString());
        statement.setBytes(2, value);
        statement.setString(3, rev_auth.toString());
        statement.setTimestamp(4, new Timestamp(created.getTimeInMillis()));
        int nrows = statement.executeUpdate();
        return nrows == 1;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public byte[] getRevocationInformation(URI key, URI rev_auth) {
    String command = "SELECT data FROM  " + REV_INFO_TABLE + "  WHERE id = ? AND rev_auth = ?";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, key.toString());
        statement.setString(2, rev_auth.toString());
        try (ResultSet rs = statement.executeQuery();) {
          if (rs.next()) {
            return rs.getBytes(1);
          } else {
            return null;
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public byte[] getLatestRevocationInformation(URI rev_auth) {
    String command =
        "SELECT data FROM  " + REV_INFO_TABLE
            + "  WHERE rev_auth = ? ORDER BY created DESC LIMIT 1";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.setString(1, rev_auth.toString());
        try (ResultSet rs = statement.executeQuery();) {
          if (rs.next()) {
            return rs.getBytes(1);
          } else {
            return null;
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  // Visible for test
  void clearTableForTest(SimpleParamTypes table) {
    clearTableForTest(tableName(table), "id");
  }

  // Visible for test
  void clearTableForTest(TokenTypes table) {
    clearTableForTest(tableName(table), "tokenid");
  }

  // Visible for test
  void clearTableForTest(String table) {
    clearTableForTest(table, "id");
  }

  // Visible for test
  void clearTableForTest(String table, String column) {
    String command = "DELETE FROM  " + table + "  where  " + column + "  like 'test%'";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  // Visible for test
  void clearTestTable() {
    String command = "DELETE FROM  " + tableName(SimpleParamTypes.TEST_TABLE) + " ";
    try (Connection connect = dbConnFactory.getConnection()) {
      try (PreparedStatement statement = connect.prepareStatement(command);) {
        statement.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private String tableName(TokenTypes type) {
    switch (type) {
      case ISSUANCE_TOKEN:
        return "pseudonym_in_issuance_token";
      case VERIFIER_TOKEN:
        return "pseudonym_in_verifier_token";
    }
    throw new RuntimeException("Unknown value of TokenTypes");
  }

  private String tableName(SimpleParamTypes type) {
    switch (type) {
      case CRED_SPEC:
        return "cred_spec";
      case INSPECTOR_PUBLIC_KEY:
        return "inspector_public_key";
      case INSPECTOR_SECRET_KEY:
        return "inspector_secret_key";
      case ISSUANCE_LOG_ENTRY:
        return "issuance_log_entry";
      case ISSUANCE_TOKEN:
        return "issuance_token";
      case ISSUER_PARAMS:
        return "issuer_params";
      case ISSUER_SECRET_KEY:
        return "issuer_secret_key";
      case NON_REVOCATION_EVIDENCE:
        return "non_revocation_evidence";
      case REVOCATION_HISTORY:
        return "revocation_history";
      case REV_AUTH_LOG_ENTRY:
        return "rev_auth_log_entry";
      case REV_AUTH_PARAMS:
        return "rev_auth_params";
      case REV_AUTH_SECRET_KEY:
        return "rev_auth_secret_key";
      case SYSTEM_PARAMS:
        return "system_parameters";
      case VERIFIER_TOKEN:
        return "verifier_token";
      case TEST_TABLE:
        return "test_table";
      case STATE_ISSUER:
        return "state_storage_issuer";
      case STATE_RECIPIENT:
        return "state_storage_recipient";
    }
    throw new RuntimeException("Unknown value of TokenTypes");
  }

  // Visible for test
  static final String PSEUDONYM_TABLE = "pseudonym";
  // Visible for test
  static final String CREDENTIAL_TABLE = "credential";
  // Visible for test
  static final String SECRET_TABLE = "secret";
  // Visible for test
  static final String REV_INFO_TABLE = "revocation_information";

}
