package com.somefriggnidiot.discord.commands.functionalities.xp.rolelevels;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddRoleLevelCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(AddRoleLevelCommand.class);

   public AddRoleLevelCommand() {
      this.name = "addrolelevel";
      this.aliases = new String[]{};
      this.arguments = "<level> <role>";
      this.category = new Category("Functionality");
      this.help = "Adds a role to be granted to a member when they reach the designated level.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_WRITE,
      Permission.MANAGE_ROLES};
      this.guildOnly = true;
      this.requiredRole = "Staff";
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      String[] args = event.getMessage().getContentDisplay().split("\\s", 3);
      Role role = event.getMessage().getMentionedRoles().get(0);
      GuildInfoUtil giu = new GuildInfoUtil(event.getGuild().getIdLong());

      giu.addRoleLevelMapping(role.getIdLong(), Integer.valueOf(args[1]));

      event.reply(String.format("Added role \"%s\" to be granted at level %s.",
          role.getName(),
          args[1]));

      logger.info(String.format("[%s] Added role \"%s\" to be granted at level %s.",
          event.getGuild(),
          role.getName(),
          args[1]));
   }
}
