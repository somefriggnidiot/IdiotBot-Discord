package com.somefriggnidiot.discord.data_access.models;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BotModeEntry {

   @Id
   private final Instant id;
   private Long guildId;
   private Long channelId;
   private String commandPrefix;

   public BotModeEntry(Long guildId, Long channelId, String commandPrefix) {
      this.id = Instant.now();
      this.guildId = guildId;
      this.channelId = channelId;
      this.commandPrefix = commandPrefix;
   }

   public Instant getId() {
      return id;
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
