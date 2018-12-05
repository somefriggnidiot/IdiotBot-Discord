package com.somefriggnidiot.discord.data_access.util;

import com.somefriggnidiot.discord.data_access.DatabaseConnector;
import com.somefriggnidiot.discord.data_access.DatabaseConnector.Table;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import java.sql.Timestamp;
import java.time.Instant;
import javax.persistence.EntityManager;

public class DatabaseUserUtil {

   private static EntityManager em = new DatabaseConnector().getEntityManager(Table.DATABASE_USER);
   private DatabaseUser dbu;

   public DatabaseUserUtil(Long userId) {
      this.dbu = getDatabaseObject(userId);
   }

   public static DatabaseUser getUser(Long userId) {
      return getDatabaseObject(userId);
   }

   /**
    * Adds the provided amount of tokens to the existing amount for the {@link DatabaseUser}
    * within the provided Guild.
    *
    * @param guildId the ID of the Guild in which this user is gaining tokens.
    * @param tokenGain the amount of tokens being gained.
    * @return the final quantity of tokens possessed by the user.
    */
   public Integer addTokens(Long guildId, Integer tokenGain) {
      Integer currentTokens =
          dbu.getTokenMap().get(guildId) == null ? 0 : dbu.getTokenMap().get(guildId);
      Integer newTokens = currentTokens + tokenGain;

      em.getTransaction().begin();
      dbu.updateTokens(guildId, newTokens);
      em.persist(dbu);
      em.getTransaction().commit();

      return newTokens;
   }

   public static Integer addXp(Long guildId, Long userId, Integer xpGain) {
      DatabaseUser dbu = getDatabaseObject(userId);
      Integer currentXp = dbu.getXpMap().get(guildId) == null ? 0 : dbu.getXpMap().get(guildId);
      Integer newXp = currentXp + xpGain;

      em.getTransaction().begin();
      dbu.updateXp(guildId, newXp);
      dbu.setLastMessageDtTm(Timestamp.from(Instant.now()));
      em.persist(dbu);
      em.getTransaction().commit();

      return newXp;
   }

   public static void setXp(Long guildId, Long userId, Integer xpValue) {
      DatabaseUser dbu = getDatabaseObject(userId);

      em.getTransaction().begin();
      dbu.updateXp(guildId, xpValue);
      dbu.setLastMessageDtTm(Timestamp.from(Instant.now()));
      em.persist(dbu);
      em.getTransaction().commit();
   }

   public static Integer incrementLevel(Long userId) {
      DatabaseUser dbu = getDatabaseObject(userId);
      Integer currentLevel = dbu.getLevel() == null ? 0 : dbu.getLevel();

      em.getTransaction().begin();
      dbu.setLevel(++currentLevel);
      em.persist(dbu);
      em.getTransaction().commit();

      return currentLevel;
   }

   public static Integer setLevel(Long userId, Integer level) {
      DatabaseUser dbu = getDatabaseObject(userId);

      em.getTransaction().begin();
      dbu.setLevel(level);
      em.persist(dbu);
      em.getTransaction().commit();

      return dbu.getLevel();
   }

   private static DatabaseUser getDatabaseObject(Long userId) {
      DatabaseUser dbu = em.find(DatabaseUser.class, userId);

      if (dbu == null) {
         em.getTransaction().begin();
         dbu = new DatabaseUser(userId);
         em.persist(dbu);
         em.getTransaction().commit();
      }

      return dbu;
   }
}
