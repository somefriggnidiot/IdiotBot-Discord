package com.somefriggnidiot.discord.util.command_util;

import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import java.util.List;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class CommandUtil {

   /**
    * Checks that the {@link Member} is a member of either the `ownerRole` for the {@link Guild}
    * or the specified {@code minimumRole}.
    *
    * @param member the Guild Member whose permissions are being checked.
    * @param minimumRole the role which the Member must be to pass the permission check.
    * @return `true` if the member is part of the serverOwner or specified minimumRole.
    */
   public static Boolean checkPermissions(Member member, Role minimumRole) {
      Guild guild = member.getGuild();
      Long serverOwnerRoleId = GuildInfoUtil.getGuildInfo(guild).getOwnerRoleId();
      Role serverOwnerRole = guild.getRoleById(serverOwnerRoleId);
      List<Role> memberRoles = member.getRoles();

      return memberRoles.contains(serverOwnerRole) || memberRoles.contains(minimumRole);
   }
}
