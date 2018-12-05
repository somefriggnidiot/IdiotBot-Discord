package com.somefriggnidiot.discord.commands.functionalities.xp.xpinfo;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import com.somefriggnidiot.discord.util.XpUtil;
import java.text.DecimalFormat;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowXpCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(ShowXpCommand.class);
   private EmbedBuilder eb;

   public ShowXpCommand() {
      this.name = "showxp";
      this.aliases = new String[]{"myxp", "xp"};
      this.arguments = "(userMention)";
      this.category = new Category("Functionality");
      this.help = "Displays information about your current xp and level on IdiotBot.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ,
          Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};
      this.guildOnly = false;
      this.cooldown = 5;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      DatabaseUser dbu;
      String level;
      Integer xp;
      Integer nextXp;
      Integer guildRank;
      Integer guildRanks;
      DecimalFormat df = new DecimalFormat("###,###");

      if (event.getMessage().getMentionedMembers().size() == 0) {

         try { //Get XP for level
            String msg = event.getArgs();
            Integer xpForLevel = XpUtil.getXpThresholdForLevel(Integer.parseInt(msg));

            event.reply(String.format("Level %s requires %s XP.",
                msg,
               df.format(xpForLevel)));
         } catch (NumberFormatException e) {
            //Get self XP
            dbu = DatabaseUserUtil.getUser(event.getAuthor().getIdLong());
            level = dbu.getLevel() == null ? "0" : dbu.getLevel().toString();
            xp = dbu.getXpMap().get(event.getGuild().getIdLong()) == null ? 0 : dbu.getXpMap().get
                (event.getGuild().getIdLong());
            nextXp = XpUtil.getXpThresholdForLevel(Integer.valueOf(level)+1);
            guildRank = XpUtil.getGuildRank(event.getGuild(), event.getAuthor());
            guildRanks = XpUtil.getGuildLeaderboardSize(event.getGuild());

            eb = new EmbedBuilder()
                .setTitle(event.getAuthor().getName())
                .setFooter("Provided to you by IdiotBot. The most idiotic of bots.",
                    "http://www.foundinaction.com/wp-content/uploads/2018/08/"
                        + "Neon_600x600_Transparent.png")
                .setThumbnail(event.getAuthor().getAvatarUrl())
                .addField("Current Level", level, false)
                .addField("Rank", String.format("%s of %s", guildRank, guildRanks), false)
                .addField("Progress to Next Level", df.format(xp) + " / " + df.format(nextXp),
                    false);
            event.getChannel().sendMessage(eb.build()).queue();
         }
      } else {
         User user = event.getMessage().getMentionedUsers().get(0);
         dbu = DatabaseUserUtil.getUser(user.getIdLong());
         level = dbu.getLevel() == null ? "0" : dbu.getLevel().toString();
         xp = dbu.getXpMap().get(event.getGuild().getIdLong()) == null ? 0 : dbu.getXpMap().get
             (event.getGuild().getIdLong());
         nextXp = XpUtil.getXpThresholdForLevel(Integer.valueOf(level)+1);
         guildRank = XpUtil.getGuildRank(event.getGuild(), user);
         guildRanks = XpUtil.getGuildLeaderboardSize(event.getGuild());

         eb = new EmbedBuilder()
             .setTitle(user.getName())
             .setFooter("Provided to you by IdiotBot. The most idiotic of bots.",
                 "http://www.foundinaction.com/wp-content/uploads/2018/08/"
                     + "Neon_600x600_Transparent.png")
             .setThumbnail(user.getAvatarUrl())
             .addField("Current Level", level, false)
             .addField("Rank", String.format("%s of %s", guildRank, guildRanks), false)
             .addField("Progress to Next Level", df.format(xp) + " / " + df.format(nextXp), false);
         event.getChannel().sendMessage(eb.build()).queue();
      }
   }
}
