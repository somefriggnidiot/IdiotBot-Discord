package com.somefriggnidiot.discord.data_access.models;

import java.util.HashMap;
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
   /**
    * Key = Game as displayed on Discord
    * Value = Name of group/role associated with game.
    */
   private HashMap<String, String> gameGroupMappings;

   /**
    * Initializes a new GuildInfo object denoted by the given Discord Guild ID.
    * @param guildId The Discord ID of the guild.
    */
   public GuildInfo(Long guildId) {
      this.guildId = guildId;
      this.groupMappingsActive = false;
      gameGroupMappings = new HashMap<>();
   }

   /**
    * @return The Discord ID of the guild.
    */
   public Long getGuildId() {
      return guildId;
   }

   public Boolean isGroupingGames() {
      return groupMappingsActive;
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
    * Key = Game as displayed on Discord
    * Value = Name of group/role associated with game.
    * @return a map containing game names as keys and role names as values.
    */
   public HashMap<String, String> getGameGroupMappings() {
      return gameGroupMappings;
   }

   @Override
   public String toString() {
      return String.format("%s: IsTracking=%s: Mappings=%s",
          guildId,
          groupMappingsActive.toString(),
          gameGroupMappings.toString());
   }
}
