package com.somefriggnidiot.discord.util;

import com.somefriggnidiot.discord.data_access.models.DatabaseUser;

public class HighscoreObject {
   DatabaseUser user;
   Integer xp;

   public HighscoreObject(DatabaseUser user, Integer xp) {
      this.user = user;
      this.xp = xp;
   }

   public Integer getXp() {
      return xp;
   }

   public DatabaseUser getUser() {
      return user;
   }
}
