package com.somefriggnidiot.discord.commands.functionalities.raffle;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.util.RaffleUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloseRaffleCommand extends Command {

   private Logger logger = LoggerFactory.getLogger(this.getClass());

   public CloseRaffleCommand() {
      this.name = "closeraffle";
      this.aliases = new String[]{"endraffle"};
      this.arguments = "<raffleId>";
      this.category = new Category("Raffles");
      this.help = "Permanently closes a raffle";
      this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE, Permission.MESSAGE_READ};
      this.guildOnly = true;
      this.requiredRole = "Staff";
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      Guild guild = event.getGuild();
      String idRaw = event.getMessage().getContentDisplay().split("\\s", 2)[1];
      Long id = Long.parseLong(idRaw);

      RaffleUtil ru = new RaffleUtil(id);
      ru.closeRaffle();

      event.reply(String.format("Raffle %s (\"%s\") completed.",
          ru.getRaffleId(),
          ru.getRaffle().getRaffleName()));

      logger.info("[%s] %s closed raffle %s (\"%s\").",
          guild,
          event.getAuthor().getName(),
          ru.getRaffleId(),
          ru.getRaffle().getRaffleName());
   }
}
