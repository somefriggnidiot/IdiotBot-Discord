package com.somefriggnidiot.discord.events;

import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener class for {@code GuildMember} events.
 */
public class GuildMemberListener extends ListenerAdapter {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);
   private final String[] bannedNames = new String[]{"cunt", "whore", "fag", "slut",
       "hitler", "fuck"};

   @Override
   public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
      logNicknameChange(event);
      resetBannedName(event);
   }

   @Override
   public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
      logger.info(String.format("[%s] %s has left the server.",
          event.getGuild(),
          event.getMember().getEffectiveName()));
   }

   @Override
   public void onGuildMemberJoin(GuildMemberJoinEvent event) {
      Long guildId = event.getGuild().getIdLong();
      Long memberId = event.getMember().getUser().getIdLong();

      DatabaseUserUtil.setXp(guildId, memberId, 0);
      DatabaseUserUtil.getUser(memberId).updateLevel(guildId, 0);

      GuildInfoUtil giu = new GuildInfoUtil(event.getGuild());
      Role guestRole = giu.getGuestRole();
      if (null != guestRole) {
         event.getGuild().addRoleToMember(event.getMember(), guestRole).complete();
         logger.info(String.format("[%s] %s has been assigned the %s role.",
             event.getGuild(),
             event.getMember().getEffectiveName(),
             guestRole.getName()));
      }

      logger.info(String.format("[%s] %s has joined the server.",
          event.getGuild(),
          event.getMember().getEffectiveName()));
   }

   /**
    * Resets a the nickname of a {@link net.dv8tion.jda.api.entities.User} if their new nickname
    * contains a banned word.
    *
    * @param event the {@link GuildMemberUpdateNicknameEvent} including the {@code User} being scanned.
    */
   private void resetBannedName(GuildMemberUpdateNicknameEvent event) {
      String newName = event.getNewNickname() == null ? event.getMember().getEffectiveName() :
          event.getNewNickname();

      for (String bannedSegment : bannedNames) {
         if (newName.toLowerCase().contains(bannedSegment)) {
            event.getGuild().modifyNickname(event.getMember(), null).queue();
         }
      }
   }

   /**
    * Logs the {@link GuildMemberUpdateNicknameEvent} details to the console and log file.
    *
    * @param event {@link GuildMemberUpdateNicknameEvent} being logged.
    */
   private void logNicknameChange(GuildMemberUpdateNicknameEvent event) {
      String oldName = event.getOldNickname() == null ? event.getMember().getUser().getName() :
          event.getOldNickname();
      String newName = event.getNewNickname() == null ? event.getMember().getEffectiveName() :
          event.getNewNickname();

      logger.info(String.format("[%s] %s changed their name to %s.",
          event.getGuild(),
          oldName,
          newName));
   }
}
