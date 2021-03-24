package com.somefriggnidiot.discord.core;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.somefriggnidiot.discord.commands.ProfileCommand;
import com.somefriggnidiot.discord.commands.StatusCommand;
import com.somefriggnidiot.discord.commands.channels.CreatePrivateChannelCommand;
import com.somefriggnidiot.discord.commands.channels.InviteToPrivateChannelCommand;
import com.somefriggnidiot.discord.commands.fun.CatCommand;
import com.somefriggnidiot.discord.commands.fun.DogCommand;
import com.somefriggnidiot.discord.commands.fun.DogeCommand;
import com.somefriggnidiot.discord.commands.fun.EchoCommand;
import com.somefriggnidiot.discord.commands.functionalities.core.ConfigurationCommand;
import com.somefriggnidiot.discord.commands.functionalities.core.DieCommand;
import com.somefriggnidiot.discord.commands.functionalities.gamegroups.AddGameGroupCommand;
import com.somefriggnidiot.discord.commands.functionalities.gamegroups.GroupGamesCommand;
import com.somefriggnidiot.discord.commands.functionalities.gamegroups.RemoveGameGroupCommand;
import com.somefriggnidiot.discord.commands.functionalities.gamegroups.StreamerFeatureCommand;
import com.somefriggnidiot.discord.commands.functionalities.raffle.CloseRaffleCommand;
import com.somefriggnidiot.discord.commands.functionalities.raffle.CreateRaffleCommand;
import com.somefriggnidiot.discord.commands.functionalities.raffle.DrawRaffleCommand;
import com.somefriggnidiot.discord.commands.functionalities.raffle.EnterRaffleCommand;
import com.somefriggnidiot.discord.commands.functionalities.raffle.ListRafflesCommand;
import com.somefriggnidiot.discord.commands.functionalities.tokens.AdjustTokensCommand;
import com.somefriggnidiot.discord.commands.functionalities.tokens.ResetAllTokensCommand;
import com.somefriggnidiot.discord.commands.functionalities.xp.moderation.AdjustXpCommand;
import com.somefriggnidiot.discord.commands.functionalities.xp.moderation.ClearXpCommand;
import com.somefriggnidiot.discord.commands.functionalities.xp.moderation.SetVoiceSpecialCommand;
import com.somefriggnidiot.discord.commands.functionalities.xp.moderation.ToggleLuckBonusCommand;
import com.somefriggnidiot.discord.commands.functionalities.xp.moderation.ToggleXpGainCommand;
import com.somefriggnidiot.discord.commands.functionalities.xp.rolelevels.AddRoleLevelCommand;
import com.somefriggnidiot.discord.commands.functionalities.xp.rolelevels.RemoveRoleLevelCommand;
import com.somefriggnidiot.discord.commands.functionalities.xp.xpinfo.LatestXpLeaderboardCommand;
import com.somefriggnidiot.discord.commands.functionalities.xp.xpinfo.ShowXpCommand;
import com.somefriggnidiot.discord.commands.functionalities.xp.xpinfo.XpLeaderboardCommand;
import com.somefriggnidiot.discord.commands.moderation.AddAllUsersToRoleCommand;
import com.somefriggnidiot.discord.commands.moderation.AllowCommand;
import com.somefriggnidiot.discord.commands.moderation.BotModeCommand;
import com.somefriggnidiot.discord.commands.moderation.GetWarningsCommand;
import com.somefriggnidiot.discord.commands.moderation.RemoveAllUsersFromRoleCommand;
import com.somefriggnidiot.discord.commands.moderation.SoftBanCommand;
import com.somefriggnidiot.discord.commands.moderation.TagLogCommand;
import com.somefriggnidiot.discord.commands.moderation.WarningCommand;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.events.GuildMemberListener;
import com.somefriggnidiot.discord.events.GuildVoiceListener;
import com.somefriggnidiot.discord.events.MessageListener;
import com.somefriggnidiot.discord.events.ReconnectedEventListener;
import com.somefriggnidiot.discord.events.UserUpdateGameListener;
import com.somefriggnidiot.discord.util.GameGroupUtil;
import com.somefriggnidiot.discord.util.VoiceXpUtil;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

   private final static Logger logger = LoggerFactory.getLogger(Main.class);
   public static JDA jda;
   public static String ownerId;

   public static void main(String[] args) {
      System.setProperty("log4j.configurationFile", "./resources/log4j2.xml");
      EventWaiter waiter = new EventWaiter();
      ownerId = args[1];

      try {
         CommandClientBuilder client = new CommandClientBuilder()
             .setActivity(Activity.playing("Bot-it League"))
             .setOwnerId(ownerId)
             .setPrefix("!")
             .setHelpWord("help")
             .addCommands( //Core
                 new ConfigurationCommand()
             )
             .addCommands( //Channels
                 new CreatePrivateChannelCommand(waiter),
                 new InviteToPrivateChannelCommand()
             )
             .addCommands( //Fun
                 new CatCommand(),
                 new DieCommand(waiter), //TODO Make core functionality group and move.
                 new DogCommand(),
                 new DogeCommand(),
                 new EchoCommand()
             )
             .addCommands( // Functionalities - GameGroups
                 new AddGameGroupCommand(),
                 new GroupGamesCommand(),
                 new RemoveGameGroupCommand(),
                 new StreamerFeatureCommand()
             )
             .addCommands( // Raffles
                 new CloseRaffleCommand(),
                 new CreateRaffleCommand(),
                 new DrawRaffleCommand(),
                 new EnterRaffleCommand(),
                 new ListRafflesCommand()
             )
             .addCommands( // Tokens
                 new AdjustTokensCommand(),
                 new ResetAllTokensCommand()
             )
             .addCommands( //XP - Moderation
                 new AdjustXpCommand(),
                 new BotModeCommand(),
                 new ClearXpCommand(),
                 new SetVoiceSpecialCommand(),
                 new ToggleXpGainCommand(),
                 new ToggleLuckBonusCommand()
              )
             .addCommands( //XP - Role Levels
                 new AddRoleLevelCommand(),
                 new RemoveRoleLevelCommand()
             )
             .addCommands( // XP - XpInfo
                 new ShowXpCommand(),
                 new LatestXpLeaderboardCommand(),
                 new XpLeaderboardCommand()
             )
             .addCommands( //Moderation
                 new AddAllUsersToRoleCommand(),
                 new AllowCommand(),
                 new GetWarningsCommand(),
                 new RemoveAllUsersFromRoleCommand(),
                 new WarningCommand(),
                 new TagLogCommand(),
                 new SoftBanCommand()
             )
//             .addCommand(new KarmaCommand())
             .addCommand(new ProfileCommand())
             .addCommand(new StatusCommand());

         jda = new JDABuilder().addEventListeners(
             new MessageListener(),
             new UserUpdateGameListener(),
             new GuildVoiceListener(),
             new GuildMemberListener(),
             new ReconnectedEventListener(),
             waiter,
             client.build())
             .setAutoReconnect(true)
             .setEnabledIntents(EnumSet.allOf(GatewayIntent.class))
             .setToken(args[0])
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
            logger.error("Error while setting avatar: ", e.getMessage());
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

         //Start up Guilds
         for (Guild guild : jda.getGuilds()) {
            GuildInfo gi = GuildInfoUtil.getGuildInfo(guild.getIdLong());

            if (gi.isGrantingMessageXp()) {
               VoiceXpUtil.startTimer(guild.getIdLong());
            }

            if (gi.isGroupingGames()) {
               logger.info(String.format("[%s] Started Game Groups tracking. Refreshing role "
                   + "assignments.", guild));
               GameGroupUtil.refreshGameGroups(guild);
            }
         }
      } catch (LoginException e) {
         logger.error("Error logging in to Discord.", e);
      } catch (InterruptedException e2) {
         logger.error("Operation interrupted!", e2);
      }
   }

   private List<Command> listCommands() {
      ArrayList<Command> commands = new ArrayList<>();
      EventWaiter waiter = new EventWaiter();

      //Channels
      commands.add(new CreatePrivateChannelCommand(waiter));
//          new CreatePrivateChannelCommand(waiter),
//          new InviteToPrivateChannelCommand()
//          .addCommands( //Fun
//              new CatCommand(),
//              new DieCommand(), //TODO Make core functionality group and move.
//              new DogCommand(),
//              new DogeCommand(),
//              new EchoCommand()
//          )
//          .addCommands( // Functionalities - GameGroups
//              new AddGameGroupCommand(),
//              new GroupGamesCommand(),
//              new RemoveGameGroupCommand()
//          )
//          .addCommands( // Raffles
//              new CloseRaffleCommand(),
//              new CreateRaffleCommand(),
//              new DrawRaffleCommand(),
//              new EnterRaffleCommand(),
//              new ListRafflesCommand()
//          )
//          .addCommands( // Tokens
//              new AdjustTokensCommand(),
//              new ResetAllTokensCommand()
//          )
//          .addCommands( //XP - Moderation
//              new AdjustXpCommand(),
//              new ClearXpCommand(),
//              new SetVoiceSpecialCommand(),
//              new ToggleXpGainCommand(),
//              new ToggleLuckBonusCommand()
//          )
//          .addCommands( //XP - Role Levels
//              new AddRoleLevelCommand(),
//              new RemoveRoleLevelCommand()
//          )
//          .addCommands( // XP - XpInfo
//              new ShowXpCommand(),
//              new XpLeaderboardCommand()
//          )
//          .addCommands( //Moderation
//              new AddAllUsersToRoleCommand(),
//              new GetWarningsCommand(),
//              new RemoveAllUsersFromRoleCommand(),
//              new WarningCommand(),
//              new TagLogCommand(),
//              new SoftBanCommand()
//          )
////             .addCommand(new KarmaCommand())
//          .addCommand(new ProfileCommand())
//          .addCommand(new StatusCommand());

      return commands;
   }

}
