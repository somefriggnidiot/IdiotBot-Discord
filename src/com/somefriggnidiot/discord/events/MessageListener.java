package com.somefriggnidiot.discord.events;

import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.MessageListenerUtil;
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

      handleXp(event, msg);
      logEvent(event, author, channel, msg);
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

      if (gi != null || gi.isGrantingMessageXp() == null) {
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

   private void bridgeToGuilded(final String webhooUrl, final Message message) {
      CloseableHttpClient httpClient = HttpClients.createDefault();
      HttpPost request = new HttpPost(webhooUrl);
      JSONObject payload = new JSONObject();
      request.addHeader("content-type", "application/json");

      //Add author and any plaintext message to payload.
      String template = "%s: %s";
      String author = message.getGuild().getMember(message.getAuthor()).getEffectiveName();
      String payloadContent = String.format(template, author, message.getContentDisplay());

      payload.put("content", payloadContent);

      //Add any embeds to the message.
      if(message.getEmbeds().size() > 0) {
         JSONArray embedArray = new JSONArray();
         message.getEmbeds().forEach(e -> embedArray.put(e.toJSONObject()));
         payload.put("embeds", new JSONArray());
      }

      try {
         request.setEntity(new StringEntity(payload.toString()));
         HttpResponse response = httpClient.execute(request);
         httpClient.close();

         logger.info(response.toString());
      } catch (Exception e) {
         logger.warn("Error when trying to bridge to Guilded", e);
      }
   }
}
