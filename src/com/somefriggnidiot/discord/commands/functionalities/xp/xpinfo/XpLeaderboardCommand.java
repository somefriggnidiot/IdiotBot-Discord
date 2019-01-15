package com.somefriggnidiot.discord.commands.functionalities.xp.xpinfo;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.util.HighscoreObject;
import com.somefriggnidiot.discord.util.XpUtil;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XpLeaderboardCommand extends Command {

   public XpLeaderboardCommand() {
      this.name = "leaderboard";
      this.aliases = new String[]{"xpleaderboard", "top"};
      this.arguments = "(count)";
      this.category = new Category("Xp Info");
      this.help = "Displays the top XP earners in this guild. Defaults to 10 records, but can be "
          + "overridden.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ,
         Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};
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
         highScoreObjects.add(new HighscoreObject(dbu, xp));
      }

      //Sort the scores.
      List<HighscoreObject> sortedScores;
      sortedScores = highScoreObjects.stream()
          .sorted(Comparator.comparing(HighscoreObject::getXp).reversed())
          .filter(e -> e.getXp() > 0)
          .collect(Collectors.toList());

      String top = ""; //String to be returned as leaderboard.
      int index = (count - 10) < 0 ? 0 : (count - 10); //Set start index to 10 before top, or 0.
      int rank = (count - 10) < 0 ? 1 : (count - 9);
      int startRank = rank;

      while (index < count) {
         try {
            top += String.format("**%s. %s** - Level %s - %s XP\n",
                rank++,
                guild.getMemberById(sortedScores.get(index).getUser().getId()).getEffectiveName(),
                XpUtil.getLevelForXp(sortedScores.get(index).getXp()),
                new DecimalFormat("###,###").format(sortedScores.get(index).getXp()));
            index++;
         } catch (IndexOutOfBoundsException e) {
            rank--;
            break;
         }
      }

      EmbedBuilder eb = new EmbedBuilder()
          .setTitle(String.format("XP Leaderboard - %s to %s - %s",
              startRank,
              rank - 1,
              guild.getName()))
          .setDescription(top);
//          .addField("", top, true);

      event.getChannel().sendMessage(eb.build()).queue();
   }


}
