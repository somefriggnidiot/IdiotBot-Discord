package com.somefriggnidiot.discord.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.events.MessageListener;
import com.somefriggnidiot.discord.data_access.util.UserWarningUtil;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarningCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   public WarningCommand() {
      this.name = "warn";
      this.category = new Category("Moderation");
      this.help = "Adds a warning to a user.";
      this.arguments = "<user> <reason>";
      this.requiredRole = "Staff";
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(CommandEvent event) {
      User target = event.getMessage().getMentionedUsers().get(0);
      String reason = event.getMessage().getContentDisplay().split("\\s", 3)[2];

      Integer totalWarnings = UserWarningUtil.warnUser(target, reason, event.getAuthor().getIdLong());

      logger.info(String.format("[%s] %s has warned %s for \"%s\". %s now has %s warnings.",
          event.getGuild(),
          event.getAuthor().getName(),
          target.getName(),
          reason,
          target.getName(),
          totalWarnings));

//      target.openPrivateChannel().queue(success ->
//          {
//             MessageAction messageAction = success
//                 .sendMessage(String.format("You have been warned for \"%s\" in %s. \n"
//                         + "You have %s total warning%s now.",
//                     reason,
//                     event.getGuild().getName(),
//                     totalWarnings,
//                     totalWarnings > 1 ? "s" : ""));
//
//             messageAction.queue();
//          }
//      );
   }
}
