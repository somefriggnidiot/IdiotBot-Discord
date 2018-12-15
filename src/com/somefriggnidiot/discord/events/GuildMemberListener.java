package com.somefriggnidiot.discord.events;

import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuildMemberListener extends ListenerAdapter {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);
   private final String[] bannedNames = new String[]{"bitch", "cunt", "whore", "fag", "slut"};

   @Override
   public void onGuildMemberNickChange(GuildMemberNickChangeEvent event) {
      String oldName = event.getPrevNick() == null ? event.getMember().getUser().getName() :
          event.getPrevNick();
      String newName = event.getNewNick() == null ? event.getMember().getEffectiveName() :
          event.getNewNick();

      logger.info(String.format("[%s] %s changed their name to %s.",
          event.getGuild(),
          oldName,
          newName));

      for (String bannedSegment : bannedNames) {
         if (newName.contains(bannedSegment)) {
            event.getGuild().getController().setNickname(event.getMember(), null).queue();
         }
      }
   }
}
