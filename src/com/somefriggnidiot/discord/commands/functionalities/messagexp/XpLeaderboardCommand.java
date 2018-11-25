package com.somefriggnidiot.discord.commands.functionalities.messagexp;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
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

   private final Logger logger = LoggerFactory.getLogger(XpLeaderboardCommand.class);

   public XpLeaderboardCommand() {
      this.name = "leaderboard";
      this.aliases = new String[]{"xpleaderboard", "top"};
      this.arguments = "(count)";
      this.category = new Category("Functionality");
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
      List<HighScoreObject> highScoreObjects = new ArrayList<>();

      for (Member member : members) {
         dbus.add(DatabaseUserUtil.getUser(member.getUser().getIdLong()));
      }

      for (DatabaseUser dbu : dbus) {
         Integer xp = dbu.getXpMap().get(guild.getIdLong()) == null ? 0 : dbu.getXpMap()
             .get(guild.getIdLong());
         highScoreObjects.add(new HighScoreObject(dbu, xp));
      }

      List<HighScoreObject> sortedScores = null;
      try {
         sortedScores = highScoreObjects.stream()
             .sorted(Comparator.comparing(HighScoreObject::getXp).reversed())
             .filter(e -> e.getXp() > 0)
             .collect(Collectors.toList()).subList(0, count);
      } catch (IndexOutOfBoundsException | NullPointerException e) {
         sortedScores = highScoreObjects.stream()
             .sorted(Comparator.comparing(HighScoreObject::getXp).reversed())
             .filter(p -> p.getXp() > 0)
             .collect(Collectors.toList());
      }

      String top = "";
      int position = 1;
      for (HighScoreObject user : sortedScores) {
         top += String.format("**%s. %s** - Level %s - %s XP\n",
             position++,
             guild.getMemberById(user.getUser().getId()).getEffectiveName(),
             user.getUser().getLevel(),
             new DecimalFormat("###,###").format(user.getXp()));
      }

      EmbedBuilder eb = new EmbedBuilder()
          .setTitle("Top " + count + " - " + guild.getName())
          .addField("Top", top, true);

      event.getChannel().sendMessage(eb.build()).queue();
   }


}

class HighScoreObject {
   DatabaseUser user;
   Integer xp;

   HighScoreObject(DatabaseUser user, Integer xp) {
      this.user = user;
      this.xp = xp;
   }

   public Integer getXp() {
      return xp;
   }

   public DatabaseUser getUser() {
      return user;
   }
}
