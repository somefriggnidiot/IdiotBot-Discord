package com.somefriggnidiot.discord.data_access;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Handles connections to the embedded database.
 */
public class DatabaseConnector {

   private static final String DATABASE_USER_TABLE = "./db/database_users.odb";
   private static final String USER_WARNING_TABLE = "./db/user_warnings.odb";
   private static final String GUILD_INFO = "./db/guild_info.odb";
   private EntityManager manager;

   /**
    * Generates an {@link EntityManager} for the given {@link Table}
    *
    * @param table the {@code Table} with which a connection should be established.
    * @return an {@code EntityManager} that manages transactions to the provided {@code Table}.
    */
   public EntityManager getEntityManager(Table table) {
      EntityManagerFactory managerFactory;
      if (table == Table.DATABASE_USER) {
         managerFactory = Persistence.createEntityManagerFactory(DATABASE_USER_TABLE);
         this.manager = managerFactory.createEntityManager();
      } else if (table == Table.USER_WARNING) {
         managerFactory = Persistence.createEntityManagerFactory(USER_WARNING_TABLE);
         this.manager = managerFactory.createEntityManager();
      } else if (table == Table.GUILD_INFO) {
         managerFactory = Persistence.createEntityManagerFactory(GUILD_INFO);
         this.manager = managerFactory.createEntityManager();
      }

      return manager;
   }

   /**
    * Existing database tables in use by this program.
    */
   public enum Table {
      DATABASE_USER, USER_WARNING, GUILD_INFO
   }
}
