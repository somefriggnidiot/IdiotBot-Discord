package com.somefriggnidiot.discord.commands.functionalities.core;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.CommandUtilResponse;
import com.somefriggnidiot.discord.util.CommandUtilResponse.CommandResponseMessage;
import com.somefriggnidiot.discord.util.command_util.ConfigurationCommandUtil;
import java.util.HashMap;
import java.util.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(this.getClass());

   public ConfigurationCommand() {
      this.name = "configuration";
      this.aliases = new String[]{ "configuration", "config" };
      this.arguments = "<list/help/set/clear> (configName) (configValue)";
      this.help = "Allows setting of server-wide configuration settings for IdiotBot. Use `!help "
          + "configKey` for additional info on each configurable item.";
      this.category = new Category("Core");
      this.ownerCommand = true;
      this.guildOnly = true;
   }

   @Override
   protected void execute(final CommandEvent event) {
      /*
         TODO
         - Allow enabling/disabling of GameGroups.
         - Allow enabling/disabling of Luck Bonus
       */

      String[] params = event.getMessage().getContentDisplay().split("\\s", 4);

      String action;
      try {
         action = params[1];
      } catch (IndexOutOfBoundsException e) {
         printConfig(event);
         return;
      }

      switch (action) {
         case "set":
            try {
               String configItem = params[2];
               String configValue = params[3];

               handleResult(event,
                   setConfig(event.getGuild(), configItem, configValue,
                       event.getMember(), event.getMessage()));
            } catch (IndexOutOfBoundsException iobe) {
               event.reply("Invalid argument(s). Please see !help for proper usage.");
            }
            return;
         case "unset":
         case "clear":
            try {
               String configItem = params[2];

               event.reply("This functionality has not yet been implemented.");

               //TODO unsetConfig(configItem, event.getMember());
            } catch (IndexOutOfBoundsException iobe) {
               event.reply("Invalid argument(s). You must specify the exact name of the "
                   + "configuration field being cleared.");
            }
            return;
         case "help":
            try {
               String configItem = params[2];
               event.reply(getHelpMessage(configItem));
            } catch (IndexOutOfBoundsException iobe) {
               event.reply("Invalid argument(s). Please see !help for proper usage.");
            }
            return;
         case "list":
            printConfig(event);
            return;
         default:
            return;
      }
   }

   private CommandUtilResponse setConfig(Guild guild, String configItem, String configValue,
       Member author, Message message) {
      String validatedKey = validateConfig(configItem);
      ConfigurationCommandUtil ccUtil = new ConfigurationCommandUtil(guild);

      if (validatedKey.isEmpty()) {
         return new CommandUtilResponse(false, CommandResponseMessage.MISSING_ARGS);
      } else {
         switch(validatedKey) {
            case "ownerRole":
               return ccUtil.setOwnerRole(author, message.getMentionedRoles().get(0));
            case "staffRole":
               return ccUtil.setStaffRole(author, message.getMentionedRoles().get(0));
            case "guestRole":
               return ccUtil.setGuestRole(author, message.getMentionedRoles().get(0));
            case "streamingRole":
               return ccUtil.setStreamerRole(author, message.getMentionedRoles().get(0));
            case "voiceMultiplier":
               Double multiplier = Double.parseDouble(configValue);
               return ccUtil.setVoiceMultiplier(author, multiplier);
            case "botTextChannel":
               return ccUtil.setBotTextChannel(author, message.getMentionedChannels().get(0));
            case "xpEnabled":
               return ccUtil.setXpEnabled(author, configValue);
            case "xpDegrades":
               return ccUtil.setXpDegrades(author, configValue);
            case "xpDegradeValue":
               return ccUtil.setXpDegradeValue(author, configValue);
            default:
               return new CommandUtilResponse(false, CommandResponseMessage.INVALID_ARG);
         }
      }
   }

   private String validateConfig(String potentialKey) {
      return getConfigHelp().containsKey(potentialKey) ? potentialKey : "";
   }

   private void printConfig(CommandEvent event) {
      Guild guild = event.getGuild();
      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild);
      EmbedBuilder eb = new EmbedBuilder()
          .setTitle("Server Configuration")
          .setDescription("Use `!config set <configKey> <configValue>` to update fields, `!config"
              + " clear <configKey>` to reset fields, and `!config help <configKey>` to learn "
              + "more about a specific config field.")
          .setFooter("Provided to you by IdiotBot. The most idiotic of bots.",
              "http://www.foundinaction.com/wp-content/uploads/2018/08/"
                  + "Neon_600x600_Transparent.png");

      Role ownerRole = guild.getRoleById(gi.getOwnerRoleId());
      eb.addField("ownerRole",
          ownerRole != null ? ownerRole.getAsMention() : "Not Set", true);

      Role staffRole = guild.getRoleById(gi.getModeratorRoleId());
      eb.addField("staffRole",
          staffRole != null ? staffRole.getAsMention() : "Not Set", true);

      Role guestRole = guild.getRoleById(gi.getGuestRoleId());
      eb.addField("guestRole",
          guestRole != null ? guestRole.getAsMention() : "Not Set", true);

      TextChannel textChannel = guild.getTextChannelById(gi.getBotTextChannelId());
      eb.addField("botTextChannel",
          textChannel != null ? textChannel.getAsMention() : "Not Set", true);

      Role streamingRole = guild.getRoleById(gi.getStreamerRoleId());
      eb.addField("streamingRole",
          streamingRole != null ? streamingRole.getAsMention() : "Not Set", true);

      eb.addField("xpEnabled", String.valueOf(gi.isGrantingMessageXp()), true);

      eb.addField("xpDegrades", String.valueOf(gi.isDegradingXp()), true);

      eb.addField("xpDegradeValue", String.valueOf(gi.getXpDegradeAmount()), true);

      eb.addField("voiceMultiplier", String.valueOf(gi.getVoiceXpMultiplier()), true);

      event.reply(eb.build());
   }

   private String getHelpMessage(String configItem) {
      return getConfigHelp().getOrDefault(configItem, "Unknown configuration key.");
   }

   private Map<String, String> getConfigHelp() {
      Map<String, String> config = new HashMap<>();

      config.put("ownerRole", "**Requires:** ownerRole role once set, \"Administrator\" privileges "
          + "to set initially.\n"
          + "**Accepts:** Role as a mention.\n"
          + "**Description:** Designates a role as having access to all server-level commands, "
          + "including setting of all configuration items.");
      config.put("botTextChannel", "**Requires:** staffRole\n"
          + "**Accepts:** Text channel as a mention.\n"
          + "**Description:** Designates the channel where messages from this bot will go by "
          + "default. The bot will respond to text messages in the channel where they were "
          + "triggered. Messages such as XP level-ups will be sent to this channel.");
      config.put("staffRole", "**Requires:** ownerRole.\n"
          + "**Accepts:** Role as a mention.\n"
          + "**Description:** Designates a role as having access to staff-level commands, "
          + "including setting of most configuration items.");
      config.put("guestRole", "**Requires:** ownerRole\n"
          + "**Accepts:** Role as a mention.\n"
          + "**Description:** Designates a role as the default `Guest` role of the server. If "
          + "configured, this role is automatically assigned to all users upon joining the server, "
          + "and may be removed by staff by using `!allow <userMention>`.");
      config.put("streamingRole", "**Requires:** staffRole.\n"
          + "**Accepts:** Role as a mention.\n"
          + "**Description:** Designates a role as the role to which featured streamers are added"
          + ". For further information, see the documentation for \"!featurestreamer\".");
      config.put("voiceMultiplier", "**Requires:** staffRole\n"
          + "**Accepts:** Number, including decimal.\n"
          + "**Description:** Sets an additional multiplier to XP gained by members in voice chat"
          + ". For example, a value of '0.05' sets voice XP at a rate of '1.05' times the normal "
          + "rate.");
      config.put("xpEnabled", "**Requires:** staffRole\n"
          + "**Accepts:** true/false\n"
          + "**Description:** Enables or disables the XP system accordingly.");
      config.put("xpDegrades", "**Requires:** ownerRole\n"
          + "**Accepts:** true/false\n"
          + "**Description:** Enables or disables XP degradation. When enabled, user XP will "
          + "degrade daily by the value configured for `xpDegradeValue`.");
      config.put("xpDegradeValue", "**Requires:** ownerRole\n"
          + "**Accepts:** Positive integers. Special values: \"LINEAR\", \"PROGRESSIVE\"\n"
          + "**Description:** The amount of XP deducted from all server members on a daily basis. "
          + "Only applies when `xpDegrades` is `true`. \n"
          + "**Special Values:** \n"
          + "`LINEAR` removes a base of 10xp, plus an additional 10 xp per level for the user.\n"
          + "`PROGRESSIVE` acts the same as LINEAR, but with an additional 10% removed for each "
          + "day the user has been inactive.");

      return config;
   }

   private void handleResult(CommandEvent event, CommandUtilResponse response) {
      CommandResponseMessage message = response.getCommandResponseMessage();

      if (message != null) {
         event.reply(message.info);
      }
   }
}
