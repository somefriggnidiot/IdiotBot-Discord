package com.somefriggnidiot.discord.util;

import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO Refactor GameGroups to create roles and auto-group for any game with 2+ players or allow
// forced role creation.

/**
 * Various helper functions for GameGroups. <br />
 * GameGrouping is a concept of assigning a Discord {@link Role} to a
 * {@link net.dv8tion.jda.api.entities.User} temporarily while that {@code User}
 * is playing a certain {@link Activity} as defined by {@code User}-defined rules.
 *
 * @see com.somefriggnidiot.discord.commands.functionalities.gamegroups.AddGameGroupCommand
 * @see com.somefriggnidiot.discord.commands.functionalities.gamegroups.RemoveGameGroupCommand
 * @see com.somefriggnidiot.discord.commands.functionalities.gamegroups.GroupGamesCommand
 */
public class GameGroupUtil {

   private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   /**
    * Assigns the GameGroup {@link Role} to a {@link Member} playing
    * a {@link Activity} that has a valid GameGroup mapping in the {@link Guild}.
    *
    * @param guild the guild in which these actions are taking place.
    * @param gameMap the complete {@link HashMap} mapping {@code Game} display names to
    * {@code Role}s for a {@code Guild}.
    * @param gameName this will eventually just be pulled from the Member.
    * @param member the {@link Member} playing a {@code Game}.
    */
   public static void handleRoleAssignment(Guild guild, HashMap<String, String> gameMap,
       String gameName, Member member) {
      logger.info("Handling role assignment via GameGroupUtil.");
      if (!isValidGame(gameMap, gameName)) {
         return;
      }
      String groupName = gameMap.get(gameName);

      // Get role
      Role role = null;
      try {
         role = guild.getRolesByName(groupName, false).get(0);
      } catch (IndexOutOfBoundsException e) {
         logger.debug(String.format("%s does not have a role matching %s", guild
             .getName(), groupName));
      }
      // Throw error if the role doesn't exist.
      if (role == null) {
         logger.warn(String.format("[%s] Role not found for \"%s\"", guild, groupName));
         logger.error("Guild is missing role for game.");
         return;
      }

      // Do the actual role shit.
      logger.info(String.format("[%s] %s has started playing %s.",
          guild,
          member.getEffectiveName(),
          member.getActivities().get(0).getName()));

      try {
         guild.addRoleToMember(member, role).queue(
             success -> logger.info("Successfully added member to role."),
             error -> logger.error("Error adding member to role.", error)
         );
      } catch (Exception e) {
         logger.warn(String.format("[%s] Role not found: %s", guild, role));
         logger.error("Role is no longer available", e);
      }
   }

   public static void removeAllUserRoles(Guild guild) {
      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild);
      Collection<String> roleNames = gi.getGameGroupMappings().values();
      Collection<Role> roles = new ArrayList<>();
      /**
       * List of all members across the guild who currently
       * belong to one of the GameGroup roles.
       */
      List<Member> members = guild.getMembers().stream().filter(
          member -> member.getRoles().stream().filter(
              role -> roleNames.contains(
                  role.getName())).collect(Collectors.toList())
              .size() > 0)
          .collect(Collectors.toList());

      for (String roleName : roleNames) {
         roles.add(guild.getRolesByName(roleName, false).get(0));
      }

