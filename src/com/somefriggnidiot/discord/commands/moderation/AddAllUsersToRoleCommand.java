package com.somefriggnidiot.discord.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import com.somefriggnidiot.discord.util.command_util.CommandUtil;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddAllUsersToRoleCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   public AddAllUsersToRoleCommand() {
      this.name = "addalluserstorole";
      this.aliases = new String[]{"addalltorole", "addallto"};
      this.arguments = "<roleName>";
      this.category = new Category("Moderation");
      this.help = "Adds all non-bot users to the first role that matches the given name.";
      this.botPermissions = new Permission[]{Permission.MANAGE_ROLES, Permission.MESSAGE_READ,
          Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE};
      this.guildOnly = true;
      this.cooldown = 5;
      this.cooldownScope = CooldownScope.GUILD;
   }

   @Override
   protected void execute(final CommandEvent event) {
      GuildInfoUtil giu = new GuildInfoUtil(event.getGuild());
      if (!CommandUtil.checkPermissions(event.getMember(), giu.getOwnerRole())) {
         event.reply("You do not have the necessary permissions to use this command.");
         return;
      }

      String[] args = event.getMessage().getContentDisplay().split("\\s", 2);
      Role role = event.getGuild().getRolesByName(args[1], true).get(0);
      Guild guild = event.getGuild();
      List<Member> members = event.getGuild().getMembers().stream()
          .filter(m -> !m.getUser().isBot())
          .collect(Collectors.toList());

      members.forEach(m -> {
         guild.addRoleToMember(m, role).queue();
         logger.info(String.format("[%s] Added %s to \"%s\".",
             event.getGuild(),
             m.getEffectiveName(),
             role.getName()));
      });
   }
}
