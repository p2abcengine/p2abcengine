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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

public class JdbcDatabase implements DatabaseConnectionFactory {

  public static final String JDBC_DRIVER = "jdbcDriver";
  public static final String JDBC_STRING = "jdbcConnectionType";
  public static final String JDBC_PROPERTIES = "jdbcProperties";
  public static final String DATABASE_URL = "databaseUrl";
  public static final String DATABASE_PORT = "databasePort";
  public static final String DATABASE_NAME = "databaseName";
  public static final String DATABASE_LOGIN = "databaseLogin";
  public static final String DATABASE_PASSWORD = "databasePassword";
  public static final String MIN_CONNECTION = "minimumConnectionCount";
  public static final String MAX_CONNECTION = "maximumConnectionCount";

  private final String jdbcDriver;
  private final String jdbcConnType;
  private final Properties jdbcProperties;
  private final String dbName;
  private final String dbPort;
  private final String connURL;
  private final BoneCP connectionPool;

  @Inject
  public JdbcDatabase(@Named(JDBC_DRIVER) String jdbcDriver,
      @Named(JDBC_STRING) String jdbcConnType, @Named(JDBC_PROPERTIES) Properties jdbcProperties,
      @Named(DATABASE_NAME) String dbName, @Named(DATABASE_PORT) String dbPort,
      @Named(DATABASE_URL) String url, @Named(MIN_CONNECTION) int minConnection, @Named(MAX_CONNECTION) int maxConnection) {
    this.jdbcDriver = jdbcDriver;
    this.jdbcConnType = jdbcConnType;
    this.jdbcProperties = jdbcProperties;
    this.dbName = dbName;
    this.dbPort = dbPort;
    this.connURL = "jdbc:" + this.jdbcConnType + "://" + url + this.dbPort + "/" + this.dbName;
    try {
      Class.forName(this.jdbcDriver);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    try {
      final BoneCPConfig poolConfig = new BoneCPConfig();
      poolConfig.setJdbcUrl(this.connURL);
      poolConfig.setProperties(this.jdbcProperties);
      poolConfig.setMinConnectionsPerPartition(minConnection);
      poolConfig.setMaxConnectionsPerPartition(maxConnection);
      poolConfig.setPartitionCount(1);
      connectionPool = new BoneCP(poolConfig);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public Connection getConnection() throws SQLException {
    return connectionPool.getConnection();
  }

  @Override
  public void shutdown() {
    connectionPool.shutdown();
  }
}
