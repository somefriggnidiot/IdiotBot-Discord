package com.somefriggnidiot.discord.commands.functionalities.xp.rolelevels;

import static java.lang.String.format;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.XpUtil;
import com.somefriggnidiot.discord.util.command_util.CommandUtil;
import net.dv8tion.jda.api.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefreshRoleLevelAssignmentsCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(RefreshRoleLevelAssignmentsCommand.class);

   public RefreshRoleLevelAssignmentsCommand() {
      this.name = "refreshrolelevelassignments";
      this.aliases = new String[]{"rrla", "refreshrolelevels"};
      this.arguments = "";
      this.category = new Category("Role Levels");
      this.help = "Triggers a recalculation of Role Level assignments for all members in the "
          + "guild.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_WRITE,
      Permission.MANAGE_ROLES};
      this.guildOnly = true;
      this.cooldown = 10;
      this.cooldownScope = CooldownScope.GUILD;
   }

   @Override
   protected void execute(final CommandEvent event) {
      GuildInfoUtil giu = new GuildInfoUtil(event.getGuild());

      if(!CommandUtil.checkPermissions(event.getMember(), giu.getStaffRole())) {
        event.reply("You do not have the necessary permissions to use this command.");
        return;
      }
      event.reply("Initiated refresh of Role Level assignments.");
      logger.info(format("[%s] Started refresh of Role Level assignments.", event.getGuild()));
      XpUtil.updateLevelRoleAssignments(event.getGuild());
      event.reply("Role Level assignments up-to-date.");

   }
}
