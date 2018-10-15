package com.somefriggnidiot.discord.data_access.models;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;

@SuppressWarnings("FieldCanBeLocal")
@Entity
public class DatabaseUser {

   @Id
   private final Long userId;
   private Integer karma;
   private Long privateChannelId;
   private List<String> warningIds;

   public DatabaseUser(Long userId) {
      this.userId = userId;
      this.karma = 0;
   }

   public void setKarma(Integer newKarma) {
      this.karma = newKarma;
   }

   public Integer getKarma() {
      return karma;
   }

   public DatabaseUser withKarma(Integer newKarma) {
      this.karma = newKarma;
      return this;
   }

   public void setPrivateChannel(Long channelId) {
      this.privateChannelId = channelId;
   }

   public Long getPrivateChannel() {
      return privateChannelId;
   }

   public DatabaseUser withPrivateChannel(Long channelId) {
      this.privateChannelId = channelId;
      return this;
   }

   public void addWarning(String warning) {
      if (warningIds == null) warningIds = new ArrayList<>();
      warningIds.add(warning);
   }

   public List<String> getWarnings() {
      return warningIds;
   }
}
