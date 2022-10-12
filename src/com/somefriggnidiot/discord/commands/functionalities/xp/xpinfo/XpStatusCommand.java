package com.somefriggnidiot.discord.commands.functionalities.xp.xpinfo;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XpStatusCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(XpStatusCommand.class);

   public XpStatusCommand() {
      this.name = "xpstatus";
      this.aliases = new String[]{"xpstats", "xpinfo"};
      this.arguments = "";
      this.category = new Category("Xp Info");
      this.help = "Displays various information about the XP system.";
      this.botPermissions = new Permission[]{Permission.MANAGE_ROLES, Permission.MESSAGE_READ,
          Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};
      this.guildOnly = true;
      this.cooldown = 5;
      this.cooldownScope = CooldownScope.GUILD;
   }

   @Override
   protected void execute(final CommandEvent event) {
      GuildInfo gi = GuildInfoUtil.getGuildInfo(event.getGuild().getIdLong());
      String xpTracking = gi.isGrantingMessageXp() ? "Enabled" : "Disabled";
      String levelRoles = "" + gi.getRoleLevelMappings().size();
      String levelRoleNames = "";
      gi.getRoleLevelMappings().keySet().forEach(role -> event.getGuild()
          .getRoleById(role).getName());
      EmbedBuilder eb = new EmbedBuilder()
          .setTitle("XP Stats")
          .addField("XP Tracking", xpTracking, true)
          .addField("Total Level Roles", levelRoles, true);
      //Add total xp on server

      event.reply(eb.build());
   }
}
