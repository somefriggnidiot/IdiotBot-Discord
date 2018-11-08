package com.somefriggnidiot.discord.events;

import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.XpUtil;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuildVoiceListener extends ListenerAdapter {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   @Override
   public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
      logger.info(String.format("[%s] %s connected to %s.",
          event.getGuild(),
          event.getMember().getEffectiveName(),
          event.getChannelJoined()));

      if (GuildInfoUtil.getGuildInfo(event.getGuild().getIdLong()).isGrantingMessageXp()) {
         Timer timer = new Timer();

         timer.schedule((new TimerTask() {
            Integer executions = 0;

            @Override
            public void run() {
               GuildVoiceState voiceState = event.getVoiceState();
               Boolean isActive = !voiceState.isDeafened() && !voiceState.isMuted() &&
                   !voiceState.isGuildDeafened() && !voiceState.isGuildMuted() &&
                   !voiceState.isSuppressed();

               if (!voiceState.inVoiceChannel()){
                  timer.cancel();
                  return;
               }

               if(isActive && voiceState.inVoiceChannel() &&
                   event.getChannelJoined().getMembers().size() > 1) {
                  Integer xpGained = ThreadLocalRandom.current().nextInt(25,51) + (2*executions);
                  Integer newXp = DatabaseUserUtil.addXp(event.getMember().getUser().getIdLong(), xpGained);
                  XpUtil.checkForLevelUp(event, newXp);
                  logger.info(String.format("[%s] %s gained %s xp for participating in voice. "
                          + "They're now at %s xp.",
                      event.getGuild(),
                      event.getMember().getEffectiveName(),
                      xpGained,
                      newXp));
               }
            }
         }), 300000, 300000);
      }
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
