package com.somefriggnidiot.discord.commands.functionalities.gamegroups;

import static java.lang.String.format;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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
      try {
         String action = event.getMessage().getContentDisplay().split("\\s", 3)[1];

         switch (action) {
            case "add":
               if (checkPermissions(event, null)) add(event); //TODO add command to set staff
               // role, store on GuildInfo.
               return;
            case "remove":
               remove(event);
               return;
            case "setrole":
               setRole(event);
               return;
            case "list":
            case "status":
            case "info":
               printInfo(event);
               return;
            default:
               event.reply("Invalid argument(s). Please see !help for proper usage.");
         }
      } catch (IndexOutOfBoundsException iob) {
         printInfo(event);
      }
   }

   private void add(CommandEvent event) {
      GuildInfo gi = GuildInfoUtil.getGuildInfo(event.getGuild());
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
         logger.info(format("[%s] %s has been removed from the featured streamer list.",
             event.getGuild(),
             event.getGuild().getMember(streamerMember).getEffectiveName()));
      } else {
         event.reply("Invalid argument. Please mention the member being removed as a featured "
             + "streamer");
      }
   }

   private void setRole(CommandEvent event) {
      GuildInfo gi = GuildInfoUtil.getGuildInfo(event.getGuild());
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
      List<Long> ids = gi.getStreamerMemberIds();

      EmbedBuilder eb = new EmbedBuilder()
          .setTitle("Featured Streamers")
          .setDescription("A little extra love for the streamers active in our community. "
              + "Featured streamers will be added to the streamer role and displayed separately "
              + "from the rest of the members when live.")
          .addField("Streamer Role", streamerRole == null ? "[None Set]" : streamerRole.getName
              (), false)
          .setFooter("Provided to you by IdiotBot. The most idiotic of bots.",
              "http://www.foundinaction.com/wp-content/uploads/2018/08/"
                  + "Neon_600x600_Transparent.png");

      if (ids != null && ids.size() > 0) {
         for (Long id : ids) {
            Member member = guild.getMemberById(id);
            Activity streamingActivity = null;
            try {
               streamingActivity = member.getActivities().stream().filter(act -> act
                   .getType().equals(ActivityType.STREAMING)).collect(Collectors.toList()).get(0);
            } catch (NullPointerException npe) {
               logger.info("No activities for " + member.getEffectiveName());
            } catch (IndexOutOfBoundsException iobe) {
               logger.info("No streaming activities for " + member.getEffectiveName());
            }
            String memberName = member.getEffectiveName();
            String liveMessage = streamingActivity == null ? "[Stream Offline]" :
                streamingActivity.getName();

            eb.addField(memberName, liveMessage, true);
         }
      } else {
         eb.addField("No Featured Streamers",
             "This server has not designated any featured streamers at this time.",
             false);
      }

      event.reply(eb.build());
   }

   private Boolean checkPermissions(CommandEvent event, Role requiredRole) {
      if (!event.getMember().getRoles().contains(requiredRole)) {
         event.reply("You do not have the necessary permissions to use this command.");
         return false;
      }

      return true;
   }
}
