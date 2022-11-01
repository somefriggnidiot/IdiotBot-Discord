package com.somefriggnidiot.discord.commands.moderation;

import static java.lang.String.format;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.core.Main;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
   private int channelsDone = 0;
   private int channelsTotal;

   public InfodumpCommand() {
      this.name = "infodump";
      this.aliases = new String[]{"dump", "dox"};
      this.arguments = "<guildId>";
      this.category = new Category("Owner");
      this.help = "Dumps all server messages into channel-specific files.";
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
      channelsTotal = channels.size();

      for (TextChannel channel : channels) {
         new Thread(() -> logChannelHistory(channel)).start();
      }
   }

   private void logChannelHistory(TextChannel channel) {
      MessagePaginationAction channelIterableHistory = channel.getIterableHistory();
      List<Message> messages =
          channelIterableHistory.stream()
              .filter(msg -> msg.getTimeCreated().isAfter(OffsetDateTime.now().minusYears(1)))
              .collect(Collectors.toList());

      List<String> channelLog = new ArrayList<>();
      messages.forEach(msg -> {
         if (msg.getEmbeds().size() > 0) {
            channelLog.addAll(logEmbed(msg));
         } else {
            channelLog.add(logMessage(msg));
         }
      });

      //Write at once.
      Guild guild = channel.getGuild();
      String guildName = guild.getName().replaceAll("\\s", "");
      String time = format("%s%s%s",
          OffsetDateTime.now().getYear(),
          OffsetDateTime.now().getMonthValue(),
          OffsetDateTime.now().getDayOfMonth());

      Path channelFile = Paths.get(format("%s_%s_%s.txt",
              guildName,
              channel.getName().replaceAll("\\s", ""),
              time));

      try {
         Collections.reverse(channelLog);
         Files.write(channelFile, channelLog, StandardCharsets.UTF_8);
         channelsDone++;
         logger.info(format("Channel logging has completed %s of %s channels.",
             channelsDone, channelsTotal));
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private String logMessage(Message message) {
      String user = message.getAuthor().getName();

      try {
         user = message.getMember().getEffectiveName();
      } catch (Exception ignored) {
      }

      final String[] messageContent = {""};

      if (message.getAttachments().size() > 0) {
         messageContent[0] = message.getContentDisplay();
         message.getAttachments().forEach(atch -> messageContent[0] = messageContent[0]
             .concat("\n\t Attachment: "  + atch.getUrl()));
      } else {
         messageContent[0] = message.getContentDisplay();
      }

      return format("[%s] %s [%s] %s",
          message.getChannel().getName(),
          message.getTimeCreated().toLocalDateTime(),
          user,
          messageContent[0]);
   }

   private List<String> logEmbed(Message message) {
      List<String> embedList = new ArrayList<>();
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

         String str = format("[%s] %s [%s] %s", message.getChannel().getName(),
             message.getTimeCreated().toLocalDateTime(),
             message.getAuthor().getName(), display);

//         logger.info(str);
         embedList.add(str);
      }

      return embedList;
   }
}
