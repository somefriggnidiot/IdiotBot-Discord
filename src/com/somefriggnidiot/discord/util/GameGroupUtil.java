package com.somefriggnidiot.discord.util;

import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.managers.GuildManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various helper functions for GameGroups. <br />
 * GameGrouping is a concept of assigning a Discord {@link Role} to a
 * {@link net.dv8tion.jda.core.entities.User} temporarily while that {@code User}
 * is playing a certain {@link Game} as defined by {@code User}-defined rules.
 *
 * @see com.somefriggnidiot.discord.commands.functionalities.gamegroups.AddGameGroupCommand
 * @see com.somefriggnidiot.discord.commands.functionalities.gamegroups.RemoveGameGroupCommand
 * @see com.somefriggnidiot.discord.commands.functionalities.gamegroups.GroupGamesCommand
 */
public class GameGroupUtil {

   private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   /**
    * Assigns the GameGroup {@link Role} to a {@link net.dv8tion.jda.core.entities.User} playing
    * a {@link Game} that has a valid GameGroup mapping in the {@link Guild}.
    *
    * @param guild the guild in which these actions are taking place.
    * @param gameMap the complete {@link HashMap} mapping {@code Game} display names to
    * {@code Role}s for a {@code Guild}.
    * @param gameName this will eventually just be pulled from the Member.
    * @param member the {@link Member} playing a {@code Game}.
    */
   public static void handleRoleAssignment(Guild guild, HashMap<String, String> gameMap,
       String gameName, Member member) {
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
          member.getGame().getName()));

      try {
         guild.getController().addSingleRoleToMember(member, role).queue();
      } catch (Exception e) {
         logger.warn(String.format("[%s] Role not found: %s", guild, role));
         logger.error("Role is no longer available", e);
      }
   }

   /**
    * Scans a mapping of game display names (key) and role names to determine if a {@link Game}
    * has a valid GameGroup mapping.
    *
    * @param gameMap a complete {@link HashMap} of game display names to role names for a given
    * {@link Guild}.
    * @param gameName the name of the {@link Game}.
    * @return {@code true} if the {@code Game} name is in the list of mapping values, otherwise
    * {@code false}.
    */
   public static boolean isValidGame(HashMap<String, String> gameMap, String gameName) {
      return gameMap.get(gameName) != null;
   }

   /**
    * Returns the {@link Role} mapped to a valid GameGroup {@link Game} if a valid mapping exists.
    *
    * @param guild the {@link Guild} containing GameGroup Roles.
    * @param game the {@link Game} being scanned for a GameGroup Role mapping.
    * @return the GameGroup {@link Role} mapped to the {@link Game} or {@code null}
    */
   public static Role getGameRole(Guild guild, Game game) {
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
      GuildController gc = guild.getController();
      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild.getIdLong());
      List<Member> members = guild.getMembers();
      Collection<String> roleNames = gi.getGameGroupMappings().values();
      Collection<Role> roles = new ArrayList<>();

      for (String roleName : roleNames) {
         roles.add(guild.getRolesByName(roleName, false).get(0));
      }

      if (gi.isGroupingGames()) { //Only do if guild is grouping.
         for (Member member : members) { //For each
            // member
            logger.trace(String.format("[%s] Member: %s\nGame: %s",
                guild,
                member.toString(),
                member.getGame()));
            Game game = member.getGame();

            if (game == null || game.getName().isEmpty()) { //If not playing game
               try {
                  gc.removeRolesFromMember(member, roles).queue(); //Remove game roles.
               } catch (HierarchyException e) {
                  logger.error(String.format("[%s] Unable to modify role above your station.",
                      guild), e);
                  GuildManager gm = new GuildManager(guild);
                  MessageChannel channel = gm.getGuild()
                      .getTextChannelsByName("general", true).get(0);
                  channel.sendMessage(String.format("Error! Cannot apply Game Groups to %s "
                          + "as they are higher in the hierarchy than me!",
                      member.getEffectiveName())).queue();

                  return;
               }
               logger.debug(String.format("[%s] %s is not playing any game, removing all game "
                       + "roles.",
                   guild,
                   member.getEffectiveName()));
            } else { //Playing game

               //So check if they have a game role
               List<Role> memberRoles = member.getRoles();

               Boolean next = false;

               for (Role role : memberRoles) {
                  if (roles.contains(role)) { //User's role is a game role.
                     if (GameGroupUtil.getGameRole(guild, game) == role) { //User has correct role.
                        logger.info(String.format("[%s] %s already has the correct role.",
                            guild,
                            member.getEffectiveName()));
                     } else {
                        if (GameGroupUtil.isValidGame(gi.getGameGroupMappings(), game.getName())) {
                           gc.addSingleRoleToMember(member, GameGroupUtil.getGameRole(guild, game))
                               .queue();
                           logger
                               .info(String.format("[%s] Removing %s from existing game roles and "
                                       + "adding to %s.",
                                   guild,
                                   member.getEffectiveName(),
                                   GameGroupUtil.getGameRole(guild, game)));
                        } else {
                           gc.removeRolesFromMember(member, roles).queue();
                           logger.info(String.format("[%s] Removing %s from existing game roles.",
                               guild,
                               member.getEffectiveName()));
                        }
                     }
                     next = true;
                  }
               }

               if (!next) {
                  GameGroupUtil
                      .handleRoleAssignment(guild, gi.getGameGroupMappings(), game.getName(),
                          member);
               }
            }
         }
      } else {
         //Report that it can't be done.
         logger.warn(String.format("[%s] Attempted to refresh gamegroups but guild does not have "
                 + "grouping enabled. Removing all game roles from members.",
             guild));
         for (Member member : members) {
            gc.removeRolesFromMember(member, roles);
         }
      }
   }
}
