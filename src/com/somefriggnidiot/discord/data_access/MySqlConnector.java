package com.somefriggnidiot.discord.data_access;

import com.somefriggnidiot.discord.data_access.models.BridgeObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySqlConnector {
   private final Logger logger = LoggerFactory.getLogger(this.getClass());
   final String mysqlUrl = "jdbc:mysql://192.168.86.57:3306/idiotbot_discord";
   final String username = "idiotbot";
   final String password;
   final Connection connection;

   public MySqlConnector(String password) {
      try {
         Class.forName("com.mysql.cj.jdbc.Driver");
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
      }

      this.password = password;
      try {
         connection = DriverManager.getConnection(mysqlUrl, username, password);
      } catch (SQLException e) {
         throw new IllegalStateException("Could not establish connection with database.", e);
      }
   }

   public void shutdown() {
      try {
         connection.close();
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }

//   public List<BridgeObject> getBridges(Integer discordServerId) {
//
//   }
}
