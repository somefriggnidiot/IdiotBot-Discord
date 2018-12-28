package com.somefriggnidiot.discord.util;

import static java.lang.String.format;

import com.somefriggnidiot.discord.core.Main;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceXpUtil {

   private static final Logger logger = LoggerFactory.getLogger(VoiceXpUtil.class);
   private static final DecimalFormat df = new DecimalFormat("###,###");
   /**
    * Key=GuildId; Timer=Timer for guild
    */
   private static final Map<Long, Timer> guildTimers = new HashMap<>();
   /**
    * Key=UserId; Long=Executions
    */
   private static Map<Long, Integer> userExecutions = new HashMap<>();

   public static void startTimer(Long guildId) {
      if (!guildTimers.containsKey(guildId)) {
         Timer guildTimer = new Timer();
         guildTimer.schedule(getTimerTask(guildId), 300000, 300000);

         guildTimers.put(guildId, guildTimer);
      } else {
         logger.warn(format("[%s] Attempted to start a voice XP timer, but one already existed "
             + "for this guild.",
             Main.jda.getGuildById(guildId)));
      }
   }

   public static void stopTimer(Long guildId) {
      if (guildTimers.containsKey(guildId)) {
         Timer guildTimer = guildTimers.get(guildId);
         guildTimer.cancel();
         guildTimer.purge();
      } else {
         logger.warn(format("[%s] Attempted to stop voice XP timer, but no timer was found.",
             Main.jda.getGuildById(guildId)));
      }
   }

   public static void resetUserExecutions(User user) {
      userExecutions.remove(user.getIdLong());
   }

   private static TimerTask getTimerTask(Long guildId) {
      return new TimerTask() {

         @Override
         public void run() {
            Guild guild = Main.jda.getGuildById(guildId);

            List<VoiceChannel> nonEmptyChannels = guild.getVoiceChannels().stream()
                .filter(voiceChannel -> voiceChannel.getMembers().size() > 1)
                .collect(Collectors.toList());

            //For every voice channel with members...
            for (VoiceChannel channel : nonEmptyChannels) {
               //For every voice user
               for (Member member : channel.getMembers()) {
                  //Handle XP and increment counter
                  User user = member.getUser();
                  Integer oldExecutions = userExecutions.get(user.getIdLong()) == null ? 0 :
                      userExecutions.get(user.getIdLong());
                  Integer newExecutions = handleVoiceXp(channel, guild, user, oldExecutions);
                  userExecutions.put(user.getIdLong(), newExecutions);
               }
            }
         }
      };
   }

   private static Integer handleVoiceXp(VoiceChannel channel, Guild guild, User user, Integer
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
            logger.debug(format("[%s] %s is not in a game group role. Game: %s",
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

         logger.debug(format("base_xp: %s "
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

         logger.info(format("[%s] %s gained %s xp for participating in voice. "
                 + "They're now at %s xp.",
             guild,
             user.getName(),
             xpGained,
             df.format(newXp)));
      }

      return ++executions;
   }

   private static Integer usersPlayingTogether(VoiceChannel channel, Role gameGroup) {
      Long count = channel.getMembers()
          .stream()
          .filter(member -> member.getRoles().contains(gameGroup))
          .count();

      return Integer.valueOf(count.toString());
   }
}
