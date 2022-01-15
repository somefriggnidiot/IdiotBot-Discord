package com.somefriggnidiot.discord.util;

import static java.lang.String.format;

import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO Refactor GameGroups to create roles and auto-group for any game with 2+ players.

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

   public static void removeAllUserRoles(Guild guild) {
      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild);
      Collection<String> roleNames = gi.getGameGroupMappings().values();
      Collection<Role> roles = new ArrayList<>();
      /*
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
            logger.info(format("[%s] Removed %s from the following roles: %s", guild, member
                .getEffectiveName(), roles.toString()));
         }
      }
   }

   public static void refreshGameGroups(Guild guild) {
      GuildInfo guildInfo = GuildInfoUtil.getGuildInfo(guild);
      List<Member> guildMembers = guild.getMembers();
      Collection<String> groupedRoleNames = guildInfo.getGameGroupMappings().values();
      Collection<Role> groupedRoles = groupedRoleNames.stream().map(name -> guild.getRolesByName
          (name, false).get(0)).collect(Collectors.toList());

      if (guildInfo.isGroupingGames()) {
         guildMembers.forEach(GameGroupUtil::refreshMemberGameGroups);
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

         logger.info(format("[%s] GameGroups: Disabled for guild. Removing all members "
             + "from grouped roles.", guild));
      }
   }

   public static void refreshMemberGameGroups(Member member) {
      Guild guild = member.getGuild();
      GuildInfo guildInfo = GuildInfoUtil.getGuildInfo(guild);
      Activity groupedActivity = getMemberGroupedActivity(member);

      if (groupedActivity != null) {
         //Member has eligible activities. Update accordingly.
         String activityName = groupedActivity.getName();
         String roleName = guildInfo.getGameGroupMappings().get(activityName);
         Role groupRole = guild.getRolesByName(roleName, false).get(0);

         if (!member.getRoles().contains(groupRole)) {
            guild.addRoleToMember(member, groupRole).queue();
            logger.debug(format("[%s] GameGroups: Added %s to %s.",
                guild, member.getEffectiveName(), groupRole.getName()));
         }
      } else {
         //Member does not have any valid activities.
         Collection<Role> groupedRoles = guildInfo.getGameGroupMappings().values()
             .stream().map(name -> guild.getRolesByName(name, false).get(0))
             .collect(Collectors.toList());

         Collection<Role> memberGroupedRoles = member.getRoles().stream()
             .filter(groupedRoles::contains)
             .collect(Collectors.toList());

         if (memberGroupedRoles.size() > 0) {
            guild.modifyMemberRoles(member, Collections.EMPTY_SET, memberGroupedRoles).queue();

            memberGroupedRoles.forEach(r -> logger.debug(
                format("[%s] GameGroups: Removed %s from %s.",
                guild, member.getEffectiveName(), r.getName())));
         }
      }
   }

   /**
    * Retrieves the highest-level activity matching an existing GameGroup role.
    *
    * @param member the member whose activity is being checked.
    * @return the highest-level activity whose name matches an existing GameGroup role, or {@code
    * null} if no current Activities for the member match.
    */
   private static Activity getMemberGroupedActivity(Member member) {
      List<Activity> activities = member.getActivities();
      Set<String> groupedRoleNames = GuildInfoUtil.getGuildInfo(member.getGuild())
          .getGameGroupMappings().keySet();

      for (Activity activity : activities) {
         if (groupedRoleNames.contains(activity.getName())) {
            return activity;
         }
      }

      return null;
   }
}
