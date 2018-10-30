package com.somefriggnidiot.discord.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.events.MessageListener;
import com.somefriggnidiot.discord.data_access.util.UserWarningUtil;
import java.util.Timer;
import java.util.TimerTask;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoftBanCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   public SoftBanCommand() {
      this.name = "softban";
      this.category = new Category("Moderation");
      this.help = "Soft-bans the mentioned user. While the soft ban is in place, the user cannot "
          + "speak or type until it is lifted. A soft-banned user will automatically receive a "
          + "warning for their ban.";
      this.arguments = "<user> <duration in minutes> <reason>";
      this.requiredRole = "Staff";
      this.aliases = new String[] {"sb"};
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(CommandEvent event) {
      Member target = event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0));
      Long duration = Long.parseLong(
          event.getMessage().getContentDisplay().split("\\s", 4)[2])*60*1000;
      String reason = event.getMessage().getContentDisplay().split("\\s", 4)[3];
      Timer timer = new Timer();

      if (reason.isEmpty()) {
         event.reply("You must provide a reason.");
         return;
      }

      Role softbanRole = event.getGuild().getRolesByName("softban", false).get(0);

      event.getGuild().getController().addSingleRoleToMember(target, softbanRole).queue();

      logger.info(String.format("[%s] %s has been soft-banned for %s minutes by %s for: %s",
          event.getGuild(),
          target.getEffectiveName(),
          duration/60000,
          event.getAuthor().getName(),
          reason));

      UserWarningUtil.warnUser(event.getMessage().getMentionedUsers().get(0), reason,
          event.getAuthor().getIdLong());

      timer.schedule((new TimerTask() {
         @Override
         public void run() {
            event.getGuild().getController().removeSingleRoleFromMember(target, softbanRole).queue();
            target.getUser().openPrivateChannel().queue(success ->
                success.sendMessage(String.format(
                    "Your ban has been lifted in %s. Please be sure to better follow the rules in the future.",
                    event.getGuild().getName()))
                .queue());
            timer.cancel();
         }
      }), duration);
   }
}
