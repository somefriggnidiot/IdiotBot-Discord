package com.somefriggnidiot.discord.commands.functionalities.xp.rolelevels;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.command_util.CommandUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveRoleLevelCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(RemoveRoleLevelCommand.class);

   public RemoveRoleLevelCommand() {
      this.name = "removerolelevel";
      this.aliases = new String[]{};
      this.arguments = "<role>";
      this.category = new Category("Role Levels");
      this.help = "Removes a role to be granted to a member when they reach the designated level.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_WRITE,
          Permission.MANAGE_ROLES};
      this.guildOnly = true;
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      GuildInfoUtil giu = new GuildInfoUtil(event.getGuild());
      if (!CommandUtil.checkPermissions(event.getMember(), giu.getStaffRole())) {
         event.reply("You do not have the necessary permissions to use this command.");
         return;
      }

      Role role = event.getMessage().getMentionedRoles().get(0);

      Integer level = giu.removeRoleLevelMapping(role.getIdLong());

      event.reply(String.format("Removed role \"%s\" from being granted at level %s.",
          role.getName(),
          level));

      logger.info(String.format("[%s] Removed role \"%s\" from being granted at level %s.",
          event.getGuild(),
          role.getName(),
          level));
   }
}
