package com.somefriggnidiot.discord.util;

import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageListenerUtil {

   private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   public static void handleXpGain(MessageReceivedEvent event) {
      if (event.getAuthor().isBot()) return;

      Long userId = event.getAuthor().getIdLong();
      DatabaseUser dbu = DatabaseUserUtil.getUser(userId);
      Instant messageTime = event.getMessage().getCreationTime()
          .toInstant().truncatedTo(ChronoUnit.MINUTES);
      Instant userLastMessageTime;

      if (dbu.getLastMessageDtTm() == null) {
         DatabaseUserUtil.addXp(event.getGuild().getIdLong(), event.getAuthor().getIdLong(), 0);
         userLastMessageTime = Instant.now().truncatedTo(ChronoUnit.MINUTES);
      } else {
         userLastMessageTime = dbu.getLastMessageDtTm()
             .toInstant().truncatedTo(ChronoUnit.MINUTES);
      }

      if (messageTime.isAfter(userLastMessageTime)) {
         Integer messageLength = event.getMessage().getContentDisplay().length();
         Integer xpGain;
         //If message contains link
         if (event.getMessage().getContentDisplay().contains("http")
             || event.getMessage().getContentDisplay().isEmpty()) {
            xpGain = 5;
         } else {
            xpGain = ThreadLocalRandom.current().nextInt(10, 31);
            xpGain += (messageLength / 20);
         }

         Integer newXp = DatabaseUserUtil.addXp(event.getGuild().getIdLong(), userId, xpGain);
         logger.info(String.format("[%s] %s gained %s xp for messaging. They're now at %s xp.",
             event.getGuild(),
             event.getAuthor().getName(),
             xpGain,
             newXp));

         if (XpUtil.checkForLevelUp(event.getGuild(), event.getAuthor(), newXp) > 0) {
            logger.info(String.format("[%s] %s has leveled up! Now level %s!",
                event.getGuild(),
                event.getAuthor().getName(),
                dbu.getLevel()));
         }
      }
   }

}
