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

      //If first arg is number, return information about raffle with that id

      /*
      USAGES
      1. Display information about a raffle. (Arg provided is a number.)
      2. List raffles (No args)
      3. CREATE - similar to existing params for creating raffle.
      4. DRAW - similar to existing params for drawing raffle.
      5. CLOSE - closes a raffle.
       */
   }
}
