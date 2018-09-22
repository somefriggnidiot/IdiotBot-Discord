package com.somefriggnidiot.discord.events;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageListener extends ListenerAdapter {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   @Override
   public void onMessageReceived(MessageReceivedEvent event) {
      User author = event.getAuthor();
      Message message = event.getMessage();
      MessageChannel channel = event.getChannel();

      String msg = message.getContentDisplay();

      if (event.isFromType(ChannelType.TEXT)) {
         logger.info(String.format("[%s] %s in %s: %s",
             event.getGuild(),
             author.getName(),
             channel.getName(),
             msg));
      }
   }
}
