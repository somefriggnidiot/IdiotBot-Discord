package com.somefriggnidiot.discord.events;

import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.VoiceXpUtil;
import com.somefriggnidiot.discord.util.XpUtil;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuildVoiceListener extends ListenerAdapter {

   private final Logger logger = LoggerFactory.getLogger(this.getClass());

   @Override
   public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
      logger.info(String.format("[%s] %s connected to %s.",
          event.getGuild(),
          event.getMember().getEffectiveName(),
          event.getChannelJoined()));
      }

   @Override
   public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
      logger.info(String.format("[%s] %s disconnected from %s.",
          event.getGuild(),
          event.getMember().getEffectiveName(),
          event.getChannelLeft()));
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
