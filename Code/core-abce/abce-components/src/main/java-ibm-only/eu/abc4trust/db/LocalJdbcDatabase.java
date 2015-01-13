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
import java.sql.SQLException;
import java.util.Properties;

import com.google.inject.Inject;

public class LocalJdbcDatabase implements DatabaseConnectionFactory {

  private final static String jdbcDriver_mysql = "org.drizzle.jdbc.DrizzleDriver";
  private final static String jdbcConnType_mysql = "mysql:thin";
  private final static Properties jdbcProperties_mysql = new Properties();
  private final static String dbPort_mysql = "";
  private final static String dbName_mysql = "idmxinternal";
  private final static String url_mysql = "localhost";
  private final static String jdbcDriver_postgresql = "org.postgresql.Driver";
  private final static String jdbcConnType_postgresql = "postgresql";
  private final static Properties jdbcProperties_postgresql = new Properties();
  private final static String dbPort_postgresql = ":5435";
  private final static String dbName_postgresql = "idmxinternal";
  private final static String url_postgresql = "localhost";
  private final static String jdbcDriver_db2 = "com.ibm.db2.jcc.DB2Driver";
  private final static String jdbcConnType_db2 = "db2";
  private final static Properties jdbcProperties_db2 = new Properties();
  private final static String dbPort_db2 = ":60666";
  private final static String dbName_db2 = "abce";
  private final static String url_db2 = "9.4.195.77";

  private final JdbcDatabase db;

  @Inject
  public LocalJdbcDatabase() {
    jdbcProperties_mysql.put("user", "idmxinternal");
    jdbcProperties_mysql.put("password", "idmxinternal");
    jdbcProperties_mysql.put("retreiveMessagesFromServerOnGetMessage", "true");

    jdbcProperties_postgresql.put("retreiveMessagesFromServerOnGetMessage", "true");

    jdbcProperties_db2.put("user", "db2user");
    jdbcProperties_db2.put("password", "Zoowa2sh");
    jdbcProperties_db2.put("retreiveMessagesFromServerOnGetMessage", "true");


    this.db =
        new JdbcDatabase(jdbcDriver_mysql, jdbcConnType_mysql, jdbcProperties_mysql, dbName_mysql,
            dbPort_mysql, url_mysql, 1, 5);
//    this.db =
//        new JdbcDatabase(jdbcDriver_postgresql, jdbcConnType_postgresql, jdbcProperties_postgresql,
//            dbName_postgresql, dbPort_postgresql, url_postgresql, 1, 5);
    // this.db =
    // new JdbcDatabase(jdbcDriver_db2, jdbcConnType_db2, jdbcProperties_db2, dbName_db2,
    // dbPort_db2, url_db2, 1, 5);
  }

  @Override
  public Connection getConnection() throws SQLException {
    return db.getConnection();
  }

  @Override
  public void shutdown() {
    db.shutdown();
  }
}
