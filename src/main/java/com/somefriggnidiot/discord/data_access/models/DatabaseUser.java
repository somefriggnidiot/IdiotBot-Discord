package com.somefriggnidiot.discord.data_access.models;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class DatabaseUser {

   @Id
   private final Long userId;
   private Integer karma;

   public DatabaseUser(Long userId) {
      this.userId = userId;
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
}
