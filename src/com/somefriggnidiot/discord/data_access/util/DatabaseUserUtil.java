package com.somefriggnidiot.discord.data_access.util;

import com.somefriggnidiot.discord.data_access.DatabaseConnector;
import com.somefriggnidiot.discord.data_access.DatabaseConnector.Table;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import java.sql.Timestamp;
import java.time.Instant;
import javax.persistence.EntityManager;

public class DatabaseUserUtil {

   private static EntityManager em = new DatabaseConnector().getEntityManager(Table.DATABASE_USER);

   public static DatabaseUser getUser(Long userId) {
      return getDatabaseObject(userId);
   }

   public static Integer addXp(Long userId, Integer xpGain) {
      DatabaseUser dbu = getDatabaseObject(userId);
      Integer currentXp = dbu.getXp() == null ? 0 : dbu.getXp();
      Integer newXp = currentXp + xpGain;

      em.getTransaction().begin();
      dbu.setXp(newXp);
      dbu.setLastMessageDtTm(Timestamp.from(Instant.now()));
      em.persist(dbu);
      em.getTransaction().commit();

      return newXp;
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
