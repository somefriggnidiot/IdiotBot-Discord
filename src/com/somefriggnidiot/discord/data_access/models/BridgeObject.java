package com.somefriggnidiot.discord.data_access.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "bridges")
public class BridgeObject {
   @Id
   private Integer id;
   @Column(name = "discord_server_id")
   private Long discordServerId;
   @Column(name = "discord_channel_id")
   private Long discordChannelId;
   @Column(name = "guilded_server_id")
   private String guildedServerId;
   @Column(name = "guilded_channel_id")
   private String guildedChannelId;

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public Long getDiscordServerId() {
      return discordServerId;
   }

   public void setDiscordServerId(Long discordServerId) {
      this.discordServerId = discordServerId;
   }

   public Long getDiscordChannelId() {
      return discordChannelId;
   }

   public void setDiscordChannelId(Long discordChannelId) {
      this.discordChannelId = discordChannelId;
   }

   public String getGuildedServerId() {
      return guildedServerId;
   }

   public void setGuildedServerId(String guildedServerId) {
      this.guildedServerId = guildedServerId;
   }

   public String getGuildedChannelId() {
      return guildedChannelId;
   }

   public void setGuildedChannelId(String guildedChannelId) {
      this.guildedChannelId = guildedChannelId;
   }
}
