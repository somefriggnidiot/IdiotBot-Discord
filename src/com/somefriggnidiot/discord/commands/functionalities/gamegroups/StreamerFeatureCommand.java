package com.somefriggnidiot.discord.commands.functionalities.gamegroups;

import static java.lang.String.format;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.command_util.CommandUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamerFeatureCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(this.getClass());

   public StreamerFeatureCommand() {
      this.name = "featurestreamer";
      this.aliases = new String[]{ "streamer", "featuredstreamer", "streamers" };
      this.arguments = "[\"add\"/\"remove\"/\"list\"/\"setrole\"] userMention/roleMention";
      this.category = new Category("Game Groups");
      this.help = "Manages featured streamers.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_WRITE,
          Permission.MANAGE_ROLES};
      this.guildOnly = true;
      this.cooldown = 3;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      GuildInfoUtil giu = new GuildInfoUtil(event.getGuild());
      if (!CommandUtil.checkPermissions(event.getMember(), giu.getStaffRole())) {
         event.reply("You do not have the necessary permissions to use this command.");
         return;
      }

      try {
         String action = event.getMessage().getContentDisplay().split("\\s", 3)[1];
         GuildInfo gi = GuildInfoUtil.getGuildInfo(event.getGuild());

         switch (action) {
            case "add":
               if (checkPermissions(event, gi.getOwnerRoleId())) {
                  add(event);
               } else {
                  event.reply("Adding a featured streamer requires Owner-level permissions.");
               }
               return;
            case "remove":
               if (checkPermissions(event, gi.getOwnerRoleId())) {
                  remove(event);
               } else {
                  event.reply("Removing a featured streamer requires Owner-level permissions.");
               }
               return;
            case "setrole":
               if (checkPermissions(event, gi.getOwnerRoleId())) {
                  setRole(event);
               } else {
                  event.reply("Setting the featured streamer role requires Owner-level "
                      + "permissions.");
               }
               return;
            case "list":
               printLive(event);
               return;
            case "status":
            case "info":
               printInfo(event);
               return;
            default:
               event.reply("Invalid argument(s). Please see !help for proper usage.");
         }
      } catch (IndexOutOfBoundsException iob) {
         printLive(event);
      }
   }

   private void add(CommandEvent event) {
      User streamerMember = event.getMessage().getMentionedUsers().size() > 0 ? event
          .getMessage().getMentionedUsers().get(0) : null;

      if (streamerMember != null) {
         GuildInfoUtil.addStreamerMemberId(event.getGuild().getIdLong(),
             streamerMember.getIdLong());
         logger.info(format("[%s] %s has been added as a featured streamer.",
             event.getGuild(),
             event.getGuild().getMember(streamerMember).getEffectiveName()));
         event.reply(format("%s has been successfully added as a featured streamer.",
             event.getGuild().getMember(streamerMember)));
      } else {
         event.reply("Invalid argument. Please mention the member being added as a featured "
             + "streamer.");
      }
   }

   private void remove(CommandEvent event) {
      GuildInfo gi = GuildInfoUtil.getGuildInfo(event.getGuild());
      User streamerMember = event.getMessage().getMentionedUsers().size() > 0 ? event
          .getMessage().getMentionedUsers().get(0) : null;

      if (streamerMember != null &&
          gi.getStreamerMemberIds().contains(streamerMember.getIdLong())) {
         GuildInfoUtil.removeStreamerMemberId(event.getGuild().getIdLong(),
             streamerMember.getIdLong());
         event.reply(streamerMember.getName() + " has been removed from the featured streamer "
             + "list.");
         logger.info(format("[%s] %s has been removed from the featured streamer list.",
             event.getGuild(),
             event.getGuild().getMember(streamerMember).getEffectiveName()));
      } else {
         event.reply("Invalid argument. Please mention the member being removed as a featured "
             + "streamer");
      }
   }

   private void setRole(CommandEvent event) {
      Role role = event.getMessage().getMentionedRoles().size() > 0 ? event.getMessage()
          .getMentionedRoles().get(0) : null;

      if (role != null) {
         GuildInfoUtil.setStreamerRoleId(event.getGuild().getIdLong(), role.getIdLong());
      } else {
         event.reply("Invalid argument. Please mention the role being set as the featured "
             + "streamer role.");
      }
   }

   private void printInfo(CommandEvent event) {
      Guild guild = event.getGuild();
      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild);
      Role streamerRole = guild.getRoleById(gi.getStreamerRoleId());

      EmbedBuilder eb = new EmbedBuilder()
          .setTitle("Featured Streamers")
          .setDescription("A little extra love for the streamers active in our community. "
              + "Featured streamers will be added to the streamer role and displayed separately "
              + "from the rest of the members when live.")
          .setFooter("Provided to you by IdiotBot. The most idiotic of bots.",
              "http://www.foundinaction.com/wp-content/uploads/2018/08/"
                  + "Neon_600x600_Transparent.png")
          .addField("Streamer Role", streamerRole == null ? "[None Set]" :
              streamerRole.getName(), false)
          .addField("Total Featured Streamers",
              String.valueOf(gi.getStreamerMemberIds().size()), false);

      event.reply(eb.build());
   }

   private void printLive(CommandEvent event) {
      Guild guild = event.getGuild();
      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild);
      List<Long> ids = gi.getStreamerMemberIds();

      //Create base of response.
      EmbedBuilder eb = new EmbedBuilder()
          .setTitle("Featured Streamers")
          .setDescription("A little extra love for the streamers active in our community. "
              + "Featured streamers will be added to the streamer role and displayed separately "
              + "from the rest of the members when live.")
          .setFooter("Provided to you by IdiotBot. The most idiotic of bots.",
              "http://www.foundinaction.com/wp-content/uploads/2018/08/"
                  + "Neon_600x600_Transparent.png");

      //Create fields for response.
      List<Field> unsortedFields = new ArrayList<>();
      if (ids != null && ids.size() > 0) {
         for (Long id : ids) {
            Member member = guild.getMemberById(id);
            Activity streamingActivity = null;
            try {
               streamingActivity = member.getActivities().stream().filter(act -> act
                   .getType().equals(ActivityType.STREAMING)).collect(Collectors.toList()).get(0);
            } catch (NullPointerException npe) {
               logger.debug("No activities for " + member.getEffectiveName());
            } catch (IndexOutOfBoundsException iobe) {
               logger.debug("No streaming activities for " + member.getEffectiveName());
            }
            Boolean isLive = streamingActivity != null &&
                streamingActivity.isRich() &&
                streamingActivity.asRichPresence() != null;
            String memberName = member.getEffectiveName();

            String liveMessage;
            String featureMarker;

            if (isLive) {
               liveMessage = format("[%s](%s)", streamingActivity.asRichPresence().getDetails(),
                   streamingActivity.getUrl());
               featureMarker = streamingActivity.getName();
            } else {
               liveMessage = "[Stream Offline]";
               featureMarker = null;
            }
            String memberTitle = isLive ? format("%s - LIVE [%s]", memberName, featureMarker) :
                memberName;

            unsortedFields.add(new Field(memberTitle, liveMessage, !isLive));
         }
      } else {
         unsortedFields = Collections.singletonList(new Field("No Featured Streamers",
             "This server has not designated any featured streamers at this time.",
             false));
      }

      //Split it into live and offline streams for ordering.
      List<Field> sortedFields = new ArrayList<>();
      if (unsortedFields.size() > 1) {
         List<Field> liveFields = unsortedFields.stream().filter(field -> field.getName().contains
             ("LIVE")).collect(Collectors.toList());
         List<Field> offlineFields = unsortedFields.stream().filter(field -> !field.getName().contains
             ("LIVE")).collect(Collectors.toList());

         //Set "Currently Streaming" header if any are live.
         if (liveFields.size() > 0) {
            Field firstLive = liveFields.remove(0);
            liveFields.add(0, new Field("Currently Streaming\n" + firstLive.getName(),
                firstLive.getValue(), firstLive.isInline()));
         }
         sortedFields.addAll(liveFields);

         //Set "Offline Streams" header if any are offline.
         if (offlineFields.size() > 0) {
            Field firstOffline = offlineFields.remove(0);
            offlineFields.add(0, new Field("Offline Streams\n" + firstOffline.getName(),
                firstOffline.getValue(), firstOffline.isInline()));
         }
         sortedFields.addAll(offlineFields);
      } else {
         sortedFields = unsortedFields;
      }

      sortedFields.forEach(eb::addField);
      event.reply(eb.build());
   }

   private Boolean checkPermissions(CommandEvent event, Long requiredRoleId) {
      Role requiredRole = event.getGuild().getRoleById(requiredRoleId);

      return event.getMember().getRoles().contains(requiredRole);
   }
}
