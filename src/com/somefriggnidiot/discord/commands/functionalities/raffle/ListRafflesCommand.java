package com.somefriggnidiot.discord.commands.functionalities.raffle;

import static java.lang.String.format;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.RaffleObject;
import com.somefriggnidiot.discord.data_access.util.RaffleUtil;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;

public class ListRafflesCommand extends Command {

   public ListRafflesCommand() {
      this.name = "listraffles";
      this.aliases = new String[]{"raffles", "rafflelist"};
      this.category = new Category("Raffles");
      this.help = "Displays a list of active raffles for this guild.";
      this.botPermissions = new Permission[] {Permission.MESSAGE_EMBED_LINKS,
      Permission.MESSAGE_READ, Permission.MESSAGE_WRITE};
      this.guildOnly = true;
      this.cooldown = 5;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(CommandEvent event) {
      List<RaffleObject> raffles = RaffleUtil.getRaffles(true).stream().filter(raffle -> raffle
          .getGuildId() == event.getGuild().getIdLong()).collect(Collectors.toList());
      //TODO make this not super inefficient.
      Guild guild = event.getGuild();
      EmbedBuilder eb = new EmbedBuilder()
          .setTitle("Active Raffles")
          .setDescription(format("The following raffles are active in %s.",
              guild.getName()));

      if (raffles.size() < 1) {
         event.reply("There are no active raffles right now.");
         return;
      }

      for (RaffleObject raffle : raffles) {
         String tokenPlural = raffle.getTicketCost() > 1 ? "tokens" : "token";
         String raffleListing = format("**(ID %s)** Name: %s", raffle.getId(), raffle
             .getRaffleName());
         String raffleDetails = format("**Ticket Cost:** %s %s\n"
             + "**Max Tickets:** %s per user",
             raffle.getTicketCost(),
             tokenPlural,
             raffle.getMaxTicketsPerPerson());

         eb.addField(raffleListing, raffleDetails, false);
      }

      event.reply(eb.build());
   }
}
