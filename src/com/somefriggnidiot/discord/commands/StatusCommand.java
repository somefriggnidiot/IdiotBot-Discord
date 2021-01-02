package com.somefriggnidiot.discord.commands;

import static java.lang.String.format;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.data_access.util.RaffleUtil;
import java.awt.Color;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

public class StatusCommand extends Command {

   public StatusCommand() {
      this.name = "status";
      this.aliases = new String[]{"about", "info"};
      this.arguments = "";
      this.help = "Displays various information about the server and the systems the bot has "
          + "active.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ,
          Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};
      this.guildOnly = true;
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      GuildInfo gi = GuildInfoUtil.getGuildInfo(event.getGuild().getIdLong());
      Long ping = event.getGuild().getJDA().getGatewayPing();
      Long userCount = event.getGuild().getMembers().stream().filter(member -> !member.getUser
          ().isBot()).count();
      Long botCount = event.getGuild().getMembers().stream().filter(member -> member.getUser()
          .isBot()).count();
      Long activeRaffles = RaffleUtil.getRaffles(true).stream().filter(raffle -> raffle
          .getGuildId() == event.getGuild().getIdLong()).count();
      Long totalRaffles = activeRaffles + RaffleUtil.getRaffles(false).stream().filter(raffle ->
          raffle.getGuildId() == event.getGuild().getIdLong()).count();

      String voiceMultiplier = String.valueOf(new GuildInfoUtil(event.getGuild().getIdLong())
          .getVoiceXpMultiplier() + 1.0) + "x";
      String voiceMultiplierStatus = gi.isGrantingMessageXp() ? voiceMultiplier : "_Disabled_";
      String xpTrackingStatus = gi.isGrantingMessageXp() ? "Active" : "_Disabled_";
      String xpLuckBonusStatus = gi.luckBonusActive() ? "Active" : "_Disabled_";
      String groupingGamesStatus = gi.isGroupingGames() ?
          String.format("Active (%s)", gi.getGameGroupMappings().size()) : "_Disabled_";
      String rolesOnLevelsStatus = gi.getRoleLevelMappings().size() > 0 ?
          String.format("Active (%s)", gi.getRoleLevelMappings().size()) : "_Disabled_";
      String rafflesString = format("%s (%s)", totalRaffles, activeRaffles);

      EmbedBuilder eb = new EmbedBuilder();
      eb.setTitle(format("%s Information", event.getGuild().getName()));
      eb.setColor(Color.GREEN);
      eb.setThumbnail(event.getGuild().getIconUrl());
      eb.setFooter("Provided to you by IdiotBot. The most idiotic of bots.",
          "http://www.foundinaction.com/wp-content/uploads/2018/08/"
              + "Neon_600x600_Transparent.png");
      eb.addField("**Users**", userCount.toString(), true);
      eb.addField("**Bots**", botCount.toString(), true);
      eb.addField("**Ping**", format("%s ms", ping), true);
      eb.addField("**XP Tracking**", xpTrackingStatus, true);
      eb.addField("**Luck Bonus**", xpLuckBonusStatus, true);
      eb.addField("**Voice XP Multiplier**", voiceMultiplierStatus, true);
      eb.addField("**Grouping Games**", groupingGamesStatus, true);
      eb.addField("**Roles On Levels**", rolesOnLevelsStatus, true);
      eb.addField("**Raffles (Active)**", rafflesString, true);

      event.reply(eb.build());
   }
}
