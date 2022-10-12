package com.somefriggnidiot.discord.commands.functionalities.raffle;

import static java.lang.String.format;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.RaffleObject;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.data_access.util.RaffleUtil;
import com.somefriggnidiot.discord.util.command_util.CommandUtil;
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
      String raffleIdRaw = null;
      String entriesRaw = null;
      try {
         raffleIdRaw = message.split("\\s", 3)[1];
         entriesRaw = message.split("\\s", 3)[2];
      } catch (Exception e) {
         event.reply("Invalid usage. Command requires ID and amount of times you wish to enter "
             + "the raffle. Use `!enterraffle <id> <times>` to enter.");
      }

      Long raffleId;
      Integer entriesAttemptedToPurchase;
      try { //See if we got valid params for raffle ID and entries.
         raffleId = Long.parseLong(raffleIdRaw);
         entriesAttemptedToPurchase = Integer.valueOf(entriesRaw);
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
         Integer entriesPurchased = ru.addEntriesForUser(author, entriesAttemptedToPurchase);
         Integer tokenCost = entriesPurchased * ru.getRaffle().getTicketCost();
         String tokenPlural = tokenCost == 1 ? "token" : "tokens";
         event.reply(format("You have spent %s %s to purchase %s entries.",
             tokenCost,
             tokenPlural,
             entriesPurchased));
      } else { //Required role.
         try {
            requiredRole = guild.getRoleById(requiredRoleId);

            if (guild.getMember(author).getRoles().contains(requiredRole)) {
               //User is a member of the required role.
               Integer costPerEntry = raffle.getTicketCost();
               Integer currentUserEntries = raffle.getEntryMap().getOrDefault(author.getIdLong(),
                   0);
               Integer userTokens = DatabaseUserUtil.getUser(author.getIdLong()).getTokenMap()
                   .get(event.getGuild().getIdLong());
               Integer maxEntries = raffle.getMaxTicketsPerPerson();

               if (entriesAttemptedToPurchase + currentUserEntries > maxEntries) {
                  event.reply(format("You cannot purchase more than %s entries for raffle %s. You"
                          + " currently have %s entries.",
                      maxEntries, raffle.getId(), currentUserEntries));
                  return;
               }

               if ((entriesAttemptedToPurchase * costPerEntry) > userTokens) {
                  event.reply("You cannot afford that many entries.");
               }

               Integer entriesPurchased = ru.addEntriesForUser(author, entriesAttemptedToPurchase);
               Integer tokenCost = entriesPurchased * ru.getRaffle().getTicketCost();
               String tokenPlural = tokenCost == 1 ? "token" : "tokens";
               String entryPlural = entriesPurchased == 1 ? "entry" : "entries";
               event.reply(format("You have spent %s %s to purchase %s %s.",
                   tokenCost,
                   tokenPlural,
                   entriesPurchased,
                   entryPlural));
            } else {
               //User is not a member of the required role.
               event.reply(format("You need to be a member of the %s role to enter this raffle.",
                   requiredRole.getName()));
            }
         } catch (Exception e) {
            //Role cannot be found.
            logger.error("Exception!", e);
            event.reply("The required role for this raffle does not exist!");
            logger.warn(format("[%s] No role found for id %s, required for raffle '%s', "
                + "id %s.",
                guild,
                requiredRoleId,
                raffle.getRaffleName(),
                raffle.getId()));
         }
      }
   }
}
