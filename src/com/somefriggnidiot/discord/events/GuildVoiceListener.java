package com.somefriggnidiot.discord.events;

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuildVoiceListener extends ListenerAdapter {

   private final Logger logger = LoggerFactory.getLogger(this.getClass());

   @Override
   public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
      logger.info(String.format("[%s] [VC | %s] %s connected.",
          event.getGuild(),
          event.getChannelJoined().getName(),
          event.getMember().getEffectiveName()));
      }

   @Override
   public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
      logger.info(String.format("[%s] [VC | %s] %s disconnected.",
          event.getGuild(),
          event.getChannelLeft().getName(),
          event.getMember().getEffectiveName()));
   }

   @Override
   public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
      logger.info(String.format("[%s] %s moved from %s to %s.",
          event.getGuild(),
          event.getMember().getEffectiveName(),
          event.getChannelLeft(),
          event.getChannelJoined()));
   }
}
