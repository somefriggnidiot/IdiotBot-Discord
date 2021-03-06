package com.somefriggnidiot.discord.events;

import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import java.util.HashMap;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.user.update.UserUpdateGameEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserUpdateGameListener extends ListenerAdapter {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   @Override
   public void onUserUpdateGame(UserUpdateGameEvent event) {

      GuildInfo gi = GuildInfoUtil.getGuildInfo(event.getGuild().getIdLong());
      final HashMap<String, String> groupMappings = gi.getGameGroupMappings();

      if (gi.isGroupingGames()) {

         try {
            if (event.getNewGame() != null && event.getOldGame() != null) {
               // started playing from another game, ignore updates to same game
               if (event.getNewGame().getName().equalsIgnoreCase(event.getOldGame().getName())) {
                  return;
               }

               handleSwap(event, groupMappings, event.getOldGame().getName(), event.getNewGame()
                   .getName());
            } else if (event.getNewGame() != null && event.getOldGame() == null) {
               // started playing from nothing
               handleRoleAssignment(event, groupMappings, event.getNewGame().getName(), true);
            } else {
               // done gaming
               handleRoleAssignment(event, groupMappings, event.getOldGame().getName(), false);
            }
         } catch (IllegalArgumentException e) {
            logger.error(
                String.format("Excption thrown with %s on %s", event.getUser(), event.getGuild()));
            logger.error("Trace: ", e);
         }
      }
   }

   private boolean isValidGame(HashMap<String, String> gameMap, String gameName) {
      return gameMap.get(gameName) != null;
   }

   private void handleRoleAssignment(UserUpdateGameEvent event, HashMap<String, String> gameMap,
       String gameName, Boolean assign) {
      if (!isValidGame(gameMap, gameName)) {
         return;
      }
      String groupName = gameMap.get(gameName);

      // Get role
      Role role = null;
      try {
         role = event.getGuild().getRolesByName(groupName, false).get(0);

         if (role == null) {
            logger.warn(String.format("[%s] No GameGroup set for \"%s\"", event.getGuild(),
                groupName));
            return;
         }
      } catch (IndexOutOfBoundsException e) {
         logger.debug(String.format("%s does not have a role matching %s", event.getGuild()
             .getName(), groupName));
      }

      // Do the actual role shit.
      if (assign) {
         logger.info(String.format("[%s] %s has started playing %s.",
             event.getGuild(),
             event.getMember().getEffectiveName(),
             event.getNewGame().getName()));

         try {
            event.getGuild().getController().addSingleRoleToMember(event.getMember(), role)
                .queue();
         } catch (Exception e) {
            logger.warn(String.format("[%s] Role not found: %s", event.getGuild(), role));
         }
      } else {
         logger.info(String.format("[%s] %s has stopped playing %s.",
             event.getGuild(),
             event.getMember().getEffectiveName(),
             event.getOldGame().getName()));

         try {
            event.getGuild().getController().removeSingleRoleFromMember(event.getMember(), role)
                .queue();
         } catch (Exception e) {
            logger.warn(String.format("[%s] Role not found for \"%s\"", event.getGuild(), role));
         }
      }
   }

   private void handleSwap(UserUpdateGameEvent event, HashMap<String, String> gameMap,
       String oldGameName, String newGameName) {
      //If both old and new are invalid, skip.
      if (!isValidGame(gameMap, oldGameName) && !isValidGame(gameMap, newGameName)) {
         return;
      }

      //Check for old role.
      Role oldRole;
      try {
         oldRole = event.getGuild().getRolesByName(oldGameName, false).get(0);

         if (oldRole == null) {
            logger
                .warn(String.format("[%s] Role not found for previous game: \"%s\"",
                    event.getGuild(),
                    oldGameName));
            return;
         } else {
            //Log it
            logger.info(String.format("[%s] %s has stopped playing %s.",
                event.getGuild(),
                event.getMember().getEffectiveName(),
                event.getOldGame().getName()));

            //Revoke role
            try {
               event.getGuild().getController()
                   .removeSingleRoleFromMember(event.getMember(), oldRole)
                   .queue();
            } catch (Exception e) {
               logger.warn(String.format("[%s] Role has gone missing: %s",
                   event.getGuild(),
                   oldRole));
            }
         }
      } catch (IndexOutOfBoundsException e) {
         logger
             .debug(String.format("[%s] Previous game not present: \"%s\"",
                 event.getGuild(),
                 oldGameName));
      }

      Role newRole;
      try {
         newRole = event.getGuild().getRolesByName(newGameName, false).get(0);

         if (newRole == null) {
            logger
                .info(String.format("[%s] Game Group not found for new role: \"%s\"",
                    event.getGuild(),
                    newGameName));
         } else {
            //Log it
            logger.info(String.format("[%s] %s has started playing %s.",
                event.getGuild(),
                event.getMember().getEffectiveName(),
                event.getNewGame().getName()));

            //Assign role
            try {
               event.getGuild().getController().addSingleRoleToMember(event.getMember(), newRole)
                   .queue();
            } catch (Exception e) {
               logger.warn(String.format("[%s] Role has gone missing: %s", event
                   .getGuild(), newRole));
               logger.error(event.toString(), e);
            }
         }
      } catch (IndexOutOfBoundsException e) { //No role found for new game.
         logger
             .info(String.format("[%s] New game not present: \"%s\"",
                 event.getGuild(),
                 newGameName));
      }
   }
}
