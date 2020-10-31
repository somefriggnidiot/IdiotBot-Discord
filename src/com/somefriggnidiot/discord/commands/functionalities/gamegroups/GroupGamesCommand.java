package com.somefriggnidiot.discord.commands.functionalities.gamegroups;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import com.somefriggnidiot.discord.util.GameGroupUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.managers.GuildManager;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
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
                  update(event);
               } catch (Exception e) {
                  event.reply("Cannot modify roles of a higher rank.");
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
      GuildController gc = guild.getController();
      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild);
      List<Member> members = guild.getMembers();
      Collection<String> roleNames = gi.getGameGroupMappings().values();
      Collection<Role> roles = new ArrayList<>();

      for (String roleName : roleNames) {
         roles.add(guild.getRolesByName(roleName, false).get(0));
      }

      //Handle
      if (!enable) {
         //Disable
         GuildInfoUtil.disableGameGrouping(guild);

         //Remove from roles.
         for (Member member : members) {
            gc.removeRolesFromMember(member, roles).queue();
         }

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

         //Add to roles.
         for (Member member : members) {
            if (member.getGame() != null) {
               GameGroupUtil.handleRoleAssignment(guild, gi.getGameGroupMappings(),
                   member.getGame().getName(), member);
            }
         }

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

   private void update(CommandEvent event) {
      Guild guild = event.getGuild();
      GuildController gc = guild.getController();
      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild);
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

            if(game == null || game.getName().isEmpty()) { //If not playing game
               gc.removeRolesFromMember(member, roles).queue(); //Remove game roles.
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
                           logger.info(String.format("[%s] Removing %s from existing game roles and "
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
                  GameGroupUtil.handleRoleAssignment(guild, gi.getGameGroupMappings(),
                      game.getName(), member);
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

   private void create(CommandEvent event) {
      String gameName = event.getMessage().getContentDisplay().split("\\s", 3)[2];

      //Create group for game.
      Guild guild = event.getGuild();
      RoleAction role = guild.getController().createRole();
      role.setName(gameName)
          .setMentionable(false)
          .setHoisted(true)
          .setPermissions(Collections.EMPTY_SET)
          .queue();

      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild);
      List<String> gameGroups = new ArrayList<>(gi.getGameGroupMappings().values());
      Role firstGameGroup = guild.getRolesByName(gameGroups.get(0), true).get(0);
      Integer lastGameGroupPosition = guild.getController().modifyRolePositions(false)
          .selectPosition(firstGameGroup).getSelectedPosition() + gameGroups.size();

      //Move group to bottom of grouped games list.
      try {
         Thread.sleep(500);
      } catch (InterruptedException e) {
         logger.error("Sleep interrupted during GameGroupsCommand create.");
      }
      guild.getController().modifyRolePositions(false)
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
