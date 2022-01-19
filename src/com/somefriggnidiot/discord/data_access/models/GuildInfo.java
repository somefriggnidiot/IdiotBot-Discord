package com.somefriggnidiot.discord.data_access.models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Database storage object used for tracking various non-Discord information about a {@link Guild}.
 */
@Entity
public class GuildInfo {

   @Id
   private final Long guildId;
   private Boolean groupMappingsActive;
   private Boolean groupMappingsAutomatic;
   private Boolean messageXpActive;
   private Boolean luckBonusActive;
   /**
    * Key = Game as displayed on Discord<br />
    * Value = Name of group/role associated with game.
    */
   private HashMap<String, String> gameGroupMappings;
   private Boolean levelRolesActive;
   private HashMap<Long, Integer> roleLevelMappings;
   private double voiceXpMultiplier;
   private List<Long> raffleIds;
   private List<Instant> botModeEntryIds;
   private Long streamerRoleId;
   private List<Long> streamerMemberIds;
   private Long ownerRoleId;
   private Long moderatorRoleId;
   private Long guestRoleId;
   private Long botTextChannelId;
   private Boolean xpDegrades;
   private Long xpDegradeAmount;

   /**
    * Initializes a new GuildInfo object denoted by the given Discord Guild ID.
    *
    * @param guildId The Discord ID of the guild.
    */
   public GuildInfo(Long guildId) {
      this.guildId = guildId;
      this.groupMappingsActive = false;
      this.groupMappingsAutomatic = false;
      this.messageXpActive = false;
      this.luckBonusActive = false;
      this.gameGroupMappings = new HashMap<>();
      this.levelRolesActive = false;
      this.roleLevelMappings = new HashMap<>();
      this.voiceXpMultiplier = 0.0;
      this.raffleIds = new ArrayList<>();
      this.botModeEntryIds = new ArrayList<>();
      this.streamerRoleId = -1L;
      this.streamerMemberIds = new ArrayList<>();
      this.ownerRoleId = 1L;
      this.moderatorRoleId = 1L;
      this.guestRoleId = 1L;
      this.botTextChannelId = 1L;
      this.xpDegrades = false;
      this.xpDegradeAmount = 0L;
   }

   /**
    * @return The Discord ID of the guild.
    */
   public Long getGuildId() {
      return guildId;
   }

   public Boolean isGroupingGames() {
      return groupMappingsActive == null ? false : groupMappingsActive;
   }

   public void setGroupMappingsActive(Boolean isGroupingGames) {
      this.groupMappingsActive = isGroupingGames;
   }

   public Boolean gameGroupsAutomatic() {
      return groupMappingsAutomatic == null ? false : groupMappingsAutomatic;
   }

   public void setGroupMappingsAutomatic(Boolean groupMappingsAutomatic) {
      this.groupMappingsAutomatic = groupMappingsAutomatic;
   }

   /**
    * Associates a game with a role. Users in this guild who start playing a mapped game will be
    * automatically added to that role.
    *
    * @param gameName The name of the game as displayed on Discord.
    * @param roleName The name of the role within the guild that users should be added to.
    */
   public void addGameGroupMapping(String gameName, String roleName) {
      gameGroupMappings.put(gameName, roleName);
   }

   /**
    * Removes a mapping from the list of game group mappings.
    *
    * @param gameName The display name (on Discord) of the game being unmapped from a role.
    * @return The name of the role that was previously associated with the provided game.
    */
   public String removeGameGroupMapping(String gameName) {
      return gameGroupMappings.remove(gameName);
   }

   /**
    * Key = Game as displayed on Discord Value = Name of group/role associated with game.
    *
    * @return a map containing game names as keys and role names as values.
    */
   public HashMap<String, String> getGameGroupMappings() {
      return gameGroupMappings == null ? new HashMap<>() : gameGroupMappings;
   }

   public Boolean isGrantingMessageXp() {
      return messageXpActive == null ? false : messageXpActive;
   }

   public void setGrantingMessageXp(Boolean messageXpActive) {
      this.messageXpActive = messageXpActive;
   }

   public Boolean isDegradingXp() {
      return xpDegrades == null ? false : xpDegrades;
   }

   public void setXpDegrades(Boolean isActive) {
      this.xpDegrades = isActive;
   }

