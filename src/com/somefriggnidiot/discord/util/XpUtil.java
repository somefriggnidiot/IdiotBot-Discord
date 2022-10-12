package com.somefriggnidiot.discord.util;

import static java.lang.String.format;

import com.somefriggnidiot.discord.core.Main;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility functions pertaining to XP, levels, and level roles, but not directly writing data to
 * the database.
 */
public class XpUtil {
   private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);
   private static final DecimalFormat df = new DecimalFormat("###,###");

   /**
    * Determines if the {@link User} has reached an XP threshold for the next level to be earned
    * in the given {@link Guild}.
    *
    * @param guild the {@link Guild} in which the user is gaining XP.
    * @param user the {@link User} gaining XP.
    * @param newXp the new XP balance for the {@code DatabaseUser}
    * @return the amount of levels gained by this levelup.
    */
   public static Integer checkForLevelUp(Guild guild, User user, Integer newXp) {
      Long userId = user.getIdLong();
      Integer currentLevel = new DatabaseUserUtil(userId).getGuildLevel(guild.getIdLong());
      Integer xpThreshold = getXpThresholdForLevel(currentLevel+1);
      Integer newLevel = currentLevel;

      if (newXp > xpThreshold) {
         newLevel = new DatabaseUserUtil(userId).setGuildLevel(guild.getIdLong(), getLevelForXp(newXp));
         String newLevelString = format("**%s** (%s%s)",
             newLevel.toString(),
             (newLevel - currentLevel)>=1 ? "+" : "",
             (newLevel - currentLevel)
         );

         String effectiveName = guild.getMember(user).getEffectiveName();

         EmbedBuilder eb = new EmbedBuilder()
             .setColor(Color.CYAN)
             .setThumbnail(user.getAvatarUrl())
             .setTitle(format("%s has leveled up!", effectiveName))
             .addField("Current Level", newLevelString, true)
             .addField("Progress to Next Level",
                 df.format(newXp) + " / " + df.format(getXpThresholdForLevel(newLevel+1)),
                 true);

         List <Role> newRoles = handleRoleAssignments(userId, guild);
         if (newRoles != null && !newRoles.isEmpty()) {
            String roleList = "";

            for (Role role : newRoles) {
               roleList = roleList.concat(role.getName() + ", ");
            }

            roleList = roleList.substring(0, roleList.length()-2);
            eb.addField("Role(s) Unlocked", roleList, true);
         }

         new GuildInfoUtil(guild).getBotTextChannel().sendMessage(eb.build()).queue();

      } else if (newXp < getXpThresholdForLevel(currentLevel)) {
         newLevel = new DatabaseUserUtil(userId).setGuildLevel(guild.getIdLong(), getLevelForXp(newXp));
         logger.info(format("[%s] Demoted %s to level %s.",
             guild,
             user.getName(),
             newLevel));

         handleRoleAssignments(userId, guild);
      }

      return newLevel - currentLevel;
   }

   /**
    * Checks for and assigns any unassigned roles earned through Role Level Mappings.
    *
    * @param userId the ID of the {@link User} being checked.
    * @param guild the {@link Guild} in which the {@code User} resides.
    * @return a list of {@link Role} objects matching any roles newly assigned to the {@code User}.
    */
   private static List<Role> handleRoleAssignments(Long userId, Guild guild) {
      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild.getIdLong());

      if (gi.getLevelRolesActive() || gi.getRoleLevelMappings().size() > 0) {
         HashMap<Long, Integer> roleLevelIds = gi.getRoleLevelMappings();
         Integer userLevel = new DatabaseUserUtil(userId).getLevel(guild.getIdLong());
         List<Long> applicableRoleIds = roleLevelIds.keySet().stream()
             .filter(e -> roleLevelIds.get(e) <= userLevel)
             .collect(Collectors.toList());
         List<Role> applicableRoles = new ArrayList<>();
         applicableRoleIds.forEach(e -> applicableRoles.add(guild.getRoleById(e)));
         Optional<Member> member = Optional.of(guild.getMemberById(userId));

         if (member.isPresent()) {
            List<Role> newRoles = applicableRoles.stream()
                .filter(role -> !member.get().getRoles().contains(role))
                .collect(Collectors.toList());

            newRoles.forEach(r -> {
               try {
                  guild.addRoleToMember(member.get(), r).queue();
               } catch (Exception e) {
                  logger.error(format("[%s] Error when adding %s to %s role: ", guild, member, r), e);
               }
            });

            return newRoles;
         } else {
            logger.warn(format("[%s] Member not found for ID: %s", guild, userId));
         }

      }

      return null;
   }

   /**
    * Determines what level a user would be at for a given XP value.
    *
    * @param xp an XP value.
    * @return the level a user should be at for the provided XP value.
    */
   public static Integer getLevelForXp(Integer xp) {
      Integer level = -1;
      Integer thresholdXp = getXpThresholdForLevel(level);

      while (xp > thresholdXp) {
         thresholdXp = getXpThresholdForLevel(level);

         if (xp < thresholdXp) {
            break;
         } else {
            level++;
         }
      }

      level--;
      return level <= 0 ? 0 : level;
   }

   /**
    * Determines what XP threshold a user would need to reach to achieve a certain level.
    *
    * @param level a level value.
    * @return the amount of XP a user would need to achieve that level.
    */
   public static Integer getXpThresholdForLevel(Integer level) {
      Integer threshold = 0;

      for (int i=0; i<level; i++) {
         threshold += 5*(i*i) + 100*(i) + 250;
      }

      return threshold;
   }

   /**
    * Retrieves the ranked position of a {@link DatabaseUser} on a {@link Guild}'s leaderboard.
    *
    * @param guild the {@link Guild} in which the {@code User} resides.
    * @param user the {@link User} linked to a {@code DatabaseUser} object which has earned XP in
    * the provided {@code Guild}
    * @return the ranked position relative to all other {@code DatabaseUser} objects in a guild.
    */
   public static Integer getGuildRank(Guild guild, User user) {
      GuildInfoUtil giu = new GuildInfoUtil(guild);
      List<HighscoreObject> rankedList = giu.getRankedXpList();
      List<DatabaseUser> rankedDbus = new ArrayList<>();

      rankedList.forEach(e -> rankedDbus.add(e.getUser()));
      DatabaseUser dbu = DatabaseUserUtil.getUser(user.getIdLong());

      return rankedDbus.indexOf(dbu) + 1;
   }

   /**
    * Retrieves the total number of {@link DatabaseUser}s for a {@link Guild}, regardless of
    * their XP balances.
    *
    * @param guild the {@link Guild} for which all {@code DatabaseUser}s are being retrieved.
    * @return the size of the list containing all {@link DatabaseUser}s for a {@code Guild}.
    */
   public static Integer getGuildLeaderboardSize(Guild guild) {
      GuildInfoUtil giu = new GuildInfoUtil(guild);

      return giu.getRankedXpList().size();
   }

   /**
    * Random number generator to determine if a drop has been activated..
    *
    * @return whether or not a token drop has been activated.
    */
   public static Boolean tokenDropActivated() {
      return ThreadLocalRandom.current().nextInt(201) == 1;
   }

   /**
    * Refreshes role level assignments for a Guild. All members of the Guild
    * will be removed from mapped level roles for which they would not have the
    * proper level, and will be added to all roles for which their level should
    * have access.
    *
    * Operation will not affect roles which are not part of the server's role level
    * mappings.
    *
    * @param guild the {@link Guild} for which role level assignments shall be
    * recalculated.
    */
   public static void updateLevelRoleAssignments(Guild guild) {
      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild);

      if (gi.getLevelRolesActive() || gi.getRoleLevelMappings().size() > 0) {
         List<Member> members = guild.getMembers();
         /**
          * Key / Long = Role ID
          * Value / Integer = Level Unlocked
          */
         Map<Long, Integer> roleMappings = gi.getRoleLevelMappings();
         List<Role> rolesMapped = guild.getRoles().stream()
             .filter(role -> roleMappings.containsKey(role.getIdLong())).collect(Collectors.toList());

         for (Member member : members) {
            List<Role> rolesAllowed = new ArrayList<>();
            List<Role> rolesToRevoke = new ArrayList<>();
            Integer memberXp = DatabaseUserUtil.getUser(
                member.getIdLong()).getXpMap().getOrDefault(guild.getIdLong(), 0);

            for (Role role : rolesMapped) {
               Integer roleLevel = roleMappings.get(role.getIdLong());
               Integer memberLevel = XpUtil.getLevelForXp(memberXp) == null ? 0 : XpUtil
                   .getLevelForXp(memberXp);

               if (roleLevel > memberLevel) {
                  //Revoke - Member's level is below required for role.
                  logger.debug(format("[%s] Removing %s from Level %s Role: %s", guild, member
                      .getEffectiveName(), roleLevel, role.getName()));
                  rolesToRevoke.add(role);
               } else {
                  //Allow
                  logger.debug(format("[%s] Adding %s to Level %s Role: %s", guild, member
                      .getEffectiveName(), roleLevel, role.getName()));
                  rolesAllowed.add(role);
               }
            }

            guild.modifyMemberRoles(member, rolesAllowed, rolesToRevoke).complete();
         }
      }
   }

   /**
    * Updates a user to apply an additional amount of tokens.
    *
    * <p>
    * Provided a {@link User} and {@link Guild}, will modify the {@link DatabaseUser}'s token
    * balance for the {@code Guild}.
    * </p>
    *
    * @param guild the {@link Guild} entity containing the user being modified.
    * @param user the {@link User} whose tokens are being adjusted.
    * @param tokens the adjustment being made to the User's token count. Can be negative to
    * reduce tokens.
    */
   public static void handleTokenDrops(Guild guild, User user, Integer tokens) {
      DatabaseUserUtil dbuu = new DatabaseUserUtil(user.getIdLong());
      Integer newTokens = dbuu.addTokens(guild.getIdLong(), tokens);

      logger.info(format("[%s] %s has gained %s token(s)! Now at %s.",
          guild,
          user.getName(),
          tokens,
          newTokens));
   }

   public static Boolean luckMultiplierActivated() {
      Integer rnGesus = ThreadLocalRandom.current().nextInt(0, 1000);
      return rnGesus == 1;
   }

   public static Double getLuckMultiplier() {
      Double mult = ThreadLocalRandom.current().nextDouble(0.50, 10.00);

      logger.info("Multiplier of " + mult + " is being applied!");

      return mult > 1.00 ? mult : 1.00;
   }
}
