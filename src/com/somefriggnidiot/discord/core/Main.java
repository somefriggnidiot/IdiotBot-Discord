package com.somefriggnidiot.discord.core;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.somefriggnidiot.discord.commands.ProfileCommand;
import com.somefriggnidiot.discord.commands.StatusCommand;
import com.somefriggnidiot.discord.commands.channels.CreatePrivateChannelCommand;
import com.somefriggnidiot.discord.commands.channels.InviteToPrivateChannelCommand;
import com.somefriggnidiot.discord.commands.fun.CatCommand;
import com.somefriggnidiot.discord.commands.fun.DogCommand;
import com.somefriggnidiot.discord.commands.fun.DogeCommand;
import com.somefriggnidiot.discord.commands.functionalities.gamegroups.AddGameGroupCommand;
import com.somefriggnidiot.discord.commands.functionalities.gamegroups.GroupGamesCommand;
import com.somefriggnidiot.discord.commands.functionalities.gamegroups.RemoveGameGroupCommand;
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
import com.somefriggnidiot.discord.commands.functionalities.xp.xpinfo.ShowXpCommand;
import com.somefriggnidiot.discord.commands.functionalities.xp.xpinfo.XpLeaderboardCommand;
import com.somefriggnidiot.discord.commands.moderation.AddAllUsersToRoleCommand;
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
import com.somefriggnidiot.discord.events.UserUpdateGameListener;
import com.somefriggnidiot.discord.util.GameGroupUtil;
import com.somefriggnidiot.discord.util.VoiceXpUtil;
import java.net.URL;
import java.util.List;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

   private final static Logger logger = LoggerFactory.getLogger(Main.class);
   public static JDA jda;

   public static void main(String[] args) {
      System.setProperty("log4j.configurationFile", "./resources/log4j2.xml");
      EventWaiter waiter = new EventWaiter();

      try {
         CommandClientBuilder client = new CommandClientBuilder()
             .setGame(Game.playing("Bot-it League"))
             .setOwnerId(args[1])
             .setPrefix("!")
             .setHelpWord("help")
             .addCommands( //Channels
                 new CreatePrivateChannelCommand(waiter),
                 new InviteToPrivateChannelCommand()
             )
             .addCommands( //Fun
                 new CatCommand(),
                 new DogCommand(),
                 new DogeCommand()
             )
             .addCommands( // Functionalities - GameGroups
                 new AddGameGroupCommand(),
                 new GroupGamesCommand(),
                 new RemoveGameGroupCommand()
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
                 new XpLeaderboardCommand()
             )
             .addCommands( //Moderation
                 new AddAllUsersToRoleCommand(),
                 new GetWarningsCommand(),
                 new RemoveAllUsersFromRoleCommand(),
                 new WarningCommand(),
                 new TagLogCommand(),
                 new SoftBanCommand()
             )
//             .addCommand(new KarmaCommand())
             .addCommand(new ProfileCommand())
             .addCommand(new StatusCommand());

         jda = new JDABuilder(args[0])
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

         //Start up Guilds
         for (Guild guild : jda.getGuilds()) {
            GuildInfo gi = GuildInfoUtil.getGuildInfo(guild.getIdLong());

            if (gi.isGrantingMessageXp()) {
               VoiceXpUtil.startTimer(guild.getIdLong());
               logger.info(String.format("[%s] Started voice XP timer.",
                   guild));
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

}
