package com.somefriggnidiot.discord.commands.moderation;

import static java.lang.String.format;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.core.Main;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import java.time.OffsetDateTime;
import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfodumpCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(this.getClass());

   public InfodumpCommand() {
      this.name = "infodump";
      this.aliases = new String[]{"dump", "dox"};
      this.arguments = "<guildId>";
      this.category = new Category("Owner");
      this.help = "Dumps all server info into the console log.";
      this.botPermissions = new Permission[]{Permission.ADMINISTRATOR};
      this.guildOnly = false;
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
      this.ownerCommand = true;
   }

   @Override
   protected void execute(final CommandEvent event) {
      String[] args = event.getMessage().getContentDisplay().split("\\s", 2);
      GuildInfoUtil giu = new GuildInfoUtil(Long.valueOf(args[1]));
      Guild guild = Main.jda.getGuildById(args[1]);

      List<TextChannel> channels = guild.getTextChannels();

      for (TextChannel channel : channels) {
         logger.info(format("[%s] SERVER HISTORY - CHANNEL %s", guild, channel.getName()));
         logger.info(format("[%s] SERVER HISTORY - CHANNEL %s", guild, channel.getName()));
         logger.info(format("[%s] SERVER HISTORY - CHANNEL %s", guild, channel.getName()));
         logger.info(format("[%s] SERVER HISTORY - CHANNEL %s", guild, channel.getName()));
         logger.info(format("[%s] SERVER HISTORY - CHANNEL %s", guild, channel.getName()));
         channel.getIterableHistory()
             .takeAsync(1000)
             .thenApply(list -> list
                 .stream()
                 .filter(message -> message.getTimeCreated().isAfter(OffsetDateTime.now().minusYears(1)))
                 .forEach(recent -> recent.getTimeCreated());
//                 .forEach(history -> {
//                    if (history.getTimeCreated().isAfter(OffsetDateTime.now().minusYears(1)))
//
//                       if (history.getEmbeds().size() > 0) {
//                          logEmbed(history);
//                       } else {
//                          logMessage(history);
//                       }
//                 }););
      }
   }

   private void logMessage(Message message) {
      logger.info(format("\t[%s] %s [%s] %s", message.getChannel(),
          message.getTimeCreated().toLocalDateTime(),
          message.getAuthor().getName(), message.getContentDisplay()));
   }


   private void logEmbed(Message message) {
      String embedFormat = "\tTITLE: %s \n"
          + "\t\tLINK: %s \n"
          + "\t\tCONTENT: %s \n"
          + "\t\tFIELDS: [%s]";
      String display;

      for (MessageEmbed embed : message.getEmbeds()) {
         String fields = "";

         for (Field field : embed.getFields()) {
            fields = fields.concat(format("[Title: %s, Value: %s]", field.getName(), field.getValue()));
         }

         display = format(embedFormat, embed.getTitle(), embed.getUrl(), embed.getDescription(),
             fields);

         logger.info(format("\t[%s] %s [%s] %s", message.getChannel(),
             message.getTimeCreated().toLocalDateTime(),
             message.getAuthor().getName(), display));
      }
   }
}
