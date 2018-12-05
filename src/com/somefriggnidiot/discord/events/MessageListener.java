package com.somefriggnidiot.discord.events;

import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.MessageListenerUtil;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Emote;
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

      GuildInfo gi = GuildInfoUtil.getGuildInfo(event.getGuild().getIdLong());
      Boolean isGrantingXp = gi.isGrantingMessageXp() == null ? false : gi.isGrantingMessageXp();

      if (isGrantingXp && !msg.startsWith("!")) {
         MessageListenerUtil.handleXpGain(event);
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
   }
}
