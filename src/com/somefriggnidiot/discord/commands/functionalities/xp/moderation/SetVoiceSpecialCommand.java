package com.somefriggnidiot.discord.commands.functionalities.xp.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.command_util.CommandUtil;
import net.dv8tion.jda.api.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetVoiceSpecialCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(SetVoiceSpecialCommand.class);

   public SetVoiceSpecialCommand() {
      this.name = "setvoicexpmultiplier";
      this.aliases = new String[]{"setvoicemultiplier", "setvoicemult", "voicemultiplier",
          "voicemult"};
      this.arguments = "<multiplier>";
      this.category = new Category("Xp Moderation");
      this.help = "Adds an additional multiplier to the XP gained while in voice chat.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ};
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

      Double multiplier = Double.parseDouble(event.getMessage().getContentDisplay().split("\\s",
          3)[1]);

      giu.setVoiceXpMultiplier(multiplier);
      event.reply(String.format("**Modifier for Voice XP Updated.**\nNow %sx.",
          multiplier));
      logger.info(String.format("[%s] Set added voice multiplier to %s",
          event.getGuild(),
          multiplier));
   }
}
