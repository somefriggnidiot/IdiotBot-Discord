package com.somefriggnidiot.discord.util;

import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.events.MessageListener;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XpUtil {
   private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   public static Integer checkForLevelUp(Guild guild, User user, Integer newXp) {
      Long userId = user.getIdLong();
      Integer currentLevel = DatabaseUserUtil.getUser(userId).getLevel() == null ? 0 :
          DatabaseUserUtil.getUser(userId).getLevel();
      Integer xpThreshold = getXpThresholdForLevel(currentLevel+1);
      Integer newLevel = currentLevel;

      if (newXp > xpThreshold) {
         newLevel = DatabaseUserUtil.setLevel(userId, getLevelForXp(newXp));
         String newLevelString = String.format("**%s**  (%s%s)",
             newLevel.toString(),
             (newLevel - currentLevel)>=1 ? "+" : "",
             (newLevel - currentLevel)
         );

         EmbedBuilder eb = new EmbedBuilder()
             .setColor(Color.CYAN)
             .setThumbnail(user.getAvatarUrl())
             .setTitle(String.format("%s has leveled up!", user.getName()))
             .addField("Current Level", newLevelString, true)
             .addField("Progress to Next Level",
                 newXp + " / " + getXpThresholdForLevel(newLevel+1)
                     .toString(),
                 true);

         List <Role> newRoles = handleRoleAssignments(null, userId, guild);
         if (newRoles != null && !newRoles.isEmpty()) {
            String roleList = "";

            for (Role role : newRoles) {
               roleList += role.getName().toString() + ", ";
            }

            roleList = roleList.substring(0, roleList.length()-2);
            eb.addField("Role(s) Unlocked", roleList, true);
         }

         guild.getTextChannelsByName("bot-spam", false).get(0)
             .sendMessage(eb.build()).queue();

      } else if (newXp < getXpThresholdForLevel(currentLevel)) {
         newLevel = DatabaseUserUtil.setLevel(userId, getLevelForXp(newXp));
         logger.info(String.format("[%s] Demoted %s to level %s.",
             guild,
             user.getName(),
             newLevel));

         handleRoleAssignments(null, userId, guild);
      }

      return newLevel - currentLevel;
   }

   private static List<Role> handleRoleAssignments(TextChannel channel, Long userId, Guild
       guild) {
      GuildInfo gi = GuildInfoUtil.getGuildInfo(guild.getIdLong());

      if (gi.getLevelRolesActive() || !gi.getLevelRolesActive()) {
         HashMap<Long, Integer> roleLevelIds = gi.getRoleLevelMappings();
         Integer userLevel = DatabaseUserUtil.getUser(userId).getLevel();
         List<Long> applicableRoleIds = roleLevelIds.keySet().stream()
             .filter(e -> roleLevelIds.get(e) <= userLevel)
             .collect(Collectors.toList());
         List<Role> applicableRoles = new ArrayList<>();
         applicableRoleIds.forEach(e -> applicableRoles.add(guild.getRoleById(e)));
         Member member = guild.getMemberById(userId);
         List<Role> newRoles = applicableRoles.stream()
             .filter(role -> !member.getRoles().contains(role))
             .collect(Collectors.toList());


         guild.getController().addRolesToMember(member, newRoles).queue();

         return newRoles;
      }

      return null;
   }

   public static Integer getLevelForXp(Integer xp) {
      Integer level = -1;
      Integer thresholdXp  = getXpThresholdForLevel(level);

      while (xp > thresholdXp) {
         thresholdXp = getXpThresholdForLevel(level);

         if (xp < thresholdXp) {
            break;
         } else {
            level++;
         }
      }

      level--;
      return level;
   }

   public static Integer getXpThresholdForLevel(Integer level) {
      Integer threshold = 0;

      for (int i=0; i<level; i++) {
         threshold += 5*(i*i) + 50*(i) + 100;
      }

      return threshold;
   }
}
