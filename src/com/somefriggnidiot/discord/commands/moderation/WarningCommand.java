package com.somefriggnidiot.discord.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.data_access.util.UserWarningUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import com.somefriggnidiot.discord.util.command_util.CommandUtil;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarningCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   public WarningCommand() {
      this.name = "warn";
      this.category = new Category("Moderation");
      this.help = "Adds a warning to a user.";
      this.arguments = "<user> <reason>";
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(CommandEvent event) {
      GuildInfoUtil giu = new GuildInfoUtil(event.getGuild());
      if (!CommandUtil.checkPermissions(event.getMember(), giu.getStaffRole())) {
         event.reply("You do not have the necessary permissions to use this command.");
         return;
      }

      User target = event.getMessage().getMentionedUsers().get(0);
      String reason = event.getMessage().getContentDisplay().split("\\s", 3)[2];

      Integer totalWarnings = UserWarningUtil
          .warnUser(target, reason, event.getAuthor().getIdLong());

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
