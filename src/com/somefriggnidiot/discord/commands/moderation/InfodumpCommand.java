package com.somefriggnidiot.discord.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.core.Main;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfodumpCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(this.getClass());

   public InfodumpCommand() {
      this.name = "infodump";
      this.aliases = new String[]{"dump", "dox"};
      this.arguments = "<guildId>";
      this.category = new Category("Owner");
      this.help = "Dumps all server info into the console log.";
      this.botPermissions = new Permission[]{Permission.ADMINISTRATOR};
      this.guildOnly = false;
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
      this.ownerCommand = true;
   }

   @Override
   protected void execute(final CommandEvent event) {
      String[] args = event.getMessage().getContentDisplay().split("\\s", 2);
      GuildInfoUtil giu = new GuildInfoUtil(Long.valueOf(args[1]));
      Guild guild = Main.jda.getGuildById(args[1]);

      List<TextChannel> channels = guild.getTextChannels();

      for (TextChannel channel : channels) {
         channel.getIterableHistory()
             .forEach(history -> logger.info(history.getContentDisplay()));
      }
   }
}
