package com.somefriggnidiot.discord.commands.functionalities.xp.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.util.XpUtil;
import java.text.DecimalFormat;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Command} used to modify the XP balance on a
 * {@link com.somefriggnidiot.discord.data_access.models.DatabaseUser}.
 */
public class AdjustXpCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(AdjustXpCommand.class);
   private final DecimalFormat df = new DecimalFormat("###,###");

   public AdjustXpCommand() {
      this.name = "adjustxp";
      this.aliases = new String[]{"changexp"};
      this.arguments = "<userMention> <adjustment>";
      this.category = new Category("Xp Moderation");
      this.help = "Adds or subtracts experience points to/from a user's total XP.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ,
          Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};
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
      Integer xpChange;
      try {
         xpChange = Integer.parseInt(messageSplit[2]);
      } catch (NumberFormatException e) {
         event.reply(messageSplit[2] + " is not a valid number.");
         return;
      }

      Integer userXp = DatabaseUserUtil.getUser(memberId).getXpMap().get(guildId);
      Integer adjustedXp = userXp + xpChange < 0 ? 0 : userXp + xpChange;
      Integer adjustedLevel = XpUtil.getLevelForXp(adjustedXp);

      DatabaseUserUtil.setXp(guildId, memberId, adjustedXp);
      new DatabaseUserUtil(memberId).setGuildLevel(guildId, adjustedLevel);

      logger.info(String.format("[%s] Adjusted %s's XP by %s. Now %s XP. (Level %s)",
          event.getGuild(),
          member.getEffectiveName(),
          df.format(xpChange),
          df.format(adjustedXp),
          adjustedLevel));

      event.reply(String.format("Adjusted %s's XP by %s. Now: %s (Level %s)",
          member.getEffectiveName(),
          xpChange,
          adjustedXp,
          adjustedLevel));
   }
}
