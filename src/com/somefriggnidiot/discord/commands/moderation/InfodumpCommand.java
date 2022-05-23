package com.somefriggnidiot.discord.commands.moderation;

import static java.lang.String.format;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.core.Main;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfodumpCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(this.getClass());
   private PrintWriter writer;

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
      Guild guild = Main.jda.getGuildById(args[1]);

      List<TextChannel> channels = guild.getTextChannels();
      String guildName = guild.getName().replaceAll("\\s", "");
      String time = format("%s%s%s",
          OffsetDateTime.now().getYear(),
          OffsetDateTime.now().getMonth(),
          OffsetDateTime.now().getDayOfMonth());

      for (TextChannel channel : channels) {
         try {
            writer = new PrintWriter(
                format("%s_%s_%s.txt",
                    guildName,
                    channel.getName().replaceAll("\\s", ""),
                    time),
                "UTF-8");
            writer.println(format("[%s] SERVER HISTORY - CHANNEL %s", guild, channel.getName()));
         } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
         }

         MessagePaginationAction channelIterableHistory = channel.getIterableHistory();
         for (Message history : channelIterableHistory) {
            if (history.getTimeCreated().isAfter(OffsetDateTime.now().minusYears(1))) {
               if (history.getEmbeds().size() > 0) {
                  logEmbed(history, writer);
               } else {
                  logMessage(history, writer);
               }
            }
         }

         writer.close();
//         channelIterableHistory
//             .takeAsync(1000)
//             .thenApply(list -> {
//                list.forEach(history -> {
//                   if (history.getTimeCreated().isAfter(OffsetDateTime.now().minusYears(2))) {
//                      if (history.getEmbeds().size() > 0) {
//                         logEmbed(history, writer);
//                      } else {
//                         logMessage(history, writer);
//                      }
//                   }
//                });
//                writer.close();
//                return null;
//             });

//         for (Message history : channel.getIterableHistory()) {
//            if (history.getTimeCreated().isAfter(OffsetDateTime.now().minusYears(1))) {
//               if (history.getEmbeds().size() > 0) {
//                  logEmbed(history, writer);
//               } else {
//                  logMessage(history, writer);
//               }
//            }
//         }
      }
   }

   private void logMessage(Message message, PrintWriter writer) {
      String user = message.getAuthor().getName();

      try {
         user = message.getMember().getEffectiveName();
      } catch (Exception ignored) {

      }
      String str = format("[%s] %s [%s] %s",
          message.getChannel().getName(),
          message.getTimeCreated().toLocalDateTime(),
          user,
          message.getContentDisplay());
      writer.println(str);
   }


   private void logEmbed(Message message, PrintWriter writer) {
      String embedFormat = "TITLE: %s \n"
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

         String str = format("\t[%s] %s [%s] %s", message.getChannel().getName(),
             message.getTimeCreated().toLocalDateTime(),
             message.getAuthor().getName(), display);

         logger.info(str);
         writer.println(str);
      }
   }
}
