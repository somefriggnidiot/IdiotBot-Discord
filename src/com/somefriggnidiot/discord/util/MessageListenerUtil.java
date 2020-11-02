package com.somefriggnidiot.discord.util;

import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility/helper methods used to handle a combination of {@link MessageReceivedEvent}s and the
 * XP system.
 *
 * @see XpUtil
 * @see MessageListener
 */
public class MessageListenerUtil {

   private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);
   private static final DecimalFormat df = new DecimalFormat("###,###");

   /**
    * Assigns XP to the authoring {@link net.dv8tion.jda.api.entities.User} of a valid
    * text message.
    *
    * @param event the {@link MessageReceivedEvent} containing the potentially valid message.
    */
   public static void handleXpGain(MessageReceivedEvent event) {
      if (event.getAuthor().isBot()) return;

      Long userId = event.getAuthor().getIdLong();
      DatabaseUser dbu = DatabaseUserUtil.getUser(userId);
      Instant messageTime = event.getMessage().getTimeCreated()
          .toInstant().truncatedTo(ChronoUnit.MINUTES);
      Instant userLastMessageTime;

      if (XpUtil.tokenDropActivated()) {
         XpUtil.handleTokenDrops(event.getGuild(), event.getAuthor(), 1);
      }

      userLastMessageTime = updateDbuLastMessageTimestamp(event, dbu);

      if (messageTime.isAfter(userLastMessageTime)) {
         Boolean luckMultiplierActivated = XpUtil.luckMultiplierActivated();
         Double multiplier = luckMultiplierActivated ? XpUtil.getLuckMultiplier() : 1.0;
         Integer xpGain = calculateXpGain(event.getMessage().getContentDisplay(), multiplier);
         Integer playerLevel = DatabaseUserUtil.getUser(userId).getLevelMap()
             .get(event.getGuild().getIdLong()) == null ? 0 : DatabaseUserUtil
             .getUser(userId).getLevelMap().get(event.getGuild().getIdLong());
         Integer newXp = DatabaseUserUtil.addXp(event.getGuild().getIdLong(), userId, xpGain +
             playerLevel) - 5; //Throws NPR on playerLevel

         if (luckMultiplierActivated) {
            String user = event.getGuild().getMember(event.getAuthor()).getNickname();
            String multStr = multiplier.toString();
            String xpGainStr = xpGain.toString();
            String message = user + " has gotten a random XP multiplier of " + multStr + " for"
                + " a drop of " + xpGainStr + " XP!";
            event.getGuild().getTextChannelsByName("bot-spam", true).get(0)
                .sendMessage(message).queue();
         }

         handleLogging(event, xpGain + playerLevel, newXp, dbu);
      }
   }

   /**
    * Updates the timestamp as an {@link Instant} for the last message sent by the
    * {@link DatabaseUser}.
    *
    * @param event the {@link MessageReceivedEvent} containing a valid message.
    * @param dbu the {@link DatabaseUser} object pertaining to the user authoring the valid message.
    * @return an {@link Instant} representing the timestamp of the user's latest message.
    */
   private static Instant updateDbuLastMessageTimestamp(MessageReceivedEvent event, DatabaseUser
       dbu) {
      if (dbu.getLastMessageDtTm() == null) {
         DatabaseUserUtil.addXp(event.getGuild().getIdLong(), event.getAuthor().getIdLong(), 0);
         return Instant.now().truncatedTo(ChronoUnit.MINUTES);
      } else {
         return dbu.getLastMessageDtTm()
             .toInstant().truncatedTo(ChronoUnit.MINUTES);
      }
   }

   /**
    * Determines the amount of XP gained by a user for sending a message.
    *
    * @param messageContentDisplay the {@link String} contents of a message.
    * @return the amount of XP gained based on the message size and contents.
    */
   private static Integer calculateXpGain(String messageContentDisplay, Double multiplier) {
      //If message contains link or empty (image, embed, etc.).
      if (messageContentDisplay.contains("http")
          || messageContentDisplay.isEmpty()) {
         return 5;
      } else {
         Integer baseXp = ThreadLocalRandom.current().nextInt(15, 31);
         Integer lengthBonus = messageContentDisplay.length() / 25;

         Double totalGain = (baseXp + lengthBonus) * multiplier;

         return (int) Math.rint(totalGain);
      }
   }

   /**
    * Logs the {@link MessageReceivedEvent} based on the configured logger factory settings.
    *
    * @param event the {@link MessageReceivedEvent} containing the message.
    * @param xpGain the amount of XP gained by the message.
    * @param newXp the amount of XP the user has after the latest XP gain.
    * @param dbu the {@link DatabaseUser} representing the author of the message.
    */
   private static void handleLogging(MessageReceivedEvent event, Integer xpGain, Integer newXp,
       DatabaseUser dbu) {
      logger.info(String.format("[%s] %s gained %s xp for messaging. They're now at %s xp.",
          event.getGuild(),
          event.getGuild().getMember(event.getAuthor()).getEffectiveName(),
          df.format(xpGain),
          df.format(newXp)));

      if (XpUtil.checkForLevelUp(event.getGuild(), event.getAuthor(), newXp) > 0) {
         logger.info(String.format("[%s] %s has leveled up! Now level %s!",
             event.getGuild(),
             event.getAuthor().getName(),
             dbu.getLevelMap().get(event.getGuild().getIdLong()) == null ? "0" :
                 dbu.getLevelMap().get(event.getGuild().getIdLong()).toString()));
      }
   }
}
