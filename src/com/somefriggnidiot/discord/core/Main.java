package com.somefriggnidiot.discord.core;

import static java.lang.String.format;

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
import com.somefriggnidiot.discord.commands.functionalities.xp.rolelevels.ListRoleLevelsCommand;
import com.somefriggnidiot.discord.commands.functionalities.xp.rolelevels.RefreshRoleLevelAssignmentsCommand;
import com.somefriggnidiot.discord.commands.functionalities.xp.rolelevels.RemoveRoleLevelCommand;
import com.somefriggnidiot.discord.commands.functionalities.xp.xpinfo.LatestXpLeaderboardCommand;
import com.somefriggnidiot.discord.commands.functionalities.xp.xpinfo.ShowXpCommand;
import com.somefriggnidiot.discord.commands.functionalities.xp.xpinfo.XpLeaderboardCommand;
import com.somefriggnidiot.discord.commands.moderation.AddAllUsersToRoleCommand;
import com.somefriggnidiot.discord.commands.moderation.AllowCommand;
import com.somefriggnidiot.discord.commands.moderation.BotModeCommand;
import com.somefriggnidiot.discord.commands.moderation.GetWarningsCommand;
import com.somefriggnidiot.discord.commands.moderation.InfodumpCommand;
import com.somefriggnidiot.discord.commands.moderation.RemoveAllUsersFromRoleCommand;
import com.somefriggnidiot.discord.commands.moderation.TagLogCommand;
import com.somefriggnidiot.discord.commands.moderation.WarningCommand;
import com.somefriggnidiot.discord.data_access.MySqlConnector;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.events.GuildMemberListener;
import com.somefriggnidiot.discord.events.GuildVoiceListener;
import com.somefriggnidiot.discord.events.MessageListener;
import com.somefriggnidiot.discord.events.ReconnectedEventListener;
import com.somefriggnidiot.discord.events.UserUpdateGameListener;
import com.somefriggnidiot.discord.gdbridge.GDBridgeListener;
import com.somefriggnidiot.discord.util.GameGroupUtil;
import com.somefriggnidiot.discord.util.VoiceXpUtil;
import com.somefriggnidiot.discord.util.XpDegradationUtil;
import java.net.URL;
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
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vip.floatationdevice.guilded4j.G4JClient;
import vip.floatationdevice.guilded4j.rest.ChatMessageManager;

public class Main {

   public static JDA jda;
   public static String ownerId;

   // START - Guilded Bridge Test
   static String GTOKEN = "gapi_nI+wF/5WMksp2ixTEQxJB6pM2+QGv0f59OiNyJFVBXT0bQleDKHwK49e8kGL5eIY5VwTA7iqmIdOni3bnbArvQ==";
   public static G4JClient gClient = new G4JClient(GTOKEN);
   public static ChatMessageManager cmm;
   public static MySqlConnector msc;
   public static Session session;
   // END - Guilded Bridge Test

   /**
    *
    * @param args Discord Client Token, Bot Owner Discord ID, MySQL Password
    */
   public static void main(String[] args) {
      System.setProperty("log4j.configurationFile", "resources/log4j2.xml");
      final Logger logger = LoggerFactory.getLogger(Main.class);
      EventWaiter waiter = new EventWaiter();
      ownerId = args[1];

      //TODO Migrate commands to slash-commands.
      try {
         CommandClientBuilder client = new CommandClientBuilder()
             .setActivity(Activity.playing("Bot-it League"))
             .setOwnerId(ownerId)
             .setPrefix("!")
             .setHelpWord("help")
             .addCommands( //Core
                 new ConfigurationCommand(),
                 new DieCommand(waiter),
                 new StatusCommand()
             )
             .addCommands( //Channels
                 new CreatePrivateChannelCommand(waiter),
                 new InviteToPrivateChannelCommand()
             )
             .addCommands( //Fun
                 new CatCommand(),
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
                 new RemoveRoleLevelCommand(),
                 new ListRoleLevelsCommand(),
                 new RefreshRoleLevelAssignmentsCommand()
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
                 new TagLogCommand()
             )
             .addCommand(new ProfileCommand())
             .addCommand(new InfodumpCommand());

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

         logger.info(format("I'm live in %s guilds, serving %s users and %s bots!",
             jda.getGuilds().size(),
             users,
             jda.getUsers().size() - users));

         //General Startup
         XpDegradationUtil.startDegraderDaemon();
         //Start up Guilds
         for (Guild guild : jda.getGuilds()) {
            GuildInfo gi = GuildInfoUtil.getGuildInfo(guild.getIdLong());

            if (gi.isGrantingMessageXp()) {
               VoiceXpUtil.startTimer(guild.getIdLong());
            }

            if (gi.isGroupingGames()) {
               if (gi.gameGroupsAutomatic()) {
                  logger.debug(format("[%s] Starting Auto Groups tracking. Refreshing role "
                      + "assignments.", guild));
                  GameGroupUtil.getGameGroupUtil(guild).startAutoGrouping();
               } else {
                  logger.debug(format("[%s] Started Game Groups tracking. Refreshing role "
                      + "assignments.", guild));
                  GameGroupUtil.refreshGameGroups(guild);
               }
            }
         }
      } catch (LoginException e) {
         logger.error("Error logging in to Discord.", e);
      } catch (InterruptedException e2) {
         logger.error("Operation interrupted!", e2);
      }

      // START - Guilded Bridge Test
      gClient.ws.eventBus.register(new GDBridgeListener());
      gClient.ws.connect();
      // END - Guilded Bridge Test
   }
}
