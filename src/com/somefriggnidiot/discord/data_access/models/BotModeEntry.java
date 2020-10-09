package com.somefriggnidiot.discord.data_access.models;

import javax.persistence.Entity;

@Entity
public class BotModeEntry {

   private Long guildId;
   private Long channelId;
   private String commandPrefix;

   public BotModeEntry(Long guildId, Long channelId, String commandPrefix) {
      this.guildId = guildId;
      this.channelId = channelId;
      this.commandPrefix = commandPrefix;
   }

   public Long getGuildId() {
      return guildId;
   }

   public Long getChannelId() {
      return channelId;
   }

   public String getCommandPrefix() {
      return commandPrefix;
   }
}
