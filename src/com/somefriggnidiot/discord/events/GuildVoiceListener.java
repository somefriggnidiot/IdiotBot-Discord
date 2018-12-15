package com.somefriggnidiot.discord.events;

import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
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
            VoiceChannel channel = event.getChannelJoined();

            @Override
            public void run() {
               executions = handleVoiceXp(timer, channel, event.getGuild(), event.getMember()
                       .getUser(), executions);
            }}), 300000, 300000);
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

      if (GuildInfoUtil.getGuildInfo(event.getGuild().getIdLong()).isGrantingMessageXp()) {
         Timer timer = new Timer();

         timer.schedule(new TimerTask() {
            Integer executions = 0;
            VoiceChannel channel = event.getChannelJoined();

            @Override
            public void run() {
               executions = handleVoiceXp(timer, channel, event.getGuild(), event.getMember()
                   .getUser(), executions);
            }
         }, 300000, 300000);
      }
   }

   private Integer handleVoiceXp(Timer timer, VoiceChannel channel, Guild guild, User user, Integer
       executions) {
      GuildVoiceState voiceState = guild.getMember(user).getVoiceState();
      Boolean isActive = !voiceState.isDeafened() && !voiceState.isMuted() &&
          !voiceState.isGuildDeafened() && !voiceState.isGuildMuted() &&
          !voiceState.isSuppressed();

      /*
         EXIT WHEN
            - User is no longer in voice channel.
            - Guild is no longer tracking xp.
       */
      if (!GuildInfoUtil.getGuildInfo(guild.getIdLong()).isGrantingMessageXp() ||
          !guild.getVoiceChannelById(channel.getId()).getMembers().contains(guild.getMember(user)) ||
          !voiceState.inVoiceChannel()) {
         timer.cancel();
         return executions;
      }

      /*
         SKIP WHEN
            - Useer is muted/deafened
            - User is only one in channel
       */
      if (!isActive || channel.getMembers().size() < 2) {
         return executions;
      }

      /*
         GIVE XP WHEN
            - User is active, guild is tracking, user is not alone.
       */
      if (voiceState.inVoiceChannel() && channel.getMembers().size() > 1) {
         GuildInfo gi = GuildInfoUtil.getGuildInfo(guild.getIdLong());
         Integer base = ThreadLocalRandom.current().nextInt(15, 26);
         Double bonus = 1.5 * (executions > 36 ? 36 : executions);
         Double multiplier = 0.9 + (0.05 * channel.getMembers().size());
         Double voiceMultiplier = new GuildInfoUtil(guild.getIdLong()).getVoiceXpMultiplier();

         //Check for token drops.
         if (XpUtil.tokenDropActivated()) {
            XpUtil.handleTokenDrops(guild, user, 1);
         }

         //Check if user is in supported game.
         List<Role> userRoles = guild.getMember(user).getRoles();
         Role gameGroupRole = null;
         try {
            gameGroupRole = userRoles.stream()
                .filter(role -> gi.getGameGroupMappings().values().stream()
                    .distinct()
                    .collect(Collectors.toList())
                    .contains(role.getName()))
                .collect(Collectors.toList())
                .get(0);
         } catch (Exception e) {
            logger.debug(String.format("[%s] %s is not in a game group role. Game: %s",
                guild,
                user.getName(),
                guild.getMember(user).getGame()));
         }

         Integer usersInGame = usersPlayingTogether(channel, gameGroupRole);
         multiplier += (0.05 * usersInGame);

         //Give XP
         Integer xpGained = (int) Math.rint((base + bonus) * (multiplier + voiceMultiplier));
         Integer newXp = DatabaseUserUtil.addXp(
             guild.getIdLong(),
             user.getIdLong(),
             xpGained);

         logger.debug(String.format("base_xp: %s "
                 + "activity_bonus: %s "
                 + "multiplier: %s "
                 + "channel_users: %s "
                 + "xp_gained: %s "
                 + "xp_total: %s "
                 + "users_in_game: %s ",
             base,
             bonus,
             multiplier, channel.getMembers().size(),
             xpGained,
             newXp,
             usersInGame));

         //Check for levelup.
         XpUtil.checkForLevelUp(guild, user, newXp);

         logger.info(String.format("[%s] %s gained %s xp for participating in voice. "
                 + "They're now at %s xp.",
             guild,
             user.getName(),
             xpGained,
             newXp));
      }

      return ++executions;
   }

   private Integer usersPlayingTogether(VoiceChannel channel, Role gameGroup) {
      Long count = channel.getMembers()
          .stream()
          .filter(member -> member.getRoles().contains(gameGroup))
          .count();

      return Integer.valueOf(count.toString());
   }
}
