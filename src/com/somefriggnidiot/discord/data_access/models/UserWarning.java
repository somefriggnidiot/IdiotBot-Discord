package com.somefriggnidiot.discord.data_access.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UserWarning {

   @Id
   private final String id;
   private Long userId;
   private Timestamp timestamp;
   private String reason;
   private Timestamp expires;
   private Long warnerId;

   public UserWarning(Long userId, String reason, Long warnerId) {
      this.id = UUID.randomUUID().toString();
      this.userId = userId;
      this.reason = reason;
      this.timestamp = Timestamp.valueOf(LocalDateTime.now());
      this.expires = Timestamp.valueOf(LocalDateTime.now().plusDays(30));
      this.warnerId = warnerId;
   }

   public String getId() {
      return id;
   }

   public String getReason() {
      return reason;
   }

   public Timestamp getExpires() {
      return expires;
   }

   public Timestamp getTimestamp() {
      return timestamp;
   }

   public Long getWarnerId() {
      return warnerId;
   }
}
