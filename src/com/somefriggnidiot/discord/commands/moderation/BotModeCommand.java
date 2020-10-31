package com.somefriggnidiot.discord.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.util.BotModeUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotModeCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

   /*
      Enabled on / configured to a channel.
      Channel will only allow messages starting with prefix.
    */
   public BotModeCommand() {
      this.name = "commandonlymode";
      this.aliases = new String[]{"botonlymode", "botmode", "commandmode"};
      this.arguments = "<\"on\"|\"off\"> <prefix>";
      this.category = new Category("Moderation");
      this.requiredRole = "Staff";
      this.cooldown = 5;
      this.cooldownScope = CooldownScope.GUILD;
      this.help = "Used to enable or disable bot-only / command-only mode. When enabled, any "
          + "message to the channel not starting with the specified prefix shall be removed. Once"
          + " enabled, can be disabled by calling '!commandonlymode off <prefix>' on the desired "
          + "prefix in the channel where it is enabled.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE, Permission.MESSAGE_READ};
   }

   @Override
   protected void execute(CommandEvent event) {
      String cmd = event.getMessage().getContentDisplay().split("\\s", 2)[1];
      String[] args = cmd.split("\\s", 2);
      String actionArg = args[0];
      String prefix = args[1];

      Guild guild = event.getGuild();

      if (actionArg.equalsIgnoreCase("on")) {
         BotModeUtil.createBotModeEntry(guild.getIdLong(), event.getChannel().getIdLong(), prefix);
         event.reply(String.format("BotMode has been enabled for this channel. Any messages sent "
             + "to this channel not prefixed '%s' will be automatically deleted.",
             prefix));
      } else if (actionArg.equalsIgnoreCase("off")) {
         //TODO disable workflow
      } else {
         //TODO error workflow
      }
   }
}
