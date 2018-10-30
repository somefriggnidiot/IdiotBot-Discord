package com.somefriggnidiot.discord.core;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.somefriggnidiot.discord.commands.KarmaCommand;
import com.somefriggnidiot.discord.commands.channels.CreatePrivateChannelCommand;
import com.somefriggnidiot.discord.commands.channels.InviteToPrivateChannelCommand;
import com.somefriggnidiot.discord.commands.fun.CatCommand;
import com.somefriggnidiot.discord.commands.fun.DogCommand;
import com.somefriggnidiot.discord.commands.fun.DogeCommand;
import com.somefriggnidiot.discord.commands.functionalities.gamegroups.AddGameGroupCommand;
import com.somefriggnidiot.discord.commands.functionalities.gamegroups.GroupGamesCommand;
import com.somefriggnidiot.discord.commands.moderation.GetWarningsCommand;
import com.somefriggnidiot.discord.commands.moderation.SoftBanCommand;
import com.somefriggnidiot.discord.commands.moderation.WarningCommand;
import com.somefriggnidiot.discord.events.GuildMemberListener;
import com.somefriggnidiot.discord.events.GuildVoiceListener;
import com.somefriggnidiot.discord.events.MessageListener;
import com.somefriggnidiot.discord.events.UserUpdateGameListener;
import java.net.URL;
import java.util.List;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

   private final static Logger logger = LoggerFactory.getLogger(Main.class);

   public static void main(String[] args) {
      System.setProperty("log4j.configurationFile", "./resources/log4j2.xml");
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
             .addCommand(new InviteToPrivateChannelCommand())
             .addCommand(new WarningCommand())
             .addCommand(new GetWarningsCommand())
             .addCommand(new SoftBanCommand())
             .addCommand(new GroupGamesCommand())
             .addCommand(new AddGameGroupCommand());

         JDA jda = new JDABuilder(args[0])
             .addEventListener(new MessageListener())
             .addEventListener(new UserUpdateGameListener())
             .addEventListener(new GuildVoiceListener())
             .addEventListener(new GuildMemberListener())
             .addEventListener(waiter)
             .addEventListener(client.build())
             .build();

         jda.awaitReady();

         try {
            Icon icon = Icon.from(
                new URL("http://www.foundinaction.com/wp-content/uploads/2018/10/Neon_v2.png")
                    .openStream());
            logger.info("Attempting to set avatar.");
            jda.getSelfUser().getManager().setAvatar(icon).queue();
            logger.info("Avatar set successfully.");
         } catch (Exception e) {
            logger.error("Error while setting avatar", e);
         }

         Integer users = 0;
         List<User> userList = jda.getUsers();

         for (User user : userList) {
            if (!user.isBot()) {
               users++;
            }
         }

         logger.info(String.format("I'm live in %s guilds, serving %s users and %s bots!",
             jda.getGuilds().size(),
             users,
             jda.getUsers().size() - users));
      } catch (LoginException e) {
         logger.error("Error logging in to Discord.", e);
      } catch (InterruptedException e2) {
         logger.error("Operation interrupted!", e2);
      }
   }

}