   public Long getXpDegradeAmount() {
      return xpDegradeAmount == null ? 0L : xpDegradeAmount;
   }

   public void setXpDegradeAmount(Long amount) {
      this.xpDegradeAmount = amount;
   }

   public Boolean luckBonusActive() {
      return luckBonusActive == null ? false : luckBonusActive;
   }

   public void setLuckBonusActive(Boolean luckBonusActive) {
      this.luckBonusActive = luckBonusActive;
   }

   public Boolean getLevelRolesActive() {
      levelRolesActive = levelRolesActive == null ? false : levelRolesActive;
      return levelRolesActive;
   }

   /**
    *
    * @return
    */
   public HashMap<Long, Integer> getRoleLevelMappings() {
      return roleLevelMappings == null ? new HashMap<>() : roleLevelMappings;
   }

   public void addRoleLevelMapping(Long roleId, Integer level) {
      roleLevelMappings = roleLevelMappings == null ? new HashMap<>() : roleLevelMappings;
      roleLevelMappings.put(roleId, level);
   }

   public Integer removeRoleLevelMapping(Long roleId) {
      roleLevelMappings = roleLevelMappings == null ? new HashMap<>() : roleLevelMappings;
      return roleLevelMappings.remove(roleId);
   }

   public void setVoiceXpMultiplier(Double multiplier) {
      this.voiceXpMultiplier = multiplier;
   }

   public double getVoiceXpMultiplier() {
      return voiceXpMultiplier;
   }

   public List<Long> getRaffleIds() {
      return raffleIds == null ? new ArrayList<>() : raffleIds;
   }

   public void setRaffleIds(List<Long> raffleIds) {
      this.raffleIds = raffleIds;
   }

   public List<Instant> getBotModeEntryIds() {
      return botModeEntryIds == null ? new ArrayList<>() : botModeEntryIds;
   }

   public void setBotModeEntryIds(List<Instant> botModeEntryIds) {
      this.botModeEntryIds = botModeEntryIds;
   }

   public void setStreamerRoleId(Long streamerRoleId) {
      this.streamerRoleId = streamerRoleId;
   }

   public Long getStreamerRoleId() {
      return streamerRoleId == null ? 0 : streamerRoleId;
   }

   public void setStreamerMemberIds(List<Long> streamerMemberIds) {
      this.streamerMemberIds = streamerMemberIds;
   }

   public List<Long> getStreamerMemberIds() {
      return streamerMemberIds;
   }

   public void addStreamerMemberId(Long streamerMemberId) {
      streamerMemberIds = getStreamerMemberIds() == null ? new ArrayList<>() : streamerMemberIds;
      if (!streamerMemberIds.contains(streamerMemberId)) {
         streamerMemberIds.add(streamerMemberId);
      }
      setStreamerMemberIds(streamerMemberIds);
   }

   public Boolean removeStreamerMemberId(Long streamerMemberId) {
      streamerMemberIds = streamerMemberIds == null ? new ArrayList<>() : streamerMemberIds;
      return streamerMemberIds.remove(streamerMemberId);
   }

   public Long getOwnerRoleId() {
      return ownerRoleId == null ? 1L : ownerRoleId;
   }

   public void setOwnerRoleId(Long ownerRoleId) {
      this.ownerRoleId = ownerRoleId;
   }

   public Long getModeratorRoleId() {
      return moderatorRoleId == null ? 1L : moderatorRoleId;
   }

   public void setModeratorRoleId(Long moderatorRoleId) {
      this.moderatorRoleId = moderatorRoleId;
   }

   public Long getGuestRoleId() {
      return guestRoleId == null ? 1L : guestRoleId;
   }

   public void setGuestRoleId(Long guestroleId) {
      this.guestRoleId = guestroleId;
   }

   public void setBotTextChannelId(Long botTextChannelId) {
      this.botTextChannelId = botTextChannelId;
   }

   public Long getBotTextChannelId() {
      return botTextChannelId == null ? 1L : botTextChannelId;
   }

   @Override
   public String toString() {
      return String.format("%s: IsTracking=%s: Mappings=%s",
          guildId,
          groupMappingsActive.toString(),
          gameGroupMappings.toString());
   }
}
