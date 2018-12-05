package com.somefriggnidiot.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.lang.String.format;

public class StatusCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(StatusCommand.class);
   private final DecimalFormat df = new DecimalFormat("###,###");

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
      Long ping = event.getGuild().getJDA().getPing();
      Long userCount = event.getGuild().getMembers().stream().filter(member -> !member.getUser
          ().isBot()).count();
      Long botCount = event.getGuild().getMembers().stream().filter(member -> member.getUser()
          .isBot()).count();
//      Integer lifetimeMessages = 0;
//
//      for (TextChannel channel : event.getGuild().getTextChannels()) {
//         MessageHistory history = channel.getHistory();
//         int messages = 0;
//         List<Message> messageList = history.retrievePast(100).complete();
//         messages += messageList.size();
//
//         while (messageList.size() == 100) {
//            messageList = history.retrievePast(100).complete();
//            messages += messageList.size();
//         }
//
//         lifetimeMessages += messages;
//      }

      String voiceMultiplier = String.valueOf(new GuildInfoUtil(event.getGuild().getIdLong())
          .getVoiceXpMultiplier() + 1.0) + "x";
      String voiceMultiplierStatus = gi.isGrantingMessageXp() ? voiceMultiplier : "_Disabled_";
      String xpTrackingStatus = gi.isGrantingMessageXp() ? "Active" : "_Disabled_";
      String groupingGamesStatus = gi.isGroupingGames() ?
          String.format("Active (%s)", gi.getGameGroupMappings().size()) : "_Disabled_";
      String rolesOnLevelsStatus = gi.getRoleLevelMappings().size() > 0 ?
          String.format("Active (%s)", gi.getRoleLevelMappings().size()) : "_Disabled_";
//      String guildLifetimeMessages = df.format(lifetimeMessages);

      EmbedBuilder eb = new EmbedBuilder();
      eb.setTitle(format("%s Information", event.getGuild().getName()));
      eb.setColor(Color.GREEN);//ThreadLocalRandom.current().nextInt(0, 16777216));
      eb.setThumbnail(event.getGuild().getIconUrl());
      eb.addField("**Users**", userCount.toString(), true);
      eb.addField("**Bots**", botCount.toString(), true);
      eb.addField("**Ping**", format("%s ms", ping), true);
      eb.addField("**XP Tracking**", xpTrackingStatus, true);
      eb.addField("**Voice XP Multiplier**", voiceMultiplierStatus, true);
      eb.addField("**Grouping Games**", groupingGamesStatus, true);
      eb.addField("**Roles On Levels**", rolesOnLevelsStatus, true);
//      eb.addField("**Total Server Messages**", guildLifetimeMessages, true);

      event.reply(eb.build());
   }
}
