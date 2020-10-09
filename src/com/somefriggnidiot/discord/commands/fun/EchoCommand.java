package com.somefriggnidiot.discord.commands.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.Permission;

public class EchoCommand extends Command {
   public EchoCommand() {
      this.name = "echo";
      this.category = new Category("Fun");
      this.help = "Repeats your message.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
      this.guildOnly = false;
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      String message = event.getMessage().getContentRaw().split("\\s", 2)[1];
      event.reply(message);
   }

}

