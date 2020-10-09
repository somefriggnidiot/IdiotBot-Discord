package com.somefriggnidiot.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.util.XpUtil;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;

public class ProfileCommand extends Command {

   public ProfileCommand() {
      this.name = "profile";
      this.aliases = new String[]{"me", "myprofile"};
      this.arguments = "(userMention)";
      this.help = "Displays information about your activity in the guild.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS,
         Permission.MESSAGE_READ, Permission.MESSAGE_WRITE};
      this.guildOnly = true;
      this.cooldown = 5;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      EmbedBuilder eb;
      DatabaseUser dbu;
      String level;
      Integer xp;
      Integer nextXp;
      Integer tokens;
      Integer guildRank;
      Integer guildRanks;
      Timestamp created;
      Timestamp lastXpGain;
      DecimalFormat df = new DecimalFormat("###,###");
      SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, hh:mm a");

      if (event.getMessage().getMentionedUsers().size() == 0) {
         dbu = DatabaseUserUtil.getUser(event.getAuthor().getIdLong());
         level = new DatabaseUserUtil(event.getAuthor().getIdLong())
             .getLevel(event.getGuild().getIdLong()).toString();
         xp = dbu.getXpMap().get(event.getGuild().getIdLong()) == null ? 0 : dbu.getXpMap().get
             (event.getGuild().getIdLong());
         nextXp = XpUtil.getXpThresholdForLevel(Integer.valueOf(level)+1);
         tokens = dbu.getTokenMap().get(event.getGuild().getIdLong()) == null ? 0 : dbu
             .getTokenMap().get(event.getGuild().getIdLong());
         guildRank = XpUtil.getGuildRank(event.getGuild(), event.getAuthor());
         guildRanks = XpUtil.getGuildLeaderboardSize(event.getGuild());
         created = Timestamp.valueOf(event.getGuild().getMember(event.getAuthor())
             .getJoinDate().toLocalDateTime());
         lastXpGain = dbu.getLatestGain();

         eb = new EmbedBuilder()
             .setTitle(String.format("%s - %s",
                event.getAuthor().getName(),
                event.getGuild().getName()))
             .setFooter("Provided to you by IdiotBot. The most idiotic of bots.",
                 "http://www.foundinaction.com/wp-content/uploads/2018/08/"
                     + "Neon_600x600_Transparent.png")
             .setThumbnail(event.getAuthor().getAvatarUrl())
             .addField("Current Level", level, true)
             .addField("Rank", String.format("%s of %s", guildRank, guildRanks), true)
             .addField("Progress to Next Level",
                 String.format("%s / %s XP", df.format(xp), df.format(nextXp)), true)
             .addField("Tokens", tokens.toString(), true)
             .addField("Joined", dateFormat.format(created), true)
             .addField("Last Active", dateFormat.format(lastXpGain), true);

         event.getChannel().sendMessage(eb.build()).queue();
      } else {
         User user = event.getMessage().getMentionedUsers().get(0);
         dbu = DatabaseUserUtil.getUser(user.getIdLong());
         level = new DatabaseUserUtil(user.getIdLong())
             .getLevel(event.getGuild().getIdLong()).toString();
         xp = dbu.getXpMap().get(event.getGuild().getIdLong()) == null ? 0 : dbu.getXpMap().get
             (event.getGuild().getIdLong());
         nextXp = XpUtil.getXpThresholdForLevel(Integer.valueOf(level)+1);
         tokens = dbu.getTokenMap().get(event.getGuild().getIdLong()) == null ? 0 : dbu
             .getTokenMap().get(event.getGuild().getIdLong());
         guildRank = XpUtil.getGuildRank(event.getGuild(), user);
         guildRanks = XpUtil.getGuildLeaderboardSize(event.getGuild());
         created = Timestamp.valueOf(event.getGuild().getMember(user).getJoinDate()
             .toLocalDateTime());
         lastXpGain = dbu.getLatestGain();

         eb = new EmbedBuilder()
             .setTitle(String.format("%s - %s",
                 user.getName(),
                 event.getGuild().getName()))
             .setFooter("Provided to you by IdiotBot. The most idiotic of bots.",
                 "http://www.foundinaction.com/wp-content/uploads/2018/08/"
                     + "Neon_600x600_Transparent.png")
             .setThumbnail(user.getAvatarUrl())
             .addField("Current Level", level, true)
             .addField("Rank", String.format("%s of %s", guildRank, guildRanks), true)
             .addField("Progress to Next Level",
                 String.format("%s / %s XP", df.format(xp), df.format(nextXp)), true)
             .addField("Tokens", tokens.toString(), true)
             .addField("Joined", dateFormat.format(created), true)
             .addField("Last Active", dateFormat.format(lastXpGain), true);;

         event.getChannel().sendMessage(eb.build()).queue();
      }
   }
}
