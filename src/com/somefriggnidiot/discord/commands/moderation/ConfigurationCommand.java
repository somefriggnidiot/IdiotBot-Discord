package com.somefriggnidiot.discord.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationCommand extends Command {

   private Map<String, String> configHelp = new HashMap<>();

   public ConfigurationCommand() {
      this.name = "configuration";
      this.aliases = new String[]{ "configuration", "config" };
      this.arguments = "<list/help/set/clear> (configName) (configValue)";
      this.help = "Allows setting of server-wide configuration settings for IdiotBot.";
      this.category = new Category("Core");
      this.configHelp = getConfigHelp();
   }

   @Override
   protected void execute(final CommandEvent event) {
      /*
         TODO
         - Allow setting, changing, removing of "Staff Role"
         - Allow setting, changing, removing of "Streaming Role"
         - Allow enabling/disabling of GameGroups.
         - Allow enabling/disabling of Luck Bonus
         - Allow enabling/disabling/setting of Voice XP Multiplier
         - Allow enabling/disabling of XP Tracking
       */

   }

   private Map<String, String> getConfigHelp() {
      Map<String, String> config = new HashMap<>();

      config.put("ownerRole", "**Requires:** ownerRole role once set, \"Administrator\" privileges "
          + "to set initially.\n"
          + "**Accepts:** Role as a mention.\n"
          + "**Description:** Designates a role as having access to all server-level commands, "
          + "including setting of all configuration items.");
      config.put("staffRole", "**Requires:** ownerRole.\n"
          + "**Accepts:** Role as a mention.\n"
          + "**Description:** Designates a role as having access to staff-level commands, "
          + "including setting of most configuration items.");
      config.put("streamingRole", "**Requires:** staffRole.\n"
          + "**Accepts:** Role as a mention.\n"
          + "**Description:** Designates a role as the role to which featured streamers are added"
          + ". For fuether information, see the documentation for \"!featurestreamer\".");
      config.put("voiceMultiplier", "**Requires:** staffRole\n"
          + "**Accepts:** Number, including decimal.\n"
          + "**Description:** Sets an additional multiplier to XP gained by members in voice chat"
          + ". For example, a value of '0.05' sets voice XP at a rate of '1.05' times the normal "
          + "rate.");

      return config;
   }
}
