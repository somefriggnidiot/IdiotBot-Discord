package com.somefriggnidiot.discord.commands.functionalities.raffle;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.util.RaffleUtil;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;

public class DrawRaffleCommand extends Command {

   public DrawRaffleCommand() {
      this.name = "drawraffle";
      this.aliases = new String[]{"raffledraw", "raffle draw"};
      this.arguments = "<raffleId> <drawings> <userCanWinMultiple|true/false>";
      this.category = new Category("Raffles");
      this.help = "Closes a raffle and draws the winners. If users can win multiple times, only "
          + "the single winning entry will be removed each draw. If users cannot win multiple "
          + "times, all entries for a user will be removed upon their first winning draw.\n"
          + "Ex. `!drawraffle 1 5 true`";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_WRITE,
          Permission.MESSAGE_EMBED_LINKS};
      this.guildOnly = true;
      this.requiredRole = "Staff";
      this.cooldown = 10;
      this.cooldownScope = CooldownScope.GUILD;
   }

   @Override
   protected void execute(final CommandEvent event) {
      Guild guild = event.getGuild();
      String message = event.getMessage().getContentDisplay();
      String[] messageSplit = message.split("\\s", 4);
      String raffleIdRaw;
      String drawingsRaw;
      String userCanWinMultipleRaw;
      Long raffleId;
      Integer drawings;
      Boolean userCanWinMultiple;

      try {
         raffleIdRaw = messageSplit[1];
         drawingsRaw = messageSplit[2];
         userCanWinMultipleRaw = messageSplit[3];
      } catch (Exception e) {
         e.printStackTrace();
         event.reply("Not enough params provided.");
         return;
      }

      try {
         raffleId = Long.parseLong(raffleIdRaw);
         drawings = Integer.parseInt(drawingsRaw);
         userCanWinMultiple = Boolean.parseBoolean(userCanWinMultipleRaw);
      } catch (Exception e) {
         e.printStackTrace();
         event.reply("Invalid params.");
         return;
      }

      RaffleUtil ru = new RaffleUtil(raffleId);
      List<Long> winningUsers = ru.draw(drawings, userCanWinMultiple);

      String winnerPlural = drawings > 1 ? "Winners" : "Winner";

      EmbedBuilder eb = new EmbedBuilder()
          .setTitle(String.format("Raffle %s!", winnerPlural))
          .setDescription(ru.getRaffle().getRaffleName());

      String winners = "";
      for (Long userId : winningUsers) {
         winners += guild.getMemberById(userId).getEffectiveName() + "\n";
      }

      eb.addField(winnerPlural, winners, false);
      event.reply(eb.build());
   }
}
