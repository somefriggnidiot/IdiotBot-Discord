package com.somefriggnidiot.discord.commands.functionalities.gamegroups;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import net.dv8tion.jda.core.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddGameGroupCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   public AddGameGroupCommand() {
      this.name = "addgamegroup";
      this.aliases = new String[]{"agg"};
      this.arguments = "<gameName>|<roleName>";
      this.category = new Category("Functionality");
      this.requiredRole = "Staff";
      this.cooldown = 5;
      this.cooldownScope = CooldownScope.GUILD;
      this.help = "Adds a gamegroup to the bot. When Discord shows a user as playing a game that "
          + "has been added as a gamegroup, they will be automatically added to the provided role"
          + ". This can be used to \"group\" members in the member sidebar by the game they're "
          + "playing by creating a unique role for each game and setting that role to be "
          + "displayed separately.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE, Permission.MESSAGE_READ};
   }

   @Override
   protected void execute(CommandEvent event) {
      String cmd = event.getMessage().getContentDisplay().split("\\s", 2)[1];
      String[] args = cmd.split("\\|", 2);

      try {
         GuildInfoUtil.addGameRoleMapping(event.getGuild().getIdLong(), args[0], args[1]);
         event.reply(String.format("New game group created!\nUsers playing \"%s\" will now be "
                 + "added to the role \"%s\".",
             args[0],
             args[1]));
      } catch (Exception e) {
         logger.error(String.format("[%s] Error adding game role mapping: "
                 + "\nGameName: \"%s\"\nRoleName: \"%s\"",
             event.getGuild(),
             args[0],
             args[1]));
      }
   }
}
