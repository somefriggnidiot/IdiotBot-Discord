package com.somefriggnidiot.discord.commands.functionalities.tokens;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import java.util.List;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResetAllTokensCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(this.getClass());

   public ResetAllTokensCommand() {
      this.name = "resetalltokens";
      this.aliases = new String[]{};
      this.arguments = "";
      this.ownerCommand = true;
      this.category = new Category("Tokens");
      this.help = "Resets tokens for all users in the server.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE, Permission.MESSAGE_READ};
      this.guildOnly = true;
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER_GUILD;
   }

   @Override
   protected void execute(final CommandEvent event) {
      Long guildId = event.getGuild().getIdLong();
      List<Member> guildMembers = event.getGuild().getMembers();

      try {
         guildMembers.forEach(member ->
             new DatabaseUserUtil(member.getUser().getIdLong())
                 .setTokens(guildId, 0)
         );

         event.reply("Successfully removed tokens from all users.");
      } catch(Exception e) {
         event.reply("There was an error removing user tokens.");
      }

   }
}
