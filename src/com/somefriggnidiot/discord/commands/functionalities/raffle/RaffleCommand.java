package com.somefriggnidiot.discord.commands.functionalities.raffle;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class RaffleCommand extends Command {

   public RaffleCommand() {

   }

   @Override
   protected void execute(final CommandEvent event) {
      String message = event.getMessage().getContentDisplay();

      //TODO use switch, combine all raffle commands into one.
   }
}
