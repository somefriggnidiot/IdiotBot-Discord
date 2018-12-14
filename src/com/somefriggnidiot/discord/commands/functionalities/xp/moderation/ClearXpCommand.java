package com.somefriggnidiot.discord.commands.functionalities.xp.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearXpCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(ClearXpCommand.class);

   public ClearXpCommand() {
      this.name = "clearxp";
      this.aliases = new String[]{"resetxp"};
      this.arguments = "<user>";
      this.category = new Category("Xp Moderation");
      this.help = "Clears a user's xp.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ,
          Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};
      this.guildOnly = true;
      this.requiredRole = "Founder";
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(CommandEvent event) {
      Member member = event.getMessage().getMentionedMembers().get(0);

      DatabaseUserUtil.setXp(event.getGuild().getIdLong(), member.getUser().getIdLong(), 0);
      DatabaseUserUtil.setLevel(member.getUser().getIdLong(), 0);

      logger.info(String.format("[%s] Reset global XP for %s.",
          event.getGuild(),
          member.getEffectiveName()));
   }
}
