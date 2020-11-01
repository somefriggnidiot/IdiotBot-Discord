package com.somefriggnidiot.discord.commands.functionalities.tokens;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import java.text.DecimalFormat;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdjustTokensCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(this.getClass());
   private final DecimalFormat df = new DecimalFormat("###,###");

   public AdjustTokensCommand() {
      this.name = "adjusttokens";
      this.aliases = new String[]{"changetokens"};
      this.arguments = "<userMention> <adjustment>";
      this.category = new Category("Tokens");
      this.help = "Adds or subtracts tokens to/from a user's token count.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_WRITE};
      this.guildOnly = true;
      this.requiredRole = "Founder";
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      Member member = event.getMessage().getMentionedMembers().get(0);
      Long memberId = member.getUser().getIdLong();
      Long guildId = event.getGuild().getIdLong();
      String[] messageSplit = event.getMessage().getContentDisplay().split("\\s", 3);
      Integer tokenChange;

      try {
         tokenChange = Integer.parseInt(messageSplit[2]);
      } catch (NumberFormatException e) {
         event.reply(messageSplit[2] + " is not a valid number.");
         return;
      }

      Integer adjustedTokens = new DatabaseUserUtil(memberId).addTokens(guildId, tokenChange);

      logger.info(String.format("[%s] Adjusted %s's tokens by %s. Now %s XP.",
          event.getGuild(),
          member.getEffectiveName(),
          df.format(tokenChange),
          df.format(adjustedTokens)));

      event.reply(String.format("Adjusted %s's tokens by %s. Now: %s",
          member.getEffectiveName(),
          tokenChange,
          adjustedTokens));
   }
}
