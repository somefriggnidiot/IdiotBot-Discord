package com.somefriggnidiot.discord.util;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.events.MessageListener;
import java.util.HashMap;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameGroupUtil {

   private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   public static void handleRoleAssignment(CommandEvent event, HashMap<String, String> gameMap,
       String gameName, Member member) {
      if (!isValidGame(gameMap, gameName)) return;
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

   private static boolean isValidGame(HashMap<String, String> gameMap, String gameName) {
      return gameMap.get(gameName) != null;
   }
}
