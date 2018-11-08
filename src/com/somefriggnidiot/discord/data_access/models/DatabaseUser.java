package com.somefriggnidiot.discord.data_access.models;

import java.sql.Timestamp;
import java.time.Instant;
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
   private Integer level;
   private Integer xp;
   private Timestamp lastMessageDtTm;

   public DatabaseUser(Long userId) {
      this.userId = userId;
      this.karma = 0;
      this.level = 0;
      this.xp = 0;
   }

   public Integer getKarma() {
      return karma;
   }

   public void setKarma(Integer newKarma) {
      this.karma = newKarma;
   }

   public DatabaseUser withKarma(Integer newKarma) {
      this.karma = newKarma;
      return this;
   }

   public Long getPrivateChannel() {
      return privateChannelId;
   }

   public void setPrivateChannel(Long channelId) {
      this.privateChannelId = channelId;
   }

   public DatabaseUser withPrivateChannel(Long channelId) {
      this.privateChannelId = channelId;
      return this;
   }

   public void addWarning(String warning) {
      if (warningIds == null) {
         warningIds = new ArrayList<>();
      }
      warningIds.add(warning);
   }

   public List<String> getWarnings() {
      return warningIds;
   }

   public Integer getXp() {
      return xp;
   }

   public void setXp(Integer xp) {
      this.xp = xp;
   }

   public DatabaseUser withXp(Integer xp) {
      this.xp = xp;
      return this;
   }

   public Timestamp getLastMessageDtTm() {
      return lastMessageDtTm;
   }

   public void setLastMessageDtTm(Timestamp lastMessageDtTm) {
      this.lastMessageDtTm = lastMessageDtTm;
   }

   public DatabaseUser withUpdatedDtTm(Timestamp updatedDtTm) {
      this.lastMessageDtTm = updatedDtTm;
      return this;
   }

   public Integer getLevel() {
      return level;
   }

   public void setLevel(Integer level) {
      this.level = level;
   }

   public DatabaseUser withLevel(Integer level) {
      this.level = level;
      return this;
   }
}
