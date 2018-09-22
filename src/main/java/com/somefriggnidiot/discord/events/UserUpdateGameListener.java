package com.somefriggnidiot.discord.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.user.update.UserUpdateGameEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserUpdateGameListener extends ListenerAdapter {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);
   /**
    * Key = Game name displayed in Discord;
    * Value = Group Name
    */
   private final HashMap<String, String> groupMappings = new HashMap<String, String>();

   private class GameName {
      private String groupName;
      private ArrayList<String> gameNames;

      GameName(String groupName, String ... gameName) {
         this.groupName = groupName;
         this.gameNames = new ArrayList<String>();
         for (String alias : gameName) {
            gameNames.add(alias);
         }
      }

      public List<String> getAliases() {
         return gameNames;
      }

      public String getGroupName() {
         return groupName;
      }
   }

   @Override
   public void onUserUpdateGame(UserUpdateGameEvent event) {
      groupMappings.put("Rocket League", "Rocket League");
      groupMappings.put("Overwatch", "Overwatch");
      groupMappings.put("Counter-Strike Global Offensive", "CSGO");
      groupMappings.put("World of Warships", "World of Warships");
      groupMappings.put("Planetside 2", "Planetside 2");
      groupMappings.put("Rainbow Six Siege", "R6 Siege");
      groupMappings.put("Tom Clancy's Rainbow Six Siege", "R6 Siege");
      groupMappings.put("Terraria", "Terraria");
      groupMappings.put("Starbound", "Starboud");

      try {
         if (event.getNewGame() != null && event.getOldGame() != null) {
            // started playing from another game, ignore updates to same game
            if (event.getNewGame().getName().equalsIgnoreCase(event.getOldGame().getName())) return;

            handleSwap(event, event.getOldGame().getName(), event.getNewGame().getName());
         } else if (event.getNewGame() != null && event.getOldGame() == null) {
            // started playing from nothing
            handleRoleAssignment(event, event.getNewGame().getName(), true);
         } else {
            // done gaming
            handleRoleAssignment(event, event.getOldGame().getName(), false);
         }
      } catch (IllegalArgumentException e) {
         logger.error(String.format("Excption thrown with %s on %s", event.getUser(), event.getGuild()));
      }
   }

   private boolean isValidGame(String gameName) {
      return groupMappings.get(gameName) != null;
   }

   private void handleRoleAssignment(UserUpdateGameEvent event, String gameName, Boolean assign) {
      String groupName = null;
      Boolean isValid = false;
      for (String game : groupMappings.keySet()) {
         isValid = game.equalsIgnoreCase(gameName);
         if (isValid) {
           groupName = groupMappings.get(gameName);
           break;
         }
      }
      if (!isValid) return;

      // Get role
      Role role = null;
      try {
         role = event.getGuild().getRolesByName(groupName, false).get(0);
      } catch (IndexOutOfBoundsException e) {
         logger.debug(String.format("%s does not have a role matching %s", event.getGuild()
             .getName(), groupName));
      }
      if (role == null) {
         logger.warn(String.format("Role not found for %s in %s", groupName, event.getGuild().getName()));
         return;
      }

      // Do the actual role shit.
      if (assign) {
         logger.info(String.format("%s has started playing %s in %s.",
             event.getMember().getEffectiveName(),
             event.getNewGame().getName(),
             event.getGuild().getName()));

         try {
            event.getGuild().getController().addSingleRoleToMember(event.getMember(), role)
                .queue();
         } catch (Exception e) {
            logger.warn("Role not found.");
         }
      } else {
         logger.info(String.format("%s has stopped playing %s in %s.",
             event.getMember().getEffectiveName(),
             event.getOldGame().getName(),
             event.getGuild().getName()));

         try {
            event.getGuild().getController().removeSingleRoleFromMember(event.getMember(), role)
                .queue();
         } catch (Exception e) {
            logger.warn(String.format("[%s] Role not found.", event.getGuild().getId()));
         }
      }
   }

   private void handleSwap(UserUpdateGameEvent event, String oldGameName, String newGameName) {
      if (!isValidGame(oldGameName) && !isValidGame(newGameName)) return;

      //Check for old role.
      Role oldRole = null;
      try {
         oldRole = event.getGuild().getRolesByName(oldGameName, false).get(0);
      } catch (IndexOutOfBoundsException e) {
         logger.debug(String.format("%s does not have a role matching %s", event
             .getGuild().getName(), oldGameName));
      }
      if (oldRole == null) {
         logger.warn(String.format("Role not found for %s in %s", oldGameName,
             event.getGuild().getName()));
         return;
      } else {
         //Log it
         logger.info(String.format("%s has stopped playing %s in %s.",
             event.getMember().getEffectiveName(),
             event.getOldGame().getName(),
             event.getGuild().getName()));

         //Revoke role
         try {
            event.getGuild().getController().removeSingleRoleFromMember(event.getMember(), oldRole)
                .queue();
         } catch (Exception e) {
            logger.warn(String.format("[%s] Role not found.", event.getGuild().getId()));
         }
      }

      Role newRole = null;
      try {
         newRole = event.getGuild().getRolesByName(newGameName, false).get(0);
      } catch (IndexOutOfBoundsException e) {
         logger.debug(String.format("%s does not have a role matching %s", event
             .getGuild().getName(), newGameName));
      }
      if (newRole == null) {
         logger.warn(String.format("Role not found for %s in %s", newGameName,
             event.getGuild().getName()));
         return;
      } else {
         //Log it
         logger.info(String.format("%s has started playing %s in %s.",
             event.getMember().getEffectiveName(),
             event.getNewGame().getName(),
             event.getGuild().getName()));

         //Assign role
         try {
            event.getGuild().getController().addSingleRoleToMember(event.getMember(), newRole)
                .queue();
         } catch (Exception e) {
            logger.warn("Role not found.");
         }
      }
   }
}
