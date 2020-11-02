package com.somefriggnidiot.discord.commands.functionalities.gamegroups;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import com.somefriggnidiot.discord.util.GameGroupUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupGamesCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);
   private EmbedBuilder eb;

   public GroupGamesCommand() {
      this.name = "gamegroups";
      this.aliases = new String[]{"groupbygame", "groupgames", "gamegroupings"};
      this.arguments = "<toggle/status/enable/disable/create> [create: gameName]";
      this.requiredRole = "Staff";
      this.category = new Category("Game Groups");
      this.help = "Toggles whether or not this guild groups mambers by what game they are playing"
          + ". Members are \"grouped\" by being added to the specified group when Discord shows "
          + "them as playing the specified game.";
      this.botPermissions = new Permission[]{Permission.MANAGE_ROLES, Permission.MESSAGE_READ,
          Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};
      this.guildOnly = true;
      this.cooldown = 5;
      this.cooldownScope = CooldownScope.GUILD;
   }

   @Override
   protected void execute(final CommandEvent event) {
      Guild guild = event.getGuild();
      String[] args = event.getMessage().getContentDisplay().split("\\s", 3);
      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild);

      eb = new EmbedBuilder()
          .setTitle("Game Grouping")
          .setFooter("Provided to you by IdiotBot. The most idiotic of bots.",
              "http://www.foundinaction.com/wp-content/uploads/2018/08/"
                  + "Neon_600x600_Transparent.png");

      try {
         switch (args[1]) {
            case "reset":
            case "refresh":
            case "update":
            case "check":
               try {
                  GameGroupUtil.refreshGameGroups(guild);
               } catch (Exception e) {
//                  event.reply("Cannot modify roles of a higher rank.");
                  logger.error(e.getMessage(), e);
               }
               return;
            case "toggle":
               toggle(event, !gi.isGroupingGames());
               return;
            case "enable":
            case "on":
            case "start":
               toggle(event, true);
               return;
            case "disable":
            case "off":
            case "stop":
               toggle(event, false);
               return;
            case "create":
               create(event);
               return;
            case "info":
            case "status":
            default:
               printInfo(event);
         }
      } catch (ArrayIndexOutOfBoundsException e) {
         printInfo(event);
      }
   }

   private void toggle(CommandEvent event, Boolean enable) {
      Guild guild = event.getGuild();
      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild);

      //Handle
      if (!enable) {
         //Disable
         GuildInfoUtil.disableGameGrouping(guild);
         GameGroupUtil.refreshGameGroups(guild);

         //Log
         logger.info(String.format("[%s] Game Grouping Disabled by %s.",
             guild,
             event.getAuthor().getName()));

         //Prepare message.
         eb.setColor(Color.RED)
             .addField("Status", "INACTIVE", false);

      } else {
         //Enable
         GuildInfoUtil.enableGameGrouping(guild);
         GameGroupUtil.refreshGameGroups(guild);

         //Log
         logger.info(String.format("[%s] Game Grouping Enabled by %s.",
             guild,
             event.getAuthor().getName()));

         //Prepare message.
         eb.setColor(Color.GREEN)
             .addField("Status", "ACTIVE", false);
      }

      HashMap<String, String> mappings = gi.getGameGroupMappings();
      eb.addBlankField(false);

      if (mappings == null || mappings.isEmpty()) {
         eb.addField("Active Game Groups", "There are currently no active game groups.", true);
      } else {
         HashMap<String, String> gameList = new HashMap<>();

         eb.addField("Active Game Groups", "", false);
         for (String mappingKey : mappings.keySet()) {
            String role = mappings.get(mappingKey);
            String games = gameList.get(role);
            if (games == null || games.isEmpty()) {
               games = mappingKey;
            } else {
               games += ", " + mappingKey;
            }
            gameList.put(role, games);
         }

         for (String role : gameList.keySet()) {
            eb.addField(role, gameList.get(role), true);
         }
      }

      //Send message.
      event.getChannel().sendMessage(eb.build()).queue();
   }

   private void printInfo(CommandEvent event) {
      GuildInfo gi = GuildInfoUtil.getGuildInfo(event.getGuild().getIdLong());

      //Handle
      if (!gi.isGroupingGames()) {
         //Prepare message.
         eb.setColor(Color.RED)
             .addField("Status", "INACTIVE", false)
             .addBlankField(false);
      } else {
         //Prepare message.
         eb.setColor(Color.GREEN)
             .addField("Status", "ACTIVE", false)
             .addBlankField(false);
      }

      HashMap<String, String> mappings = gi.getGameGroupMappings();
      if (mappings == null || mappings.isEmpty()) {
         eb.addField("Active Game Groups", "There are currently no active game groups.", true);
      } else {
         HashMap<String, String> gameList = new HashMap<>();

         eb.addField("Active Game Groups", "", false);
         for (String mappingKey : mappings.keySet()) {
            String role = mappings.get(mappingKey);
            String games = gameList.get(role);
            if (games == null || games.isEmpty()) {
               games = mappingKey;
            } else {
               games += ", " + mappingKey;
            }
            gameList.put(role, games);
         }

         for (String role : gameList.keySet()) {
            eb.addField(role, gameList.get(role), true);
         }
      }

      //Send message.
      event.getChannel().sendMessage(eb.build()).queue();
   }

   private void create(CommandEvent event) {
      String gameName = event.getMessage().getContentDisplay().split("\\s", 3)[2];

      //Create group for game.
      Guild guild = event.getGuild();
      RoleAction role = guild.createRole();
      role.setName(gameName)
          .setMentionable(false)
          .setHoisted(true)
          .setPermissions(Collections.EMPTY_SET)
          .queue();

      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild);
      List<String> gameGroups = new ArrayList<>(gi.getGameGroupMappings().values());
      Role firstGameGroup = guild.getRolesByName(gameGroups.get(0), true).get(0);
      Integer lastGameGroupPosition = guild.modifyRolePositions(false)
          .selectPosition(firstGameGroup).getSelectedPosition() + gameGroups.size();

      //Move group to bottom of grouped games list.
      try {
         Thread.sleep(500);
      } catch (InterruptedException e) {
         logger.error("Sleep interrupted during GameGroupsCommand create.");
      }
      guild.modifyRolePositions(false)
          .selectPosition(guild.getRolesByName(gameName, true).get(0))
          .moveTo(lastGameGroupPosition)
          .queue();

      //Add group as GameGroup.
      try {
         GuildInfoUtil.addGameRoleMapping(guild.getIdLong(), gameName, gameName);

         // Refresh to apply new game groups.
         if (GuildInfoUtil.getGuildInfo(guild.getIdLong()).isGroupingGames()) {
            GameGroupUtil.refreshGameGroups(guild);
         }

         event.reply(String.format("New game group created!\nUsers playing \"%s\" will now be "
                 + "added to the role \"%s\".",
             gameName,
             gameName));
      } catch (Exception e) {
         logger.error(String.format("[%s] Error adding game role mapping: "
                 + "\nGameName: \"%s\"\nRoleName: \"%s\"",
             event.getGuild(),
             gameName,
             gameName));
      }
   }
}
