package com.somefriggnidiot.discord.util;

import static java.lang.String.format;

import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
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
   private static HashMap<Guild, GameGroupUtil> guildGameGroups  = new HashMap<>();
   private final Guild guild;
   private HashMap<String, Role> activeAutoGroups = new HashMap<>();
   private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

   GameGroupUtil(Guild guild) {
      this.guild = guild;
      GameGroupUtil.guildGameGroups.put(guild, this);
   }

   public static GameGroupUtil getGameGroupUtil(Guild guild) {
      return guildGameGroups.containsKey(guild) ? guildGameGroups.get(guild) :
          new GameGroupUtil(guild);
   }
   /*
      1. Update list of active auto-groups.
      2. Remove any game from the list with less than 2 players in server.
      3. Scan through those online to add to active auto-groups.
    */

   public void startAutoGrouping() {
      logger.info(format("[%s] Starting auto groups.", guild));
      GuildInfoUtil.setGroupMappingsAutomatic(guild, true);
      activeAutoGroups.clear();
      activeAutoGroups.putAll(detectNewAutoGroups());
      refreshAutoGroupAssignments();

      executorService.scheduleAtFixedRate(this::updateAutoGrouping, 1, 1,
          TimeUnit.MINUTES);
   }

   private void updateAutoGrouping() {
      logger.debug(format("[%s] Updating auto groups.", guild));
      activeAutoGroups.putAll(detectNewAutoGroups());
      pruneAutoGroups();
      refreshAutoGroupAssignments();
   }

   public void stopAutoGrouping() {
      executorService.shutdown();

      GuildInfoUtil.setGroupMappingsAutomatic(guild, false);


      Collection<Role> oldRoles = activeAutoGroups.values();
      activeAutoGroups.clear();

      for (Member member : guild.getMembers()) {
         guild.modifyMemberRoles(member, Collections.emptyList(), oldRoles)
             .queue();
      }
   }

   public HashMap<String, Role> detectNewAutoGroups() {
      //Get list of every game being played by multiple people.
      List<Activity> allActivities = new ArrayList<>();
      for (Member member : guild.getMembers()) {
         List<Activity> memberGames = member.getActivities().stream().filter(activity -> activity
             .getType().equals(ActivityType.DEFAULT)).collect(Collectors.toList());

         allActivities.addAll(memberGames);
      }
      logger.debug(format("[%s] Discovered %s activities.", guild, allActivities.size()));

      HashMap<String, Role> newAutoGroups = new HashMap<>();
      List<String> eligibleActivities = filterDuplicateActivities(allActivities);
      for (String activity : eligibleActivities) {
         //Only return activities not already active.
         if (!activeAutoGroups.containsKey(activity)) {
            //Create role to associate with game.
            Role newGroupRole = createOrFindGameGroup(activity);
            newAutoGroups.put(activity, newGroupRole);
            logger.info(format("[%s] Adding activity to auto groups: %s", guild, activity));
         }
      }

      return newAutoGroups;
   }

   public void pruneAutoGroups() {
      for (Role activityRole : activeAutoGroups.values()) {
         List<Member> players = guild.getMembersWithRoles(activityRole);
         if (players.size() < 2) {
            logger.info(format("[%s] Removing activity from auto groups: %s",
                guild, activityRole.getName()));
            activeAutoGroups.remove(activityRole.getName());
            activityRole.delete().queue();
         }
      }
   }

   public void refreshAutoGroupAssignments() {
      GuildInfo guildInfo = GuildInfoUtil.getGuildInfo(guild);
      List<Member> guildMembers = guild.getMembers();

      logger.debug(format("[%s] Refreshing all auto group assignments.", guild));

      if (guildInfo.isGroupingGames() && guildInfo.gameGroupsAutomatic()) {
         guildMembers.forEach(this::refreshMemberAutoGroups);
      }
   }

   public void refreshMemberAutoGroups(Member member) {
      List<Role> addRoles = new ArrayList<>();
      List<Role> deleteRoles = new ArrayList<>(activeAutoGroups.values());
      logger.debug(format("[%s] Refreshing auto group roles for %s.",
          guild, member.getEffectiveName()));

      for (Role memberActivityRole : getMemberActivityRoles(member)) {
         addRoles.add(memberActivityRole);
         deleteRoles.remove(memberActivityRole);
      }

      List<Role> memberRoles = member.getRoles();
      if (deleteRoles.size() > 0) {
         deleteRoles.forEach(role -> {
            if (memberRoles.contains(role)) {
               logger.info(format("[%s] Removing %s from role: %s",
                   guild, member.getEffectiveName(), role.getName()));
            }
         });
      }

      if (addRoles.size() > 0) {
         addRoles.forEach(role -> {
            if (!memberRoles.contains(role)) {
               logger.info(format("[%s] Adding %s to role: %s",
                   guild, member.getEffectiveName(), role.getName()));
            }
         });
      }

      guild.modifyMemberRoles(member, addRoles, deleteRoles).queue();
   }

   private List<Role> getMemberActivityRoles(Member member) {
      List<Role> memberActivityRoles = new ArrayList<>();
      for (Activity activity : member.getActivities()) {
         if (activeAutoGroups.containsKey(activity.getName())) {
            memberActivityRoles.add(activeAutoGroups.get(activity.getName()));
         }
      }

      logger.debug(format("[%s] %s is active in %s roles: %s",
          guild, member.getEffectiveName(), memberActivityRoles.size(), memberActivityRoles.toArray()));

      return memberActivityRoles;
   }

   private Role createOrFindGameGroup(String gameName) {
      List<Role> gameRoles = guild.getRolesByName(gameName, true);
      if (gameRoles.size() > 0) {
         return gameRoles.get(0);
      }

      RoleAction role = guild.createRole();
      role.setName(gameName)
          .setMentionable(false)
          .setHoisted(true)
          .setPermissions(Collections.EMPTY_SET)
          .queue();

      logger.info(format("[%s] Created auto role for %s", guild, gameName));

      try {
         Thread.sleep(500);
         Role createdRole = guild.getRolesByName(gameName, true).get(0);
         Role autoGroup = guild.getRolesByName("-- Auto Grouping --", true).get(0);

         guild.modifyRolePositions(false)
             .selectPosition(createdRole.getPosition())
             .moveTo(autoGroup.getPosition() + 1)
             .queue();
      } catch (InterruptedException e) {
         logger.error("Sleep interrupted during GameGroupsCommand create.");
      } catch (IndexOutOfBoundsException iob) {
         guild.getTextChannelById(GuildInfoUtil.getGuildInfo(guild).getBotTextChannelId())
             .sendMessage("Game Groups configuration has been set to AUTO, but no `-- Auto "
                 + "Grouping --` role has been created. Without this role, automatically-created "
                 + "game groups will not be able to be moved up the role list to their proper "
                 + "spot.").queue();
      }

      return guild.getRolesByName(gameName, true).get(0);
   }

   private List<String> filterDuplicateActivities(List<Activity> allActivities) {
      Set<String> checkerSet = new HashSet<>();
      List<String> eligibleActivities = new ArrayList<>();

      for (Activity activity : allActivities) {
         if (!checkerSet.add(activity.getName())) {
            eligibleActivities.add(activity.getName());
         }
      }

      eligibleActivities = eligibleActivities.stream().distinct().collect(Collectors.toList());

      logger.debug(format("[%s] Found %s eligible activities.",
          guild, eligibleActivities.size()));

      return eligibleActivities;
   }

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
