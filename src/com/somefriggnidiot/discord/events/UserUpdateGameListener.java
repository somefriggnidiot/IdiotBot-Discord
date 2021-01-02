package com.somefriggnidiot.discord.events;

import static java.lang.String.format;

import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.GameGroupUtil;
import java.util.List;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivityOrderEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserUpdateGameListener extends ListenerAdapter {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   @Override
   public void onUserActivityStart(@NotNull UserActivityStartEvent event) {
      GameGroupUtil.refreshMemberGameGroups(event.getMember());

      if (event.getNewActivity().getType().equals(ActivityType.STREAMING)) {
         Guild guild = event.getGuild();
         GuildInfo gi = GuildInfoUtil.getGuildInfo(guild);
         List<Long> featuredStreamers = gi.getStreamerMemberIds();
         Member member = event.getMember();

         if (featuredStreamers != null && featuredStreamers.contains(member.getIdLong())) {
            Role streamerRole = guild.getRoleById(gi.getStreamerRoleId());

            if (streamerRole != null && !member.getRoles().contains(streamerRole)) {
               guild.addRoleToMember(member, streamerRole).queue();
               new GuildInfoUtil(guild).getBotTextChannel()
                   .sendMessage(format(
                       "%s has gone live! Use `!streamers` for more details.",
                       member.getEffectiveName())).queue();
               logger.info(format("[%s] %s has gone live: %s",
                   guild,
                   member.getEffectiveName(),
                   event.getNewActivity().getName()));
            } else {
               try {
                  guild.getTextChannelById(gi.getBotTextChannelId()).sendMessage("A featured "
                          + "streamer has gone live, but no streamer role has been configured!")
                      .queue();
               } catch (Exception e) {
                  return;
               }
               logger.error(format("[%s] Attempted to add %s to Featured Streamer role, but no "
                   + "role was found for RoleID %s",
                   guild,
                   member.getEffectiveName(),
                   gi.getStreamerRoleId()));
            }
         }
      }
   }

   @Override
   public void onUserActivityEnd(@NotNull UserActivityEndEvent event) {
      GameGroupUtil.refreshMemberGameGroups(event.getMember());

      if (event.getOldActivity().getType().equals(ActivityType.STREAMING)) {
         Guild guild = event.getGuild();
         GuildInfo gi = GuildInfoUtil.getGuildInfo(guild);
         List<Long> featuredStreamers = gi.getStreamerMemberIds();
         Member member = event.getMember();

         if (featuredStreamers != null && featuredStreamers.contains(member.getIdLong())) {
            Role streamerRole = guild.getRoleById(gi.getStreamerRoleId());

            if (streamerRole != null && member.getRoles().contains(streamerRole)) {
               guild.removeRoleFromMember(member, streamerRole).queue();
               logger.info(format("[%s] %s has stopped streaming.",
                   guild,
                   member.getEffectiveName()));
            } else {
               logger.error(format("[%s] Attempted to remove %s from Featured Streamer role, but "
                       + "no role was found for RoleID %s",
                   guild,
                   member.getEffectiveName(),
                   gi.getStreamerRoleId()));
            }
         }
      }
   }

   @Override
   public void onUserUpdateActivityOrder(@NotNull UserUpdateActivityOrderEvent event) {
      GuildInfo gi = GuildInfoUtil.getGuildInfo(event.getGuild().getIdLong());

      if (gi.isGroupingGames()) {
         GameGroupUtil.refreshMemberGameGroups(event.getMember());
      }
   }
}
