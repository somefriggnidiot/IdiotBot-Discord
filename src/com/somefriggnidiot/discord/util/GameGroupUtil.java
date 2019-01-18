package com.somefriggnidiot.discord.util;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import java.util.HashMap;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
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
    * @param event this will eventually be replaced with a direct Guild object.
    * @param gameMap the complete {@link HashMap} mapping {@code Game} display names to
    * {@code Role}s for a {@code Guild}.
    * @param gameName this will eventually just be pulled from the Member.
    * @param member the {@link Member} playing a {@code Game}.
    */
   //TODO: Change Event and usages to Guild. Change gameName to just read from Member.
   public static void handleRoleAssignment(CommandEvent event, HashMap<String, String> gameMap,
       String gameName, Member member) {
      if (!isValidGame(gameMap, gameName)) {
         return;
      }
      String groupName = gameMap.get(gameName);

      // Get role
      Role role = null;
      try {
         role = event.getGuild().getRolesByName(groupName, false).get(0);
      } catch (IndexOutOfBoundsException e) {
         logger.debug(String.format("%s does not have a role matching %s", event.getGuild()
             .getName(), groupName));
      }
      if (role == null) {
         logger.warn(String.format("[%s] Role not found for \"%s\"", event.getGuild(), groupName));
         return;
      }

      // Do the actual role shit.
      logger.info(String.format("[%s] %s has started playing %s.",
          event.getGuild(),
          member.getEffectiveName(),
          member.getGame().getName()));

      try {
         event.getGuild().getController().addSingleRoleToMember(member, role)
             .queue();
      } catch (Exception e) {
         logger.warn(String.format("[%s] Role not found: %s", event.getGuild(), role));
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
}
