package com.somefriggnidiot.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.events.MessageListener;
import com.somefriggnidiot.discord.util.KarmaUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KarmaCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   public KarmaCommand() {
      this.name = "karma";
      this.help = "Gives another user a fake point.";
      this.arguments = "<user>";
      this.cooldownScope = CooldownScope.USER;
      this.cooldown = 30;
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ};
   }

   @Override
   protected void execute(final CommandEvent event) {
      User user = event.getMessage().getMentionedUsers().get(0);
      Integer karma = KarmaUtil.updateUser(user);
      event.reply(String.format("%s now has %s karma!", user.getName(), karma));

      logger.info(String.format("[%s] Karma given to %s by %s.",
          event.getGuild(),
          event.getMessage().getMentionedUsers().get(0).getName(),
          event.getAuthor().getName()));
   }
}
