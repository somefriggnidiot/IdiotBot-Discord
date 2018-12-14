package com.somefriggnidiot.discord.commands.functionalities.raffle;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.RaffleObject;
import com.somefriggnidiot.discord.data_access.util.RaffleUtil;
import java.util.List;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateRaffleCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(CreateRaffleCommand.class);
   private CommandEvent event;

   public CreateRaffleCommand() {
      this.name = "createraffle";
      this.aliases = new String[]{"makeraffle"};
      this.arguments = "<tokens per ticket> <max tickets per person> "
          + "<minimumRoleMention> <description>";
      this.category = new Category("Raffles");
      this.help = "Creates a raffle that users may enter using tokens, 'purchasing' up to the "
          + "specified maximum in entries.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_WRITE};
      this.guildOnly = true;
      this.requiredRole = "Staff";
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      this.event = event;
      Long guildId = event.getGuild().getIdLong();
      Long requiredRoleId;
      String message = event.getMessage().getContentDisplay();
      String[] messageSplit = message.split("\\s", 5);
      String raffleName;
      Integer ticketCost;
      Integer maxEntriesPerPerson;

      try {
         List<Role> roleMentions = event.getMessage().getMentionedRoles();
         requiredRoleId = roleMentions.size() > 0 ? roleMentions.get(0).getIdLong() : 0L;
         ticketCost = checkTicketcost(messageSplit[1]);
         maxEntriesPerPerson = checkMaxEntries(messageSplit[2]);
         raffleName = checkRaffleName(messageSplit[4]);
      } catch (Exception e) {
         e.printStackTrace();
         return;
      }

      RaffleObject raffleObject = RaffleUtil
          .createRaffle(guildId, raffleName, requiredRoleId, ticketCost, maxEntriesPerPerson);

      logger.info(String.format("[%s] %s has created a raffle: %s - %s tokens per ticket - %s "
          + "tickets maximum per person. Minimum role: %s (%s)",
          event.getGuild(),
          event.getAuthor().getName(),
          raffleName,
          ticketCost,
          maxEntriesPerPerson,
          event.getGuild().getRoleById(requiredRoleId).getName(),
          requiredRoleId));

      EmbedBuilder eb = new EmbedBuilder()
          .setDescription("Created by " + event.getAuthor().getName())
          .setTitle("**New Raffle: **" + raffleObject.getRaffleName())
          .addField("Ticket Cost", String.format("%s Tokens", raffleObject.getTicketCost()), false)
          .addField("Max Tickets / User", raffleObject.getMaxTicketsPerPerson().toString(), false);

      event.reply(eb.build());
   }

   private Integer checkTicketcost(String ticketCost) {
      try {
         return Integer.parseInt(ticketCost);
      } catch (NumberFormatException e) {
         event.reactError();
         event.reply("Ticket cost must be a number.");
         throw e;
      }
   }

   private Integer checkMaxEntries(String maxEntries) {
      try {
         return Integer.parseInt(maxEntries);
      } catch (NumberFormatException e) {
         event.reactError();
         event.reply("Max tickets per person must be a number.");
         throw e;
      }
   }

   private String checkRaffleName(String raffleName) {
      try {
         return raffleName;
      } catch (IndexOutOfBoundsException e) {
         event.reactError();
         event.reply("Argument missing: Raffle name.");
         throw e;
      }
   }
}
