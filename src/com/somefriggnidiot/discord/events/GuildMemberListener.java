package com.somefriggnidiot.discord.events;

import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener class for {@code GuildMember} events.
 */
public class GuildMemberListener extends ListenerAdapter {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);
   private final String[] bannedNames = new String[]{"bitch", "cunt", "whore", "fag", "slut",
       "hitler", "fuck"};

   @Override
   public void onGuildMemberNickChange(GuildMemberNickChangeEvent event) {

      logNicknameChange(event);
      resetBannedName(event);
   }

   /**
    * Resets a the nickname of a {@link net.dv8tion.jda.core.entities.User} if their new nickname
    * contains a banned word.
    *
    * @param event the {@link GuildMemberNickChangeEvent} including the {@code User} being scanned.
    */
   private void resetBannedName(GuildMemberNickChangeEvent event) {
      String newName = event.getNewNick() == null ? event.getMember().getEffectiveName() :
          event.getNewNick();

      for (String bannedSegment : bannedNames) {
         if (newName.contains(bannedSegment)) {
            event.getGuild().getController().setNickname(event.getMember(), null).queue();
         }
      }
   }

   /**
    * Logs the {@link GuildMemberNickChangeEvent} details to the console and log file.
    *
    * @param event {@link GuildMemberNickChangeEvent} being logged.
    */
   private void logNicknameChange(GuildMemberNickChangeEvent event) {
      String oldName = event.getPrevNick() == null ? event.getMember().getUser().getName() :
          event.getPrevNick();
      String newName = event.getNewNick() == null ? event.getMember().getEffectiveName() :
          event.getNewNick();

      logger.info(String.format("[%s] %s changed their name to %s.",
          event.getGuild(),
          oldName,
          newName));
   }
}
