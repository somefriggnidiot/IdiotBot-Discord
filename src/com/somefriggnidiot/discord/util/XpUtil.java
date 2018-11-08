package com.somefriggnidiot.discord.util;

import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import java.awt.Color;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XpUtil {
   private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   public static Boolean checkForLevelUp(MessageReceivedEvent event, Integer currentLevel,
       Integer newXp, Long userId) {
      Integer xpThreshold = getXpThresholdForLevel(currentLevel+1);
      Integer newLevel = currentLevel;

      if (newXp > xpThreshold) {
         newLevel = DatabaseUserUtil.incrementLevel(userId);

         EmbedBuilder eb = new EmbedBuilder()
             .setColor(Color.CYAN)
             .setThumbnail(event.getAuthor().getAvatarUrl())
             .setTitle(String.format("%s has leveled up!", event.getAuthor().getName()))
             .setFooter("Provided to you by IdiotBot. The most idiotic of bots.",
                 "http://www.foundinaction.com/wp-content/uploads/2018/08/"
                     + "Neon_600x600_Transparent.png")
             .addField("Current Level", newLevel.toString(), true)
             .addField("Progress to Next Level",
                 newXp + " / " + getXpThresholdForLevel(newLevel+1)
                     .toString(),
                 true);

         event.getChannel().sendMessage(eb.build()).queue();
      } else if (newXp < getXpThresholdForLevel(currentLevel)) {
         newLevel = DatabaseUserUtil.setLevel(userId, currentLevel-1);
         logger.info(String.format("[%s] Demoted %s to level %s.",
             event.getGuild(),
             event.getAuthor().getName(),
             newLevel));
      }

      return newLevel > currentLevel;
   }

   public static boolean checkForLevelUp(GuildVoiceJoinEvent event, Integer newXp) {
      Long userId = event.getMember().getUser().getIdLong();
      Integer currentLevel = DatabaseUserUtil.getUser(userId).getLevel();
      Integer xpThreshold = getXpThresholdForLevel(currentLevel+1);
      Integer newLevel = currentLevel;

      if (newXp > xpThreshold) {
         newLevel = DatabaseUserUtil.incrementLevel(userId);

         EmbedBuilder eb = new EmbedBuilder()
             .setColor(Color.CYAN)
             .setThumbnail(event.getMember().getUser().getAvatarUrl())
             .setTitle(String.format("%s has leveled up!", event.getMember().getUser().getName()))
             .setFooter("Provided to you by IdiotBot. The most idiotic of bots.",
                 "http://www.foundinaction.com/wp-content/uploads/2018/08/"
                     + "Neon_600x600_Transparent.png")
             .addField("Current Level", newLevel.toString(), true)
             .addField("Progress to Next Level",
                 newXp + " / " + getXpThresholdForLevel(newLevel+1)
                     .toString(),
                 true);

         event.getGuild().getTextChannelsByName("general", false).get(0)
             .sendMessage(eb.build()).queue();
      } else if (newXp < getXpThresholdForLevel(currentLevel)) {
         newLevel = DatabaseUserUtil.setLevel(userId, currentLevel-1);
         logger.info(String.format("[%s] Demoted %s to level %s.",
             event.getGuild(),
             event.getMember().getUser().getName(),
             newLevel));
      }

      return newLevel > currentLevel;
   }

   private static void handleRoleAssignments(TextChannel channel, Long userId, Long guildId) {
      return;
   }

   public static Integer getXpThresholdForLevel(Integer level) {
      Integer threshold = 0;

      for (int i=0; i<level; i++) {
         threshold += 5*(i*i) + 50*(i) + 100;
      }

      return threshold;
   }
}
