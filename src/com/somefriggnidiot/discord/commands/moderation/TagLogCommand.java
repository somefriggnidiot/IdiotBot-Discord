package com.somefriggnidiot.discord.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import net.dv8tion.jda.core.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagLogCommand extends Command{

   private final Logger logger = LoggerFactory.getLogger(this.getClass());

   public TagLogCommand() {
      this.name = "taglog";
      this.aliases = new String[]{"snapshot", "logtag"};
      this.arguments = "(notes)";
      this.category = new Category("Moderation");
      this.help = "Tags the server log with a unique ID and optional notes for easy searching.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_WRITE};
      this.guildOnly = true;
      this.requiredRole = "Staff";
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      String message = event.getMessage().getContentDisplay();
      String notes = message.contains(" ") ? message.split("\\s", 2)[1] : "";
      String uuid = UUID.randomUUID().toString();

      logger.info(String.format("[%s] LOGMARKER-%s\nNotes:\n%s",
          event.getGuild(),
          uuid,
          notes));

      event.replyInDm(String.format("Log successfully tagged. Please keep the following "
          + "information for future reference:\n"
          + "Tag: LOGMARKER-%s\n"
          + "Timestamp: %s\n",
          uuid,
          LocalDateTime.now()));
   }
}
