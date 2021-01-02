package com.somefriggnidiot.discord.util.command_util;

import static com.somefriggnidiot.discord.util.command_util.CommandUtil.checkPermissions;

import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.CommandUtilResponse;
import com.somefriggnidiot.discord.util.CommandUtilResponse.CommandResponseMessage;
import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class ConfigurationCommandUtil {
   private GuildInfoUtil giu;
   private GuildInfo gi;
   private Guild guild;

    public ConfigurationCommandUtil(final Guild guild) {
       this.guild = guild;
       this.giu = new GuildInfoUtil(guild);
       this.gi = GuildInfoUtil.getGuildInfo(guild);
    }

    public CommandUtilResponse setOwnerRole(Member author, Role newOwnerRole) {
       Boolean authorHasPermission = false;
       Long ownerRoleId = gi.getOwnerRoleId();
       List<Role> authorRoles = author.getRoles();

       if (ownerRoleId > 1L) {
          //Check author is owner.

          for (Role authorRole : authorRoles) {
             if (authorRole.getIdLong() == ownerRoleId) {
                authorHasPermission = true;
                break;
             }
          }
       } else {
          //Check author has a role with admin permissions.

          for (Role authorRole : authorRoles) {
             if (authorRole.getPermissions().contains(Permission.ADMINISTRATOR)) {
                authorHasPermission = true;
                break;
             }
          }
       }

       //
       if (authorHasPermission) {
             giu.setOwnerId(newOwnerRole.getIdLong());
             return new CommandUtilResponse(true, CommandResponseMessage.SUCCESS);
       } else {
          return new CommandUtilResponse(false, CommandResponseMessage.PERMISSION_DENIED);
       }
    }

    public CommandUtilResponse setStaffRole(Member author, Role newStaffRole) {
       if (checkPermissions(author, giu.getOwnerRole())) {
          giu.setModeratorId(newStaffRole.getIdLong());

          if (gi.getModeratorRoleId() == newStaffRole.getIdLong()) {
             return new CommandUtilResponse(true, CommandResponseMessage.SUCCESS);
          } else {
             return new CommandUtilResponse(false, CommandResponseMessage.UNKNOWN_ERROR);
          }
       } else {
          return new CommandUtilResponse(false, CommandResponseMessage.PERMISSION_DENIED);
       }
    }

    public CommandUtilResponse setStreamerRole(Member author, Role newStreamerRole) {
       if (checkPermissions(author, giu.getStaffRole())) {
          GuildInfoUtil.setStreamerRoleId(guild.getIdLong(), newStreamerRole.getIdLong());

          if (gi.getStreamerRoleId() == newStreamerRole.getIdLong()) {
             return new CommandUtilResponse(true, CommandResponseMessage.SUCCESS);
          } else {
             return new CommandUtilResponse(false, CommandResponseMessage.UNKNOWN_ERROR);
          }
       } else {
          return new CommandUtilResponse(false, CommandResponseMessage.PERMISSION_DENIED);
       }
    }

    public CommandUtilResponse setVoiceMultiplier(Member author, Double voiceXpMultiplier) {
       if(checkPermissions(author, giu.getStaffRole())) {
          giu.setVoiceXpMultiplier(voiceXpMultiplier);

          if (gi.getVoiceXpMultiplier() == voiceXpMultiplier) {
             return new CommandUtilResponse(true, CommandResponseMessage.SUCCESS);
          } else {
             return new CommandUtilResponse(false, CommandResponseMessage.UNKNOWN_ERROR);
          }
       } else {
          return new CommandUtilResponse(false, CommandResponseMessage.PERMISSION_DENIED);
       }
    }

    public CommandUtilResponse setBotTextChannel(Member author, TextChannel newBotTextChannel) {
       if(checkPermissions(author, giu.getOwnerRole())) {
          giu.setBotTextChannelId(newBotTextChannel.getIdLong());

          if (gi.getBotTextChannelId() == newBotTextChannel.getIdLong()) {
             return new CommandUtilResponse(true, CommandResponseMessage.SUCCESS);
          } else {
             return new CommandUtilResponse(false, CommandResponseMessage.UNKNOWN_ERROR);
          }
       } else {
          return new CommandUtilResponse(false, CommandResponseMessage.PERMISSION_DENIED);
       }
    }

    public CommandUtilResponse setXpEnabled(Member author, String value) {
       if(checkPermissions(author, giu.getStaffRole())) {
          try {
             Boolean setActive = Boolean.parseBoolean(value);
             giu.setXpTracking(setActive);

             if (gi.isGrantingMessageXp() == setActive) {
                return new CommandUtilResponse(true, CommandResponseMessage.SUCCESS);
             } else {
                return new CommandUtilResponse(false, CommandResponseMessage.UNKNOWN_ERROR);
             }
          } catch (Exception e) {
             return new CommandUtilResponse(false, CommandResponseMessage.INVALID_ARG);
          }
       } else {
          return new CommandUtilResponse(false, CommandResponseMessage.PERMISSION_DENIED);
       }
    }
}
