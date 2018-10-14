package com.somefriggnidiot.discord.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.events.MessageListener;
import java.util.Timer;
import java.util.TimerTask;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoftBanCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   public SoftBanCommand() {
      this.name = "softban";
      this.category = new Category("Moderation");
      this.help = "Soft-bans the mentioned user. While the soft ban is in place, the user cannot "
          + "speak or type until it is lifted.";
      this.arguments = "<user> <duration in minutes>";
      this.requiredRole = "Staff";
   }

   @Override
   protected void execute(CommandEvent event) {
      Member target = event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0));
      Long duration = Long.parseLong(
          event.getMessage().getContentDisplay().split("\\s", 3)[2])*60*1000;
      Timer timer = new Timer();

      Role softbanRole = event.getGuild().getRolesByName("softban", false).get(0);

      event.getGuild().getController().addSingleRoleToMember(target, softbanRole).queue();

      timer.schedule((new TimerTask() {
         @Override
         public void run() {
            event.getGuild().getController().removeSingleRoleFromMember(target, softbanRole).queue();
            timer.cancel();
         }
      }), duration);
   }
}
