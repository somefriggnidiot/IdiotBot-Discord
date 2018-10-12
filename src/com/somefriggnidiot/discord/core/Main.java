package com.somefriggnidiot.discord.core;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.somefriggnidiot.discord.commands.channels.CreatePrivateChannelCommand;
import com.somefriggnidiot.discord.commands.channels.InviteToPrivateChannelCommand;
import com.somefriggnidiot.discord.commands.fun.DogCommand;
import com.somefriggnidiot.discord.commands.fun.DogeCommand;
import com.somefriggnidiot.discord.commands.KarmaCommand;
import com.somefriggnidiot.discord.events.GuildMemberListener;
import com.somefriggnidiot.discord.events.GuildVoiceListener;
import com.somefriggnidiot.discord.events.MessageListener;
import com.somefriggnidiot.discord.commands.fun.CatCommand;
import com.somefriggnidiot.discord.events.UserUpdateGameListener;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

   private final static Logger logger = LoggerFactory.getLogger(Main.class);

   public static void  main(String[] args) {
      EventWaiter waiter = new EventWaiter();

      try {
         CommandClientBuilder client = new CommandClientBuilder()
             .setGame(Game.watching("you sleep."))
             .setOwnerId("129098270323638272")
             .setPrefix("!")
             .setHelpWord("help")
             .addCommand(new CatCommand())
             .addCommand(new DogCommand())
             .addCommand(new DogeCommand())
             .addCommand(new KarmaCommand())
             .addCommand(new CreatePrivateChannelCommand(waiter))
             .addCommand(new InviteToPrivateChannelCommand());

         JDA jda = new JDABuilder(args[0])
            .addEventListener(new MessageListener())
            .addEventListener(new UserUpdateGameListener())
            .addEventListener(new GuildVoiceListener())
            .addEventListener(new GuildMemberListener())
            .addEventListener(waiter)
            .addEventListener(client.build())
            .build();

         jda.awaitReady();

         logger.info(String.format("I'm live in %s guilds, serving %s users!",
             jda.getGuilds().size(),
             jda.getUsers().size()));
      } catch (LoginException e) {
         logger.error("Error logging in to Discord.", e);
      } catch (InterruptedException e2) {
         logger.error("Operation interrupted!", e2);
      }
   }

}
