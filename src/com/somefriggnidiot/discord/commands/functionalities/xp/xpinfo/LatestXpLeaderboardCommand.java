package com.somefriggnidiot.discord.commands.functionalities.xp.xpinfo;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.util.HighscoreObject;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

public class LatestXpLeaderboardCommand extends Command {

   public LatestXpLeaderboardCommand() {
      this.name = "latest";
      this.aliases = new String[]{"latest-top", "recent"};
      this.category = new Category("Xp Info");
      this.help = "Displays the top XP earners in this guild who have earned XP in the past week..";
      this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE, Permission.MESSAGE_READ};
      this.guildOnly = true;
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      //Set the X in "top X"
      Integer count;
      try {
         count = Integer.parseInt(event.getMessage()
             .getContentDisplay().split("\\s", 2)[1]);
      } catch (IndexOutOfBoundsException ex) {
         count = 10;
      }

      Guild guild = event.getGuild();
      List<Member> members = event.getGuild().getMembers();
      List<DatabaseUser> dbus = new ArrayList<>();
      List<HighscoreObject> highScoreObjects = new ArrayList<>();

      //Get DBUs for all members.
      for (Member member : members) {
         dbus.add(DatabaseUserUtil.getUser(member.getUser().getIdLong()));
      }

      //Convert DBUs into HighscoreObjects if XP > 0
      for (DatabaseUser dbu : dbus) {
         Integer xp = dbu.getXpMap().get(guild.getIdLong()) == null ? 0 : dbu.getXpMap()
             .get(guild.getIdLong());
         highScoreObjects.add(new HighscoreObject(dbu, xp, dbu.getLatestGain()));
      }

      highScoreObjects = highScoreObjects.stream()
          .filter(e -> e.getLastGain()
              .after(Timestamp.valueOf(LocalDateTime.now().minusDays(7))))
          .collect(Collectors.toList());

      List<HighscoreObject> sortedScores = highScoreObjects.stream()
          .sorted(Comparator.comparing(HighscoreObject::getLastGain).reversed())
          .filter(e -> e.getXp() > 0)
          .collect(Collectors.toList());

      String top = ""; //String to be returned as leaderboard.
      int index = (count - 10) < 0 ? 0 : (count - 10); //Set start index to 10 before top, or 0.
      int rank = (count - 10) < 0 ? 1 : (count - 9);
      int startRank = rank;
      SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d, hh:mm a");

      while (index < count) {
         try {
            top += String.format("**%s. %s** - %s\n",
                rank++,
                guild.getMemberById(sortedScores.get(index).getUser().getId()).getEffectiveName(),
                format.format(sortedScores.get(index).getLastGain()));
            index++;
         } catch (IndexOutOfBoundsException e) {
            rank--;
            break;
         }
      }

      EmbedBuilder eb = new EmbedBuilder()
          .setTitle(String.format("Latest XP Grinders - %s to %s - %s",
              startRank,
              rank - 1,
              guild.getName()))
          .setDescription(top);

      event.getChannel().sendMessage(eb.build()).queue();
   }

}
