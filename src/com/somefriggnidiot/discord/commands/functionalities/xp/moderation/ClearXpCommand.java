package com.somefriggnidiot.discord.commands.functionalities.xp.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.command_util.CommandUtil;
import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearXpCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(ClearXpCommand.class);

   public ClearXpCommand() {
      this.name = "clearxp";
      this.aliases = new String[]{"resetxp"};
      this.arguments = "<user-mention>";
      this.category = new Category("Xp Moderation");
      this.help = "Clears a user's xp.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ,
          Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};
      this.guildOnly = true;
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(CommandEvent event) {
      GuildInfoUtil giu = new GuildInfoUtil(event.getGuild());
      if (!CommandUtil.checkPermissions(event.getMember(), giu.getOwnerRole())) {
         event.reply("You do not have the necessary permissions to use this command.");
         return;
      }

      //Reset for all users.
      List<Member> xpMembers = event.getGuild().getMembers();

      for (Member member : xpMembers) {
         Long guildId = event.getGuild().getIdLong();
         Long userId = member.getUser().getIdLong();

         DatabaseUserUtil.setXp(guildId, userId, 0);
         DatabaseUserUtil.getUser(userId).updateLevel(guildId, 0);

         logger.info(String.format("[%s] Reset guild XP for %s.",
             event.getGuild(),
             member.getEffectiveName()));
      }

      event.reply("Unless I fucked up while hacking this command out, all XP has been yeeted "
          + "from this server. May wanna use '!top' to verify.");

      //Reset specific user.
//      Member member = event.getMessage().getMentionedMembers().get(0);
//      Long guildId = event.getGuild().getIdLong();
//
//      DatabaseUserUtil.setXp(guildId, member.getUser().getIdLong(), 0);
//      DatabaseUserUtil.getUser(member.getUser().getIdLong()).updateLevel(guildId, 0);
//
//      logger.info(String.format("[%s] Reset guild XP for %s.",
//          event.getGuild(),
//          member.getEffectiveName()));
   }
}
