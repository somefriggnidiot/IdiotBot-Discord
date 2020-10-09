package com.somefriggnidiot.discord.events;

import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.MessageListenerUtil;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageListener extends ListenerAdapter {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   @Override
   public void onMessageReceived(final MessageReceivedEvent event) {
      User author = event.getAuthor();
      MessageChannel channel = event.getChannel();
      String msg = event.getMessage().getContentDisplay();

      logEvent(event, author, channel, msg);
      handleXp(event, msg);
      handleBotChannel(event.getGuild().getIdLong(), author, event.getMessage());
   }

   /**
    * Routes messages to the {@link MessageListenerUtil} for XP Gains when applicable.
    *
    * @param event a {@link MessageReceivedEvent} to be checked
    * @param msg the message contents as a {@link String}
    */
   private void handleXp(final MessageReceivedEvent event, final String msg) {
      GuildInfo gi = event.getGuild().isAvailable()? GuildInfoUtil.getGuildInfo(event.getGuild()
          .getIdLong()) : null;

      Boolean isGrantingXp;

      if (gi == null || gi.isGrantingMessageXp() == null) {
         isGrantingXp = false;
      } else {
         isGrantingXp = gi.isGrantingMessageXp();
      }

      if (isGrantingXp) {
         if (!msg.startsWith("!") && !msg.startsWith("$") && !msg.startsWith("~")) {
            MessageListenerUtil.handleXpGain(event);
         }
      }
   }

   /**
    * Logs the {@link MessageReceivedEvent} contents based on the current logging
    * configuration settings.
    *
    * @param event the {@link MessageReceivedEvent} being logged.
    * @param author the {@link User} that authored the message which created the
    * event.
    * @param channel the {@link MessageChannel} where the logged message was received.
    * @param msg the message contents as a {@link String}
    */
   private void logEvent(final MessageReceivedEvent event, final User author,
       final MessageChannel channel, final String msg) {
      Guild guild = event.getGuild();
      if (event.isFromType(ChannelType.TEXT) &&
          !event.getChannel().getName().equalsIgnoreCase("log")) {

         String userName;
         if (guild == null || guild.getMember(author) == null) {
            userName = author.getName();
         } else {
            userName = guild.getMember(author).getEffectiveName();
         }
         logger.info(String.format("[%s] %s in #%s: %s",
             guild == null ? "DIRECT MESSAGE" : event.getGuild(),
             userName,
             channel.getName(),
             msg));
      }
   }

   /**
    * Deprecated. This shouldn't even exist in the first place.
    *
    * Method specific to Celestial Brothers guild, deletes any messages in the bot channel not
    * preceeded with specified known bot prefixes.
    *
    * @param guildId the ID of the guild within which a message was sent.
    * @param author the User object of the sender of the message.
    * @param message the Message which was sent.
    */
   @Deprecated
   private void handleBotChannel(final Long guildId, final User author, final Message message) {
      List<String> allowedPrefixes = new ArrayList<>();
      List<Long> moderatedChannelIds = new ArrayList<>();

      //TODO Finish BotModeEntry model / BotModeUtil / BotModeCommand and make this flexible.
      moderatedChannelIds.add(728748254275305482L);

      if (guildId == 217175952701128714L && !author.isBot()) {
         if (moderatedChannelIds.contains(message.getChannel().getIdLong())) {
            allowedPrefixes.add(".rs ");
            allowedPrefixes.add(".pc ");

            Boolean allowed = false;

            for(String prefix : allowedPrefixes) {
               if (message.getContentRaw().startsWith(prefix)) {
                  allowed = true;
               }
            }

            if (!allowed) {
               message.delete().queue();
            }
         }
      }
   }

}
