package com.somefriggnidiot.discord.util;

import com.objectdb.o.InternalException;
import com.somefriggnidiot.discord.data_access.DatabaseConnector;
import com.somefriggnidiot.discord.data_access.DatabaseConnector.Table;
import com.somefriggnidiot.discord.data_access.models.BotModeEntry;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import java.time.Instant;
import java.util.List;
import javax.persistence.EntityManager;

public class BotModeUtil {

   private static EntityManager botModeEntryManager = new DatabaseConnector().getEntityManager
       (Table.BOT_MODE_ENTRY);

   public static boolean checkDuplicate(BotModeEntry botModeEntry) {
      GuildInfo gi = GuildInfoUtil.getGuildInfo(botModeEntry.getGuildId());
      List<Instant> botModeEntryIds = gi.getBotModeEntryIds();

      return botModeEntryIds.contains(botModeEntry.getId());
   }

   public static void createBotModeEntry(Long guildId, Long channelId, String prefix) {
      BotModeEntry bme = new BotModeEntry(guildId, channelId, prefix);
      GuildInfoUtil giu = new GuildInfoUtil(guildId);

      try {
         botModeEntryManager.getTransaction().begin();
         botModeEntryManager.persist(bme); //Throws IE for some reason.
         giu.addBotModeEntryId(bme.getId());
         botModeEntryManager.getTransaction().commit();
      } catch (InternalException ie) {
         throw ie;
      }
   }

   public static BotModeEntry getBotModeEntry(Instant entryId) {
      return botModeEntryManager.find(BotModeEntry.class, entryId);
   }
}
