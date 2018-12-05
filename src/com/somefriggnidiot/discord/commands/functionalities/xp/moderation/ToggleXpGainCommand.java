package com.somefriggnidiot.discord.commands.functionalities.xp.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import net.dv8tion.jda.core.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToggleXpGainCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   public ToggleXpGainCommand() {
      this.name = "togglexpgains";
      this.aliases = new String[]{"togglegains", "togglexp"};
      this.arguments = "";
      this.requiredRole = "Staff";
      this.category = new Category("Functionality");
      this.help = "Displays information about your current xp and level on IdiotBot.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ,
          Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};
      this.guildOnly = false;
      this.cooldown = 5;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      GuildInfo gi = GuildInfoUtil.getGuildInfo(event.getGuild().getIdLong());

      if (gi.isGrantingMessageXp()) {
         GuildInfoUtil.setXpTracking(event.getGuild().getIdLong(), false);
         logger.info(String.format("[%s] Guild is no longer granting message xp.",
             event.getGuild()));
         event.reply("XP tracking disabled.");
      } else {
         GuildInfoUtil.setXpTracking(event.getGuild().getIdLong(), true);
         logger.info(String.format("[%s] Guild is now granting message xp.",
             event.getGuild()));
         event.reply("XP tracking enabled.");
      }
   }
}
