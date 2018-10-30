package com.somefriggnidiot.discord.data_access.util;

import com.somefriggnidiot.discord.data_access.DatabaseConnector;
import com.somefriggnidiot.discord.data_access.DatabaseConnector.Table;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import javax.persistence.EntityManager;

public class GuildInfoUtil {

   static EntityManager em = new DatabaseConnector().getEntityManager(Table.GUILD_INFO);

   public static GuildInfo getGuildInfo(Long guildId) {
      return getDatabaseObject(guildId);
   }

   public static void enableGameGrouping(Long guildId) {
      GuildInfo gi = getGuildInfo(guildId);

      em.getTransaction().begin();
      gi.setGroupMappingsActive(true);
      em.persist(gi);
      em.getTransaction().commit();
   }

   public static void disableGameGrouping(Long guildId) {
      GuildInfo gi = getGuildInfo(guildId);

      em.getTransaction().begin();
      gi.setGroupMappingsActive(false);
      em.persist(gi);
      em.getTransaction().commit();
   }

   public static Boolean removeRoleMapping(Long guildId, String gameName) {
      GuildInfo gi = getDatabaseObject(guildId);
      String role;

      em.getTransaction().begin();
      role = gi.removeGameGroupMapping(gameName);
      em.persist(gi);
      em.getTransaction().commit();

      return !(role == null || role.isEmpty());
   }

   public static void addGameRoleMapping(Long guildId, String gameName, String roleName) {
      GuildInfo gi = getDatabaseObject(guildId);

      em.getTransaction().begin();
      gi.addGameGroupMapping(gameName, roleName);
      em.persist(gi);
      em.getTransaction().commit();
   }

   private static GuildInfo getDatabaseObject(Long guildId) {
      GuildInfo gi = em.find(GuildInfo.class, guildId);

      if (gi == null) {
         em.getTransaction().begin();
         gi = new GuildInfo(guildId);
         em.persist(gi);
         em.getTransaction().commit();
      }

      return gi;
   }
}
