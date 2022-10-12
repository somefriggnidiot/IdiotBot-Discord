package com.somefriggnidiot.discord.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.command_util.CommandUtil;
import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllowCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(AllowCommand.class);

   public AllowCommand() {
      this.name = "allow";
      this.aliases = new String[]{"permit", "member"};
      this.arguments = "<userMention>( <userMention>)";
      this.category = new Category("Moderation");
      this.help = "Removes the configured guest role from the designated user or multiple users.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_WRITE,
          Permission.MANAGE_ROLES};
      this.guildOnly = true;
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      Guild guild = event.getGuild();
      GuildInfoUtil giu = new GuildInfoUtil(guild);
      if (!CommandUtil.checkPermissions(event.getMember(), giu.getStaffRole())) {
         event.reply("You do not have the necessary permissions to use this command.");
         return;
      }

      Role guestRole = giu.getGuestRole();

      List<Member> allowedMembers = event.getMessage().getMentionedMembers();
      for (Member member : allowedMembers) {
         guild.removeRoleFromMember(member, guestRole)
             .reason("Granted access by " + event.getMember().getEffectiveName())
             .complete();
      }
   }

}
