package com.somefriggnidiot.discord.commands.functionalities.core;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.somefriggnidiot.discord.core.Main;
import com.somefriggnidiot.discord.util.GameGroupUtil;
import java.util.List;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DieCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(this.getClass());
   private final EventWaiter waiter;

   public DieCommand(EventWaiter waiter) {
      this.waiter = waiter;
      this.name = "die";
      this.guildOnly = false;
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER_CHANNEL;
   }

   @Override
   protected void execute(final CommandEvent event) {
      String reply = "Bleh. XP";
      String ownerId = Main.ownerId;

      event.reply(reply);

      if (ownerId.equalsIgnoreCase(event.getAuthor().getId())) {
         JDA jda = Main.jda;

         new ButtonMenu.Builder()
             .setChoices("✅", "❌")
             .setAction(
                 (ReactionEmote reactionEmote) -> {
                    if (reactionEmote.getName().equalsIgnoreCase("✅")) {
                       logger.info(
                           String.format("[%s] Shutting down.", event.getGuild()));
                       event.reply("Shutting down.");

                       List<Guild> guilds = jda.getGuilds();

                       for (Guild guild : guilds) {
                          GameGroupUtil.removeAllUserRoles(guild);
                       }

                       jda.shutdown();
                       System.exit(0);
                    } else {
                       event.reply("Shutdown aborted.");
                       logger.info(
                           String.format("[%s] Aborting shutdown.", event.getGuild()));
                    }
                 }
             )
         .setEventWaiter(waiter)
         .setText("Are you sure you would like to shut down IdiotBot for all Guilds?")
         .build()
         .display(event.getTextChannel());

      }
   }

}
