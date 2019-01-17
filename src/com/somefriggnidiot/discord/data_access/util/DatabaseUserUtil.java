package com.somefriggnidiot.discord.data_access.util;

import com.somefriggnidiot.discord.data_access.DatabaseConnector;
import com.somefriggnidiot.discord.data_access.DatabaseConnector.Table;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.util.XpUtil;
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
      Integer newTokens = currentTokens + tokenGain < 0 ? 0 : currentTokens + tokenGain;

      em.getTransaction().begin();
      dbu.updateTokens(guildId, newTokens);
      em.persist(dbu);
      em.getTransaction().commit();

      return newTokens;
   }

   public Integer getTokens(Long guildId) {
      Integer tokens = dbu.getTokenMap().get(guildId);

      return tokens == null ? 0 : tokens;
   }

   public void setTokens(Long guildId, Integer newTokenBalance) {
      em.getTransaction().begin();
      dbu.updateTokens(guildId, newTokenBalance);
      em.persist(dbu);
      em.getTransaction().commit();
   }

   public Integer getGuildLevel(Long guildId) {
      Integer level = dbu.getLevelMap().get(guildId);

      return level == null ? 0 : level;
   }

   public Integer setGuildLevel(Long guildId, Integer newLevel) {
      em.getTransaction().begin();
      dbu.updateLevel(guildId, newLevel);
      em.persist(dbu);
      em.getTransaction().commit();

      return newLevel;
   }

   /**
    *
    * @param guildId
    * @param userId
    * @param xpGain
    * @return the {@code DatabaseUser}'s new XP balance.
    */
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

   public Integer getLevel(Long guildId) {
      Integer level = dbu.getLevelMap().get(guildId);

      if (level == null || level == 0) {
         Integer xp = dbu.getXpMap().get(guildId);

         if (xp != null && xp > 0) {
            return XpUtil.getLevelForXp(xp);
         } else {
            return 0;
         }
      } else {
         return level;
      }
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
