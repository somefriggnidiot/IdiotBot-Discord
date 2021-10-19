package com.somefriggnidiot.discord.data_access.util;

import com.somefriggnidiot.discord.core.Main;
import com.somefriggnidiot.discord.data_access.DatabaseConnector;
import com.somefriggnidiot.discord.data_access.DatabaseConnector.Table;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.util.HighscoreObject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class GuildInfoUtil {

   private static EntityManager em = new DatabaseConnector().getEntityManager(Table.GUILD_INFO);
   private GuildInfo gi;
   private Guild guild;

   public GuildInfoUtil(Long guildId) {
      this.gi = getGuildInfo(guildId);
      this.guild = Main.jda.getGuildById(guildId);
   }

   public GuildInfoUtil(Guild guild) {
      this.gi = getGuildInfo(guild.getIdLong());
      this.guild = guild;
   }

   public void addRoleLevelMapping(Long roleId, Integer level) {
      em.getTransaction().begin();
      gi.addRoleLevelMapping(roleId, level);
      em.persist(gi);
      em.getTransaction().commit();
   }

   public Integer removeRoleLevelMapping(Long roleId) {
      Integer level;

      em.getTransaction().begin();
      level = gi.removeRoleLevelMapping(roleId);
      em.persist(gi);
      em.getTransaction().commit();

      return level;
   }

   public void setOwnerId(Long ownerId) {
      em.getTransaction().begin();
      gi.setOwnerRoleId(ownerId);
      em.persist(gi);
      em.getTransaction().commit();
   }

   public Role getOwnerRole() {
      return guild.getRoleById(gi.getOwnerRoleId());
   }

   public void setModeratorId(Long moderatorId) {
      em.getTransaction().begin();
      gi.setModeratorRoleId(moderatorId);
      em.persist(gi);
      em.getTransaction().commit();
   }

   public void setGuestRoleId(Long guestRoleId) {
      em.getTransaction().begin();
      gi.setGuestRoleId(guestRoleId);
      em.persist(gi);
      em.getTransaction().commit();
   }

   public Role getGuestRole() {
      return guild.getRoleById(gi.getGuestRoleId());
   }

   public Role getStaffRole() {
      return guild.getRoleById(gi.getModeratorRoleId());
   }

   public void setBotTextChannelId(Long botTextChannelId) {
      em.getTransaction().begin();
      gi.setBotTextChannelId(botTextChannelId);
      em.persist(gi);
      em.getTransaction().commit();
   }

   public TextChannel getBotTextChannel() {
      return guild.getTextChannelById(gi.getBotTextChannelId());
   }

   public void setVoiceXpMultiplier(double multiplier) {
      em.getTransaction().begin();
      gi.setVoiceXpMultiplier(multiplier);
      em.persist(gi);
      em.getTransaction().commit();
   }

   public double getVoiceXpMultiplier() {
      return gi.getVoiceXpMultiplier();
   }

   public List<HighscoreObject> getRankedXpList() {
      List<HighscoreObject> rankedList = new ArrayList<>();
      List<DatabaseUser> dbus = new ArrayList<>();
      List<Member> members = guild.getMembers();

      members.forEach(member -> dbus
          .add(DatabaseUserUtil.getUser(member.getUser().getIdLong())));
      dbus.forEach(dbu -> rankedList.add(new HighscoreObject(dbu, dbu.getXpMap().get(guild
          .getIdLong()) == null ? 0 : dbu.getXpMap().get(guild.getIdLong()), dbu.getLatestGain())));

      return rankedList.stream()
          .sorted(Comparator.comparing(HighscoreObject::getXp).reversed())
          .collect(Collectors.toList());
   }

   public List<Long> getRaffleIds() {
      return getGuildInfo(guild.getIdLong()).getRaffleIds();
   }

   public void addRaffleId(Long raffleId) {
      List<Long> raffleIds = getGuildInfo(guild.getIdLong()).getRaffleIds();

      if (!raffleIds.contains(raffleId)) {
         raffleIds.add(raffleId);
         getGuildInfo(guild.getIdLong()).setRaffleIds(raffleIds);

         em.getTransaction().begin();
         em.persist(getGuildInfo(guild.getIdLong()));
         em.getTransaction().commit();
      }
   }

   public void addBotModeEntryId(Instant bmeId) {
      List<Instant> bmeIds = getGuildInfo(guild).getBotModeEntryIds();

      if (!bmeIds.contains(bmeId)) {
         bmeIds.add(bmeId);
         getGuildInfo(guild).setBotModeEntryIds(bmeIds);

         em.getTransaction().begin();
         em.persist(getGuildInfo(guild));
         em.getTransaction().commit();
      }
   }

   public static GuildInfo getGuildInfo(Long guildId) {
      return getDatabaseObject(guildId);
   }

   public static GuildInfo getGuildInfo(Guild guild) {
      return getDatabaseObject(guild.getIdLong());
   }

   public static void enableGameGrouping(Guild guild) {
      GuildInfo gi = getGuildInfo(guild.getIdLong());

      em.getTransaction().begin();
      gi.setGroupMappingsActive(true);
      em.persist(gi);
      em.getTransaction().commit();
   }

   public static void disableGameGrouping(Guild guild) {
      GuildInfo gi = getGuildInfo(guild.getIdLong());

      em.getTransaction().begin();
      gi.setGroupMappingsActive(false);
      em.persist(gi);
      em.getTransaction().commit();
   }

   public static Boolean removeRoleMapping(Long guildId, String gameName) {
      GuildInfo gi = getDatabaseObject(guildId);
      String role;

      em.getTransaction().begin();
      role = gi.removeGameGroupMapping(gameName);
      em.persist(gi);
      em.getTransaction().commit();

      return !(role == null || role.isEmpty());
   }

   public static void addGameRoleMapping(Long guildId, String gameName, String roleName) {
      GuildInfo gi = getDatabaseObject(guildId);

      em.getTransaction().begin();
      gi.addGameGroupMapping(gameName, roleName);
      em.persist(gi);
      em.getTransaction().commit();
   }

   public static void setStreamerRoleId(Long guildId, Long streamerRoleId) {
      GuildInfo gi = getGuildInfo(guildId);

      em.getTransaction().begin();
      gi.setStreamerRoleId(streamerRoleId);
      em.persist(gi);
      em.getTransaction().commit();
   }

   public static Long getStreamerRoleId(Long guildId) {
      return getGuildInfo(guildId).getStreamerRoleId();
   }

   public static void addStreamerMemberId(Long guildId, Long streamerMemberId) {
      GuildInfo gi = getDatabaseObject(guildId);

      em.getTransaction().begin();
      gi.addStreamerMemberId(streamerMemberId);
      em.persist(gi);
      em.getTransaction().commit();
   }

   public static List<Long> getStreamerMemberIds(Long guildId) {
      GuildInfo gi = getDatabaseObject(guildId);
      return gi.getStreamerMemberIds();
   }

   public static void removeStreamerMemberId(Long guildId, Long streamerMemberId) {
      GuildInfo gi = getDatabaseObject(guildId);

      em.getTransaction().begin();
      gi.removeStreamerMemberId(streamerMemberId);
      em.persist(gi);
      em.getTransaction().commit();
   }

   public void setXpTracking(Boolean isActive) {
      em.getTransaction().begin();
      gi.setGrantingMessageXp(isActive);
      em.persist(gi);
      em.getTransaction().commit();
   }

   public void setXpDegradeAmount(Long amount) {
      em.getTransaction().begin();
      gi.setXpDegradeAmount(amount);
      em.persist(gi);
      em.getTransaction().commit();
   }

   public void setXpDegrades(Boolean isActive) {
      em.getTransaction().begin();
      gi.setXpDegrades(isActive);
      em.persist(gi);
      em.getTransaction().commit();
   }
   public static void setLuckBonusActive(Long guildId, Boolean isActive) {
      GuildInfo gi = getGuildInfo(guildId);

      em.getTransaction().begin();
      gi.setLuckBonusActive(isActive);
      em.persist(gi);
      em.getTransaction().commit();
   }

   private static GuildInfo getDatabaseObject(Long guildId) {
      GuildInfo gi = em.find(GuildInfo.class, guildId);

      if (gi == null) {
         em.getTransaction().begin();
         gi = new GuildInfo(guildId);
         em.persist(gi);
         em.getTransaction().commit();
      }

      return gi;
   }
}
