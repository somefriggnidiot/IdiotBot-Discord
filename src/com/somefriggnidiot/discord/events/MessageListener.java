package com.somefriggnidiot.discord.events;

import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.MessageListenerUtil;
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

      GuildInfo gi = GuildInfoUtil.getGuildInfo(event.getGuild().getIdLong());
      Boolean isGrantingXp = gi.isGrantingMessageXp() == null ? false : gi.isGrantingMessageXp();

      if (isGrantingXp && !msg.startsWith("!")) {
         MessageListenerUtil.handleXpGain(event);
      }

      //Karma Check
      //TODO do it on mentions instead.
//      List<User> mentionedUsers = message.getMentionedUsers();
//
//      if(!mentionedUsers.isEmpty()) {
//         String[] split = msg.split("//s");
//         Boolean addKarma = null;
//         Integer karma = 0;
//
//         for (String string : split) {
//            if(string.endsWith("++")) {
//               karma = KarmaUpdate.updateUser(mentionedUsers.get(0), true);
//               event.getChannel().sendMessage(String.format("%s leveled up! (%s karma)",
//                   mentionedUsers.get(0).getName(), karma.toString()));
//               break;
//            } else if (string.endsWith("--")) {
//               karma = KarmaUpdate.updateUser(mentionedUsers.get(0), false);
//               event.getChannel().sendMessage(String.format("%s got pwned! (%s karma)",
//                   mentionedUsers.get(0).getName(), karma.toString()));
//               break;
//            }
//         }
//      }

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
