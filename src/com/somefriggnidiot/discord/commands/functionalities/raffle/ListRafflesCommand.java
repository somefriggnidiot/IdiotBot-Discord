package com.somefriggnidiot.discord.commands.functionalities.raffle;

import static java.lang.String.format;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.RaffleObject;
import com.somefriggnidiot.discord.data_access.util.RaffleUtil;
import java.util.List;
import java.util.Map;
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
      //Set the X in "top X"
//      Integer displayCount;
//      try {
//         displayCount = Integer.parseInt(event.getMessage()
//             .getContentDisplay().split("\\s", 2)[1]);
//      } catch (IndexOutOfBoundsException ex) {
//         displayCount = 10;
//      } catch (NumberFormatException nf) {
//         event.reply("Invalid request. Arguments must be positive integer.");
//         return;
//      }

      List<RaffleObject> raffles = RaffleUtil.getRaffles(true).stream().filter(raffle -> raffle
          .getGuildId() == event.getGuild().getIdLong()).collect(Collectors.toList());
      //TODO make this not super inefficient.
      Guild guild = event.getGuild();
      EmbedBuilder eb = new EmbedBuilder()
          .setTitle("Active Raffles")
          .setDescription(format("The following raffles are active in %s. \n`!enterraffle id "
                  + "times` to enter.",
              guild.getName()))
          .setFooter("Provided to you by IdiotBot. The most idiotic of bots.",
              "http://www.foundinaction.com/wp-content/uploads/2018/08/"
                  + "Neon_600x600_Transparent.png");

      //If no active raffles, exit.
      if (raffles.size() < 1) {
         event.reply("There are no active raffles right now.");
         return;
      }

      int displayed = 0;
      for (RaffleObject raffle : raffles) {
         String tokenPlural = raffle.getTicketCost() > 1 ? "tokens" : "token";
         String raffleListing = format("__*[ID: %s]* **%s**__", raffle.getId(), raffle
             .getRaffleName());
         Map<Long, Integer> entries = raffle.getEntryMap();
         Integer entrantCount = entries.keySet().size();
         Integer ticketCount = 0;

         for (Integer count : entries.values()) {
            ticketCount += count;
         }

         String raffleDetails = format("**Ticket Cost:** %s %s\n"
             + "**Max Tickets:** %s per user\n"
             + "**Current Entries / Entrants:** %s / %s",
             raffle.getTicketCost(),
             tokenPlural,
             raffle.getMaxTicketsPerPerson(),
             entrantCount,
             ticketCount);

         eb.addField(raffleListing, raffleDetails, false);

         if (++displayed > 7) {
            eb.addField("And more!", "There are more raffles than able to be displayed right "
                + "now. Consider closing some out before opening more!", false);
            break;
         }
      }

      event.reply(eb.build());
   }
}
