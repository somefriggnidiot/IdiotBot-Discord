package com.somefriggnidiot.discord.data_access.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import net.dv8tion.jda.core.entities.Guild;

/**
 * Database storage object used for tracking various non-Discord information about a {@link Guild}.
 */
@Entity
public class GuildInfo {

   @Id
   private final Long guildId;
   private Boolean groupMappingsActive;
   private Boolean messageXpActive;

   /**
    * Key = Game as displayed on Discord Value = Name of group/role associated with game.
    */
   private HashMap<String, String> gameGroupMappings;
   private Boolean levelRolesActive;
   private HashMap<Long, Integer> roleLevelMappings;
   private double voiceXpMultiplier;
   private List<Long> raffleIds;

   /**
    * Initializes a new GuildInfo object denoted by the given Discord Guild ID.
    *
    * @param guildId The Discord ID of the guild.
    */
   public GuildInfo(Long guildId) {
      this.guildId = guildId;
      this.groupMappingsActive = false;
      this.messageXpActive = false;
      this.gameGroupMappings = new HashMap<>();
      this.levelRolesActive = false;
      this.roleLevelMappings = new HashMap<>();
      this.voiceXpMultiplier = 0.0;
      this.raffleIds = new ArrayList<>();
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

   public Boolean getLevelRolesActive() {
      levelRolesActive = levelRolesActive == null ? false : levelRolesActive;
      return levelRolesActive;
   }

   public void setLevelRolesActive(Boolean isActive) {
      this.levelRolesActive = isActive;
   }

   public void setRoleLevelMappings(HashMap<Long, Integer> roleLevelMappings) {
      this.roleLevelMappings = roleLevelMappings;
   }

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

   @Override
   public String toString() {
      return String.format("%s: IsTracking=%s: Mappings=%s",
          guildId,
          groupMappingsActive.toString(),
          gameGroupMappings.toString());
   }
}
