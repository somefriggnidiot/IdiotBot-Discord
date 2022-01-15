package com.somefriggnidiot.discord.commands.functionalities.xp.rolelevels;

import static java.lang.String.format;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListRoleLevelsCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(ListRoleLevelsCommand.class);

   public ListRoleLevelsCommand() {
      this.name = "listrolelevels";
      this.aliases = new String[]{"rolelevels", "listlevelroles", "levelroles"};
      this.category = new Category("Role Levels");
      this.help = "Lists all existing Level Role mappings for the guild.";
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_READ};
      this.guildOnly = true;
      this.cooldown = 3;
      this.cooldownScope = CooldownScope.USER_GUILD;
   }

   @Override
   protected void execute(final CommandEvent event) {
      Guild guild = event.getGuild();
      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild);
      Map<Long, Integer> mappings = gi.getRoleLevelMappings();

      List<Integer> levelsRewarded = mappings.values().stream().distinct().sorted()
          .collect(Collectors.toList());


      EmbedBuilder eb = new EmbedBuilder();
      eb.setTitle(format("Level Roles - %s", guild.getName()))
          .setColor(Color.BLUE)
          .setThumbnail(guild.getIconUrl())
          .setFooter("Provided to you by IdiotBot. The most idiotic of bots.",
              "http://www.foundinaction.com/wp-content/uploads/2018/08/"
                  + "Neon_600x600_Transparent.png");

      List<Long> invalidRoles = new ArrayList<>();
      for (Integer groupLevel : levelsRewarded) {
         String levelRolesBody = "";

         for (Entry<Long, Integer> entry : mappings.entrySet()) {
            if (entry.getValue().equals(groupLevel)) {
               Role role = guild.getRoleById(entry.getKey());

               if (role != null) {
                  levelRolesBody = levelRolesBody.concat(role.getAsMention() + " ");
               } else {
                  invalidRoles.add(role.getIdLong());
               }
            }
         }

         eb.addField(format("**Level %s**", groupLevel), levelRolesBody, false);
      }
      invalidRoles.forEach(gi::removeRoleLevelMapping);

      event.reply(eb.build());
   }

   private class RoleLevelMapping implements Comparable<RoleLevelMapping> {
      Integer level;
      Role role;
      Long roleId;

      RoleLevelMapping(Integer level, Role role) {
         this.level = level;
         this.role = role;
      }

      @Override
      public int compareTo(RoleLevelMapping d) {
         return this.level - d.level;
      }

      public Integer getLevel() {
         return level;
      }

      public void setLevel(Integer level) {
         this.level = level;
      }

      public Role getRole() {
         return role;
      }

      public void setRole(Role role) {
         this.role = role;
      }

      public Long getRoleId() {
         return roleId;
      }

      public void setRoleId(Long roleId) {
         this.roleId = roleId;
      }
   }

   private class LevelGroup {
      private List<RoleLevelMapping> group;
      private Integer level;

      public LevelGroup(
          List<RoleLevelMapping> group, Integer level) {
         this.group = group;
         this.level = level;
      }

      public List<RoleLevelMapping> getGroup() {
         return group;
      }

      public void setGroup(List<RoleLevelMapping> group) {
         this.group = group;
      }

      public void addToGroup(RoleLevelMapping mapping) {
         this.group.add(mapping);
      }

      public Integer getLevel() {
         return level;
      }

      public void setLevel(Integer level) {
         this.level = level;
      }
   }
}