      if (!roles.isEmpty()) {
         List<String> removedRoleNames = new ArrayList<>();
         roles.forEach(role -> removedRoleNames.add(role.getName()));
         for (Member member : members) {
            roles.forEach(r -> guild.removeRoleFromMember(member, r).queue());
            logger.info(String.format("[%s] Removed %s from the following roles: %s", guild, member
                .getEffectiveName(), roles.toString()));
         }
      }

   }
   /**
    * Scans a mapping of game display names (key) and role names to determine if a {@link Activity}
    * has a valid GameGroup mapping.
    *
    * @param gameMap a complete {@link HashMap} of game display names to role names for a given
    * {@link Guild}.
    * @param gameName the name of the {@link Activity}.
    * @return {@code true} if the {@code Game} name is in the list of mapping values, otherwise
    * {@code false}.
    */
   public static boolean isValidGame(HashMap<String, String> gameMap, String gameName) {
      return gameMap.get(gameName) != null;
   }

   /**
    * Returns the {@link Role} mapped to a valid GameGroup {@link Activity} if a valid mapping exists.
    *
    * @param guild the {@link Guild} containing GameGroup Roles.
    * @param game the {@link Activity} being scanned for a GameGroup Role mapping.
    * @return the GameGroup {@link Role} mapped to the {@link Activity} or {@code null}
    */
   public static Role getGameRole(Guild guild, Activity game) {
      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild.getIdLong());

      if (game == null || game.getName().isEmpty()) {
         return null;
      } else if (isValidGame(gi.getGameGroupMappings(), game.getName())) {
         return guild.getRolesByName(gi.getGameGroupMappings().get(game.getName()), false).get(0);
      } else {
         return null;
      }
   }

   public static void refreshGameGroups(Guild guild) {
      GuildInfo guildInfo = GuildInfoUtil.getGuildInfo(guild);
      List<Member> guildMembers = guild.getMembers();
      Collection<String> groupedRoleNames = guildInfo.getGameGroupMappings().values();
      Collection<Role> groupedRoles = groupedRoleNames.stream().map(name -> guild.getRolesByName
          (name, false).get(0)).collect(Collectors.toList());

      if (guildInfo.isGroupingGames()) { //Add gamegroup role to appropriate members.
         //Add gamegroup roles to applicable members.

         for (Member member : guildMembers) {
            /*
               For each member, we want to filter down a list of their activities which match any
                of the grouped role names. From that list, we want to add the member to the
                top-displayed activity/game role.
             */
            List<Activity> memberActivities = member.getActivities();
            memberActivities = memberActivities.stream().filter(ma -> groupedRoleNames.contains
                (ma.getName())).collect(Collectors.toList());

            if (memberActivities.size() > 0) {
               Activity topActivity = memberActivities.get(0);

               if (GameGroupUtil
                   .isValidGame(guildInfo.getGameGroupMappings(), topActivity.getName())) {
                  Role ggRole = GameGroupUtil.getGameRole(guild, topActivity);

                  if (!member.getRoles().contains(ggRole)) {
                     guild.addRoleToMember(member, ggRole).queue();

                     logger.info(String.format("[%s] GameGroups: Added %s to %s.", guild, member
                         .getEffectiveName(), ggRole.getName()));
                  }
               }
            } else {
               //If member has no activities, ensure they're removed from any existing group roles.
               guild.modifyMemberRoles(member, Collections.EMPTY_SET, groupedRoles).queue();

               logger.debug(String.format("[%s] GameGroups: Removed %s from all grouped roles.",
                   guild, member.getEffectiveName()));
            }
         }
      } else {
         //Remove all gamegroup roles from all members.
         List<Member> rolledMembers = new ArrayList<>();
         for (Role groupedRole : groupedRoles) {
            for (Member member : guildMembers) {
               if (member.getRoles().contains(groupedRole)) {
                  rolledMembers.add(member);
               }
            }
         }
         rolledMembers.forEach(m ->
             guild.modifyMemberRoles(m, Collections.EMPTY_SET, groupedRoles).queue());

         logger.info(String.format("[%s] GameGroups: Disables for guild. Removing all members "
             + "from grouped roles.", guild));
      }
   }

   public static void refreshMemberGameGroups(Member member) {
      Guild guild = member.getGuild();
      GuildInfo guildInfo = GuildInfoUtil.getGuildInfo(guild);
      List<Member> guildMembers = guild.getMembers();
      Collection<String> groupedRoleNames = guildInfo.getGameGroupMappings().values();
      Collection<Role> groupedRoles = groupedRoleNames.stream().map(name -> guild.getRolesByName
          (name, false).get(0)).collect(Collectors.toList());

      /*
         We want to filter down a list of the member's activities which match any
          of the grouped role names. From that list, we want to add the member to the
          top-displayed activity/game role.
       */
      List<Activity> memberActivities = member.getActivities();
      memberActivities = memberActivities.stream().filter(ma -> groupedRoleNames.contains
          (ma.getName())).collect(Collectors.toList());

      if (memberActivities.size() > 0) {
         Activity topActivity = memberActivities.get(0);

         if (GameGroupUtil
             .isValidGame(guildInfo.getGameGroupMappings(), topActivity.getName())) {
            Role ggRole = GameGroupUtil.getGameRole(guild, topActivity);

            if (!member.getRoles().contains(ggRole)) {
               guild.addRoleToMember(member, ggRole).queue();

               logger.info(String.format("[%s] GameGroups: Added %s to %s.", guild, member
                   .getEffectiveName(), ggRole.getName()));
            }
         }
      } else {
         //If member has no activities, ensure they're removed from any existing group roles.
         Collection<Role> memberGroupedRoles = member.getRoles().stream()
             .filter(mr -> groupedRoles.contains(mr))
             .collect(Collectors.toList());

         guild.modifyMemberRoles(member, Collections.EMPTY_SET, memberGroupedRoles).queue();

         memberGroupedRoles.forEach(r -> logger.info(String.format("[%s] GameGroups: Removed %s "
                 + "from %s.",
             guild, member.getEffectiveName(), r.getName())));

//         logger.debug(String.format("[%s] GameGroups: Removed %s from all grouped roles.",
//             guild, member.getEffectiveName()));
      }
   }
}
