package com.somefriggnidiot.discord.commands.functionalities.xp.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.command_util.CommandUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToggleLuckBonusCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(ToggleLuckBonusCommand.class);

   public ToggleLuckBonusCommand() {
      this.name = "toggleluckbonus";
      this.aliases = new String[]{"toggleluck"};
      this.arguments = "";
      this.category = new Category("Xp Moderation");
      this.help = "Toggles whether or not an additional luck factor is applied to XP gains.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_WRITE,
          Permission.MESSAGE_EMBED_LINKS};
      this.guildOnly = true;
      this.cooldown = 5;
      this.cooldownScope = CooldownScope.GUILD;
   }

   @Override
   protected void execute(final CommandEvent event) {
      GuildInfoUtil giu = new GuildInfoUtil(event.getGuild());
      if (!CommandUtil.checkPermissions(event.getMember(), giu.getStaffRole())) {
         event.reply("You do not have the necessary permissions to use this command.");
         return;
      }

      Guild guild = event.getGuild();
      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild.getIdLong());

      if (gi.luckBonusActive()) {
         GuildInfoUtil.setLuckBonusActive(guild.getIdLong(), false);

         logger.info(String.format("[%s] Guild is no longer offering a luck bonus on xp.",
             event.getGuild()));
         event.reply("**Luck Bonus Disabled**\n"
             + "Users will no longer have rare bonuses applied to XP gains.");
      } else {
         GuildInfoUtil.setLuckBonusActive(guild.getIdLong(), true);

         logger.info(String.format("[%s] Guild is now offering a luck bonus on xp.",
             event.getGuild()));
         event.reply("**Luck Bonus Enabled**\n"
             + "Users may have rare bonuses applied to XP gains.");
      }
   }
}
