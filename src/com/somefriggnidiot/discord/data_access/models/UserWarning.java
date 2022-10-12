package com.somefriggnidiot.discord.data_access.models;

import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;

@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal"})
@Entity
public class UserWarning {

   @Id
   private String id;
   private Long userId;
   private Timestamp warnStart;
   private Long warnerId;
   private String reason;
   private Timestamp expires;

   public UserWarning(Long userId, String reason, Long warnerId, Timestamp warnStart, Timestamp
       expires) {
      this.id = UUID.randomUUID().toString();
      this.userId = userId;
      this.reason = reason;
      this.warnStart = warnStart;
      this.expires = expires;
      this.warnerId = warnerId;
   }

   public String getId() {
      return id;
   }

   public Long getUserId() {
      return userId;
   }

   public String getReason() {
      return reason;
   }

   public Timestamp getExpires() {
      return expires;
   }

   public Timestamp getTimestamp() {
      return warnStart;
   }

   public Long getWarnerId() {
      return warnerId;
   }
}
