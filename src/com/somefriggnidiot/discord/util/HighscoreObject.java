package com.somefriggnidiot.discord.util;

import com.somefriggnidiot.discord.data_access.models.DatabaseUser;

/**
 * Model used to depict an entry on the XP Leaderboard.
 *
 * @see com.somefriggnidiot.discord.commands.functionalities.xp.xpinfo.XpLeaderboardCommand
 */
public class HighscoreObject {
   private DatabaseUser user;
   private Integer xp;

   /**
    * Creates a new {@link HighscoreObject} mapping a {@link DatabaseUser} to an XP value.
    *
    * @param user the {@link DatabaseUser}.
    * @param xp the {@code DatabaseUser}'s XP balance.
    */
   public HighscoreObject(DatabaseUser user, Integer xp) {
      this.user = user;
      this.xp = xp;
   }

   /**
    * @return the XP balance of the {@link HighscoreObject}.
    */
   public Integer getXp() {
      return xp;
   }

   /**
    * @return the {@link DatabaseUser} mapped to this {@code HighscoreObject}.
    */
   public DatabaseUser getUser() {
      return user;
   }
}
