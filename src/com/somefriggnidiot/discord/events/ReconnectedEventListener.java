package com.somefriggnidiot.discord.events;

import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.GameGroupUtil;
import com.somefriggnidiot.discord.util.VoiceXpUtil;
import java.util.List;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconnectedEventListener extends ListenerAdapter {

   private final Logger logger = LoggerFactory.getLogger(ReconnectedEventListener.class);

   @Override
   public void onReconnect(final ReconnectedEvent event) {
      logger.info("Session reconnected.");
      List<Guild> guilds = event.getJDA().getGuilds();

      for (Guild guild : guilds) {
         GuildInfo gi = GuildInfoUtil.getGuildInfo(guild.getIdLong());

         if (gi.isGrantingMessageXp()) {
            VoiceXpUtil.startTimer(guild.getIdLong());
         }

         if (gi.isGroupingGames()) {
            logger.info(String.format("[%s] Started Game Groups tracking. Refreshing role "
                + "assignments.", guild));
            GameGroupUtil.refreshGameGroups(guild);
         }
      }
   }

   @Override
   public void onResume(final ResumedEvent event) {
      logger.info("Session resumed.");
      List<Guild> guilds = event.getJDA().getGuilds();

      for (Guild guild : guilds) {
         GuildInfo gi = GuildInfoUtil.getGuildInfo(guild.getIdLong());

         if (gi.isGrantingMessageXp()) {
            VoiceXpUtil.startTimer(guild.getIdLong());
         }

         if (gi.isGroupingGames()) {
            logger.info(String.format("[%s] Started Game Groups tracking. Refreshing role "
                + "assignments.", guild));
            GameGroupUtil.refreshGameGroups(guild);
         }
      }
   }

   @Override
   public void onReady(final ReadyEvent event) {
      logger.info("Session ready.");
   }

}
