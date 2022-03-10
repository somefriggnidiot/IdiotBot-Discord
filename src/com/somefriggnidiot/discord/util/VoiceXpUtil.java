package com.somefriggnidiot.discord.util;

import static java.lang.String.format;

import com.somefriggnidiot.discord.core.Main;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains helper and utility functions for the Voice XP Timer and its handling of granting XP
 * to users.
 */
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

   /**
    * Starts execution of a Voice XP Timer for a given {@link Guild} if one is not already running.
    *
    * @see VoiceXpUtil#getTimerTask(Long)
    * @param guildId the {@link Guild} for which the timer is starting.
    */
   public static void startTimer(Long guildId) {
      if (!guildTimers.containsKey(guildId)) {
         Timer guildTimer = new Timer();
         guildTimer.schedule(getTimerTask(guildId), 300000, 300000);
         guildTimers.put(guildId, guildTimer);

         logger.info(String.format("[%s] VoiceXP: Started voice XP timer.",
             Main.jda.getGuildById(guildId)));
      } else {
         logger.warn(format("[%s] VoiceXP: Attempted to start a voice XP timer, but one already "
                 + "existed for this guild.",
             Main.jda.getGuildById(guildId)));
      }
   }

   /**
    * Stops execution of a Voice XP Timer for a given {@link Guild} if one is running.
    *
    * @param guildId the {@link Guild} for which the timer is being stopped.
    */
   public static void stopTimer(Long guildId) {
      if (guildTimers.containsKey(guildId)) {
         Timer guildTimer = guildTimers.get(guildId);
         guildTimer.cancel();
         guildTimer.purge();
      } else {
         logger.warn(format("[%s] VoiceXP: Attempted to stop voice XP timer, but no timer was "
                 + "found.",
             Main.jda.getGuildById(guildId)));
      }
   }

   /**
    * Resets the consecutive execution count for a given user.
    * <br />
    * Each time a {@code User} gains XP through the Voice XP Timer, this count is incremented by
    * one until being reset back to zero.
    *
    * @param user the {@link User} whose execution count is being reset.
    */
   @Deprecated
   public static void resetUserExecutions(User user) {
      userExecutions.remove(user.getIdLong());
   }

   /**
    * Resets the consecutive execution count for a given user if they have not gained any XP after
    * the specified delay.
    *
    * @param user the {@link User} whose execution count may be reset.
    * @param delay the time IN MINUTES after which execution count may be reset.
    */
   private static void resetUserExecutionsDelayed(User user, long delay) {
      DatabaseUser dbu = DatabaseUserUtil.getUser(user.getIdLong());
      Instant resetTime = dbu.getLatestGain().toInstant().plus(delay, ChronoUnit.MINUTES);

      if (!userExecutions.containsKey(user.getIdLong()) ||
          userExecutions.get(user.getIdLong()) == 0) {
         return;
      }

      //Reset the executions if the current time is past the reset time.
      if (Instant.now().isAfter(resetTime)) {
         Integer executions = userExecutions.remove(user.getIdLong());

         logger.info(format("VoiceXP: %s has had their executions reset. Was %s.",
             user.getName(),
             executions));
      }
   }

   /**
    * Retrieves the {@link TimerTask} being executed each iteration for a {@link Guild}.
    *
    * @param guildId the {@link Guild} for which the {@code TimerTask} is being retrieved.
    * @return the {@link TimerTask} in use for that {@code Guild}.
    */
   private static TimerTask getTimerTask(Long guildId) {
      return new TimerTask() {

         @Override
         public void run() {
            Guild guild = Main.jda.getGuildById(guildId);

            /*
               Grants "voice activity" XP for users in voice channels. Process additionally
               increments the execution count so members may gain more xp over time.
             */
            List<VoiceChannel> nonEmptyChannels = guild.getVoiceChannels().stream()
                .filter(voiceChannel -> voiceChannel.getMembers().size() > 1)
                .collect(Collectors.toList());

            for (VoiceChannel channel : nonEmptyChannels) {
               for (Member member : channel.getMembers()) {
                  User user = member.getUser();
                  Integer oldExecutions = userExecutions.get(user.getIdLong()) == null ? 0 :
                      userExecutions.get(user.getIdLong());
                  Integer newExecutions = handleVoiceXp(channel, guild, user, oldExecutions);
                  userExecutions.put(user.getIdLong(), newExecutions);
               }
            }

            /*
               Scans list of execution counts for stale entries to reset executions for members
               who are no longer in voice and have not gained XP after a specified time.
             */
            final Set<Long> userIds = userExecutions.keySet();

            if (!userIds.isEmpty()) {
               Set<Member> members = userIds.stream()
                   .map(guild::getMemberById)
                   .collect(Collectors.toSet());

               try {
                  for (final Member member : members) {

                     try {
                        Thread.sleep(100);
                     } catch (InterruptedException e) {
                        logger.error("Thread interrupted while executing timer task.");
                     }

                     if (member != null && (
                         member.getVoiceState() == null ||
                             !member.getVoiceState().inVoiceChannel())) {
                        resetUserExecutionsDelayed(member.getUser(), 20);
                     }
                  }
               } catch (ConcurrentModificationException cme) {
                  logger.error("Concurrent modification exception with " + guild.getName() +
                      " execution resets.");
               }

            }
         }
      };
   }

   /**
    * Determines whether a {@link User} is eligible to earn XP, assigning it accordingly.
    *
    * @param channel the {@link VoiceChannel} in which the {@code User} currently resides.
    * @param guild the {@link Guild} containing the {@code VoiceChannel} and {@code User}.
    * @param user the {@link User} potentially gaining XP.
    * @param executions the total times this {@code User} has gained XP from execution of this
    * command without being reset by {@link VoiceXpUtil#resetUserExecutions(User)}
    * @return the new total value for {@code executions}.
    */
   private static Integer handleVoiceXp(VoiceChannel channel, Guild guild, User user, Integer
       executions) {
      GuildVoiceState voiceState = guild.getMember(user).getVoiceState();
      /*
       * Returns true if the user is:
       *  & not deafened
       *  & not suppressed
       *  & not muted by the guild. (Self-mute is fine.)
       */
      Boolean isActive = !voiceState.isDeafened() && !voiceState.isGuildMuted()
          && !voiceState.isSuppressed();

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
            - Useer is deafened
       */
      if (!isActive) {
         return executions;
      }

      if (channel.getMembers().size() < 2) {
         return ++executions;
      }

      /*
         GIVE XP WHEN
            - User is active, guild is tracking, user is not alone.

         XP Calculations:
            - Base is random between 15 and 25 xp.
            - Add an increasing bonus of 1.5 xp for every execution they've been eligible.
            - Apply a multiplier of 5% for each member in the channel over 2.
            - Apply an arbitrary multiplier set by the guild.
       */
      if (voiceState.inVoiceChannel() && channel.getMembers().size() > 1) {
         GuildInfo gi = GuildInfoUtil.getGuildInfo(guild.getIdLong());
         int base = 0;
         double bonus = 3.0 * (executions > 100 ? 100 : executions);
         double multiplier = 0.9 + (0.05 * channel.getMembers().size());
         double voiceMultiplier = new GuildInfoUtil(guild.getIdLong()).getVoiceXpMultiplier();
         Integer level = DatabaseUserUtil.getUser(user.getIdLong()).getLevelMap()
             .get(guild.getIdLong());
         if (level != null) {
            if (level >= 45) {
               base = 15;
            } else if (level >= 35) {
               base = 10;
            } else if (level >= 25) {
               base = 5;
            } else if (level <= 10 && level >= 5) {
               base = level - 5;
            }
         }

         if(executions < 3) {
            base += ThreadLocalRandom.current().nextInt(1, 11);
         } else {
            base += ThreadLocalRandom.current().nextInt(15, 26);
         }

         //Check for token drops.
         if (XpUtil.tokenDropActivated()) {
            XpUtil.handleTokenDrops(guild, user, 1);
         }

         //Check if user is in supported game.
         List<Role> userRoles = guild.getMember(user).getRoles();
         Role gameGroupRole = null;
         try {
            if (gi.gameGroupsAutomatic()) {
               List<String> groups = GameGroupUtil.getGameGroupUtil(guild).getActiveAutoGroups();
               for (Role userRole : userRoles) {
                  if (groups.contains(userRole.getName())) {
                     gameGroupRole = userRole;
                  }
               }
            } else {
               gameGroupRole = userRoles.stream()
                   .filter(role -> gi.getGameGroupMappings().values().stream()
                       .distinct()
                       .collect(Collectors.toList())
                       .contains(role.getName()))
                   .collect(Collectors.toList())
                   .get(0);
            }
         } catch (Exception e) {
            logger.debug(format("[%s] VoiceXP: %s is not in a game group role. Game: %s",
                guild,
                guild.getMember(user).getEffectiveName(),
                guild.getMember(user).getActivities().size() > 0 ? guild.getMember(user)
                    .getActivities().get(0) : ""));
         }

         Integer usersInGame = usersPlayingTogether(channel, gameGroupRole);
         multiplier += (0.15 * usersInGame);

         //Check for Luck Bonus
         Boolean luckMultActive = XpUtil.luckMultiplierActivated();
         if (gi.luckBonusActive() && luckMultActive) {
            multiplier += XpUtil.getLuckMultiplier();
         }

         //Give XP
         Integer xpGained = (int) Math.rint((base + bonus) * (multiplier + voiceMultiplier));
         Integer newXp = DatabaseUserUtil.addXp(
             guild.getIdLong(),
             user.getIdLong(),
             xpGained);

         if (luckMultActive) {
            Double mult = multiplier + 0.00000;
            String multStr = mult.toString().substring(0, 4);
            String xpGainStr = String.valueOf(xpGained.doubleValue());
            String message = guild.getMemberById(user.getIdLong()).getEffectiveName() + " has gotten"
                + " a random XP multiplier of " + multStr + " for a drop of " + xpGainStr + " XP!";
            new GuildInfoUtil(guild).getBotTextChannel().sendMessage(message).queue();
         }

         logger.debug(format("user: %s "
                 + "base_xp: %s "
                 + "activity_bonus: %s "
                 + "multiplier: %s "
                 + "channel_users: %s "
                 + "xp_gained: %s "
                 + "xp_total: %s "
                 + "users_in_game: %s "
                 + "executions: %s "
                 + "lastgainz: %s ",
             user.getName(),
             base,
             bonus,
             multiplier, channel.getMembers().size(),
             xpGained,
             newXp,
             usersInGame,
             executions,
             DatabaseUserUtil.getUser(user.getIdLong()).getLatestGain()));

         //Check for levelup.
         XpUtil.checkForLevelUp(guild, user, newXp);

         logger.info(format("[%s] VoiceXP: %s gained %s xp for participating in voice. "
                 + "They're now at %s xp.",
             guild,
             guild.getMember(user).getEffectiveName(),
             xpGained,
             df.format(newXp)));
      }

      return ++executions;
   }

   /**
    * Calculates the amount of {@link User}s in a {@link VoiceChannel} that are also in the same
    * Game Group {@link Role}. Used to get a rough estimate of how many {@code User}s may be
    * playing a game in the same lobby.
    *
    * @param channel the {@link VoiceChannel} containing the {@link User}s being scanned.
    * @param gameGroup the {@link Role} of the Game Group being scanned.
    * @return the count of {@link User}s in the {@link VoiceChannel} that share the same Game
    * Group {@link Role}.
    */
   private static Integer usersPlayingTogether(VoiceChannel channel, Role gameGroup) {
      long count = channel.getMembers()
          .stream()
          .filter(member -> member.getRoles().contains(gameGroup))
          .count();

      return Integer.valueOf(Long.toString(count));
   }
}
