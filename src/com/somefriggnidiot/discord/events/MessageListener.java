package com.somefriggnidiot.discord.events;

import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.MessageListenerUtil;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.managers.GuildManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
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
   private CloseableHttpClient httpClient = HttpClients.createDefault();

   @Override
   public void onMessageReceived(MessageReceivedEvent event) {
      User author = event.getAuthor();
      Message message = event.getMessage();
      MessageChannel channel = event.getChannel();
      String msg = message.getContentDisplay();

      GuildInfo gi = GuildInfoUtil.getGuildInfo(event.getGuild().getIdLong());
      Boolean isGrantingXp = gi.isGrantingMessageXp() == null ? false : gi.isGrantingMessageXp();

      if (isGrantingXp) {
         if (!msg.startsWith("!") && !msg.startsWith("$") && !msg.startsWith("~")) {
            MessageListenerUtil.handleXpGain(event);
         }
      }

      if (author.getName().equalsIgnoreCase("Discord.RSS")) {
         message.addReaction("👍").queue();
         message.addReaction("👎").queue();
      }

      //Logging
      if (event.isFromType(ChannelType.TEXT) &&
          !event.getChannel().getName().equalsIgnoreCase("log")) {
         logger.info(String.format("[%s] %s in #%s: %s",
             event.getGuild(),
             author.getName(),
             channel.getName(),
             msg));
      }

      if (event.isFromType(ChannelType.TEXT) &&
          event.getGuild().getName().contains("Celestial") &&
          !event.getMessage().isWebhookMessage()) {
         if (event.getChannel().getName().equalsIgnoreCase("log")) {
            bridgeToGuilded("https://media.guilded.gg/webhooks/fca53363-1ed5-418c-a296-ea82a4961300/zhjKqJSsvjpguDms5PjWOkdHscC6Tmcl_ReEqCicOPrJKw-z-Ui8kFuswUhm1nI4wDezAZcCNRMatWbfs9nLWA", event.getMessage());
         }
      }
   }

   private void bridgeToGuilded(final String webhooUrl, final Message message) {
      httpClient = HttpClients.createDefault();
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
