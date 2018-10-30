package com.somefriggnidiot.discord.data_access.models;

import javax.persistence.Entity;

@Entity
public class GuildGameMappings {

   private String guildId;
   private String gameName;
   private String groupName;

   public GuildGameMappings(String guildId, String gameName, String groupName) {
      this.guildId = guildId;
      this.gameName = gameName;
      this.groupName = groupName;
   }

   public String getGuildId() {
      return guildId;
   }

   public void setGuildId(String guildId) {
      this.guildId = guildId;
   }

   public String getGameName() {
      return gameName;
   }

   public void setGameName(String gameName) {
      this.gameName = gameName;
   }

   public String getGroupName() {
      return groupName;
   }

   public void setGroupName(String groupName) {
      this.groupName = groupName;
   }
}
