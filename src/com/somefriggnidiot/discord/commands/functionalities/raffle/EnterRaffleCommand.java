package com.somefriggnidiot.discord.commands.functionalities.raffle;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.RaffleObject;
import com.somefriggnidiot.discord.data_access.util.RaffleUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnterRaffleCommand extends Command {

   private Logger logger = LoggerFactory.getLogger(EnterRaffleCommand.class);

   public EnterRaffleCommand() {
      this.name = "enterraffle";
      this.aliases = new String[]{"joinraffle", "buytickets"};
      this.arguments = "<raffleId> <entries>";
      this.category = new Category("Raffles");
      this.help = "Purchases tickets/entries for a raffle. If you cannot afford the amount of "
          + "tickets designated, the maximum ammount affordable will be purchased.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE, Permission.MESSAGE_READ};
      this.guildOnly = true;
      this.cooldown = 5;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      Guild guild = event.getGuild();
      User author = event.getAuthor();
      String message = event.getMessage().getContentDisplay();
      String raffleIdRaw = message.split("\\s", 3)[1];
      String entriesRaw = message.split("\\s", 3)[2];
      Long raffleId;
      Integer entries;

      try { //See if we got valid params for raffle ID and entries.
         raffleId = Long.parseLong(raffleIdRaw);
         entries = Integer.valueOf(entriesRaw);
      } catch(Exception e) {
         event.reply("Invalid number provided for raffle ID or entries.");
         return;
      }

      RaffleUtil ru;
      try {
         ru = new RaffleUtil(raffleId);
      } catch (Exception e) {
         event.reply("Invalid raffle ID. Use `!raffles` to see active raffles.");
         return;
      }

      if (ru == null || !ru.getRaffle().isActive()) {
         event.reply("No raffle is active with that ID.");
         return;
      }

      RaffleObject raffle = ru.getRaffle();
      Long requiredRoleId = raffle.getRequiredRoleId();
      Role requiredRole;

      if (requiredRoleId == 0L) {
         //No required role, just purchase.
         Integer entriesPurchased = ru.addEntriesForUser(author, entries);
         Integer tokenCost = entriesPurchased * ru.getRaffle().getTicketCost();
         String tokenPlural = tokenCost == 1 ? "token" : "tokens";
         event.reply(String.format("You have spent %s %s to purchase %s entries.",
             tokenCost,
             tokenPlural,
             entriesPurchased));
      } else { //Required role.
         try {
            requiredRole = guild.getRoleById(requiredRoleId);

            if (guild.getMember(author).getRoles().contains(requiredRole)) {
               //User is a member of the required role.
               Integer entriesPurchased = ru.addEntriesForUser(author, entries);
               Integer tokenCost = entriesPurchased * ru.getRaffle().getTicketCost();
               String tokenPlural = tokenCost == 1 ? "token" : "tokens";
               event.reply(String.format("You have spent %s %s to purchase %s entries.",
                   tokenCost,
                   tokenPlural,
                   entriesPurchased));
            } else {
               //User is not a member of the required role.
               event.reply(String.format("You need a member of the %s role to enter this raffle.",
                   requiredRole.getName()));
            }
         } catch (Exception e) {
            //Role cannot be found.
            event.reply("The required role for this raffle does not exist!");
            logger.warn(String.format("[%s] No role found for id %s, required for raffle '%s', "
                + "id %s.",
                guild,
                requiredRoleId,
                raffle.getRaffleName(),
                raffle.getId()));
         }
      }
   }
}
