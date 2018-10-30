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
import java.util.HashMap;
import java.util.List;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.managers.GuildController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupGamesCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);
   private EmbedBuilder eb;

   public GroupGamesCommand(){
      this.name = "groupgames";
      this.aliases = new String[]{"groupbygame", "gamegroups", "gamegroupings"};
      this.arguments = "<toggle/status/enable/disable>";
      this.category = new Category("Functionality");
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
      String[] args = event.getMessage().getContentDisplay().split("\\s", 2);
      GuildInfo gi = GuildInfoUtil.getGuildInfo(event.getGuild().getIdLong());

      eb = new EmbedBuilder()
          .setTitle("Game Grouping")
          .setFooter("Provided to you by IdiotBot. The most idiotic of bots.",
              "http://www.foundinaction.com/wp-content/uploads/2018/08/"
                  + "Neon_600x600_Transparent.png");

      switch (args[1]) {
         case "info":
         case "status":
            printInfo(event);
            return;
         case "toggle":
            toggle(event, !gi.isGroupingGames());
            return;
         case "enable":
            toggle(event, true);
            return;
         case "disable":
            toggle(event, false);
            return;
         default:
            //INVALID ARGS
            return;
      }
   }

   private void toggle(CommandEvent event, Boolean enable) {
      GuildController gc = event.getGuild().getController();
      GuildInfo gi = GuildInfoUtil.getGuildInfo(event.getGuild().getIdLong());
      List<Member> members = event.getGuild().getMembers();
      Collection<String> roleNames = gi.getGameGroupMappings().values();
      Collection<Role> roles = new ArrayList<>();

      for (String roleName : roleNames) {
         roles.add(event.getGuild().getRolesByName(roleName, false).get(0));
      }

      //Handle
      if (!enable) {
         //Disable
         GuildInfoUtil.disableGameGrouping(event.getGuild().getIdLong());

         //Remove from roles.
         for (Member member : members) {
            gc.removeRolesFromMember(member, roles).queue();
         }

         //Log
         logger.info(String.format("[%s] Game Grouping Disabled by %s.",
             event.getGuild(),
             event.getAuthor().getName()));

         //Prepare message.
         eb.setColor(Color.RED)
             .addField("Status", "INACTIVE", false);

      } else {
         //Enable
         GuildInfoUtil.enableGameGrouping(event.getGuild().getIdLong());

         //Add to roles.
         for (Member member : members) {
            if (member.getGame() != null) {
               GameGroupUtil.handleRoleAssignment(event, gi.getGameGroupMappings(),
                   member.getGame().getName(), member);
            }
         }

         //Log
         logger.info(String.format("[%s] Game Grouping Enabled by %s.",
             event.getGuild(),
             event.getAuthor().getName()));

         //Prepare message.
         eb.setColor(Color.GREEN)
             .addField("Status", "ACTIVE", false);
      }

      HashMap<String, String> mappings = gi.getGameGroupMappings();
      eb.addBlankField(false);
      HashMap<String, String> gameList = new HashMap<>();

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
         eb.addField(role, gameList.get(role), false);
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
}
