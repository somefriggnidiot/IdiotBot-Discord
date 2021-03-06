package com.somefriggnidiot.discord.data_access.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
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
   private HashMap<Long, Integer> xpMap;
   private HashMap<Long, Integer> levelMap;
   private HashMap<Long, Integer> tokenMap;
   private Timestamp lastMessageDtTm;

   public DatabaseUser(Long userId) {
      this.userId = userId;
      this.karma = 0;
      this.level = 0;
      this.xpMap = new HashMap<>();
      this.levelMap = new HashMap<>();
      this.tokenMap = new HashMap<>();
   }

   public Long getId() {
      return userId;
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

   public HashMap<Long, Integer> getXpMap() {
      return xpMap == null ? new HashMap<>() : xpMap;
   }

   public void setXp(HashMap<Long, Integer> xpMap) {
      this.xpMap = xpMap;
   }

   public Integer updateXp(Long guldId, Integer newXpValue) {
      xpMap = xpMap == null ? new HashMap<>() : xpMap;
      return xpMap.put(guldId, newXpValue);
   }

   public DatabaseUser withXpMap(HashMap<Long, Integer> xpMap) {
      this.xpMap = xpMap;
      return this;
   }

   /**
    * Fetches the map of a user's level for each guild.
    * @return a HashMap where the guildId functions as the key and the user's level within that
    * guild is the value.
    */
   public HashMap<Long, Integer> getLevelMap() {
      return levelMap == null ? new HashMap<>() : levelMap;
   }

   public void setLevelMap(HashMap<Long, Integer> levelMap) {
      this.levelMap = levelMap;
   }

   public Integer updateLevel(Long guildId, Integer newLevelValue) {
      levelMap = levelMap == null ? new HashMap<>() : levelMap;
      return levelMap.put(guildId, newLevelValue);
   }

   public HashMap<Long, Integer> getTokenMap() {
      return tokenMap == null ? new HashMap<>() : tokenMap;
   }

   public void setTokenMap(HashMap<Long, Integer> tokenMap) {
      this.tokenMap = tokenMap;
   }

   public Integer updateTokens(Long guldId, Integer newTokenValue) {
      tokenMap = tokenMap == null ? new HashMap<>() : tokenMap;
      return tokenMap.put(guldId, newTokenValue);
   }

   public DatabaseUser withTokenMap(HashMap<Long, Integer> tokenMap) {
      this.tokenMap = tokenMap;
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
}
