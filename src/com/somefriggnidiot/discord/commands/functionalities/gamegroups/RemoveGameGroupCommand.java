package com.somefriggnidiot.discord.commands.functionalities.gamegroups;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import net.dv8tion.jda.core.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveGameGroupCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   public RemoveGameGroupCommand() {
      this.name = "removegamegroup";
      this.aliases = new String[]{"rgg"};
      this.arguments = "<gameName>";
      this.category = new Category("Game Groups");
      this.requiredRole = "Staff";
      this.cooldown = 5;
      this.cooldownScope = CooldownScope.GUILD;
      this.help = "Removes the specified game from game groups.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE, Permission.MESSAGE_READ};
   }

   @Override
   protected void execute(CommandEvent event) {
      String cmd = event.getMessage().getContentDisplay().split("\\s", 2)[1];

      try {
         if (GuildInfoUtil.removeRoleMapping(event.getGuild().getIdLong(), cmd)) {
            event.reply(String.format("Game group removed.\nUsers playing \"%s\" will no longer be "
                    + "added to a role.",
                cmd));
         } else {
            event.reply("That game is currently not mapped to a gamegroup.");
         }
      } catch (Exception e) {
         logger.error(String.format("[%s] Error removing game role mapping for GameName: \"%s\"",
             event.getGuild(),
             cmd));
      }
   }
}
