package com.somefriggnidiot.discord.util;

import static java.lang.String.format;

import com.somefriggnidiot.discord.core.Main;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.DatabaseUserUtil;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimerTask;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XpDegradationUtil {

   private static final Logger logger = LoggerFactory.getLogger(XpDegradationUtil.class);

   public static void startDegraderDaemon() {
      Calendar timeOfDay = Calendar.getInstance();
      timeOfDay.set(Calendar.HOUR_OF_DAY, 0);
      timeOfDay.set(Calendar.MINUTE, 30);
      timeOfDay.set(Calendar.SECOND, 0);

      new DailyRunnerDaemon(timeOfDay, getXpDegraderTask(), "degrader").start();
      logger.info("Started daemon for daily XP degradation.");
   }

   private static TimerTask getXpDegraderTask() {
      return new TimerTask() {

         @Override
          public void run() {
            List<Guild> guilds = Main.jda.getGuilds();
            List<Guild> guildsDegrading = new ArrayList<>();

            for (Guild guild : guilds) {
               GuildInfo gi = GuildInfoUtil.getGuildInfo(guild);
               if (gi.isDegradingXp()) {
                  guildsDegrading.add(guild);
               }
            }

            for (Guild guildDegrading : guildsDegrading) {
               logger.info(format("[%s] Started daily XP degradation.", guildDegrading));
               GuildInfo gi = GuildInfoUtil.getGuildInfo(guildDegrading);
               Long degradeValue = gi.getXpDegradeAmount();

               Integer degradeAmountAbs = gi.getXpDegradeAmount().intValue();

               List<Member> members = Main.jda.getGuildById(guildDegrading.getIdLong())
                   .getMembers();

               Integer membersDegraded = 0;
               for (Member member : members) {
                  DatabaseUser dbu = DatabaseUserUtil.getUser(member.getIdLong());
                  Integer xpBefore = dbu.getXpMap().get(guildDegrading.getIdLong()) == null ? 0 :
                      dbu.getXpMap().get(guildDegrading.getIdLong());

                  if (degradeValue == -1L) {
                     //degradeAmountAbs becomes 10*level + 10xp;
                     degradeAmountAbs = XpUtil.getLevelForXp(xpBefore) * 10 +  10;
                  }

                  if (xpBefore != 0) { //Don't bother if the user has no xp already.
                     Integer levelBefore = XpUtil.getLevelForXp(xpBefore);
                     Integer xpAfter = xpBefore - degradeAmountAbs;
                     Integer levelAfter = XpUtil.getLevelForXp(xpAfter);

                     if (xpAfter <= 0) { //Make sure user doesn't go negative.
                        xpAfter = 0;

                        logger.info(format("[%s] %s has run out of XP.",
                            guildDegrading, member.getEffectiveName()));
                     }

                     DatabaseUserUtil.setXp(guildDegrading.getIdLong(),
                         member.getIdLong(),
                         xpAfter,
                         false);

                     //Check for level changes.
                     if (!levelBefore.equals(levelAfter)) {
                        logger.info(format("[%s] %s has lost a level from XP degradation. Was %s,"
                            + " now %s.", guildDegrading, member.getEffectiveName(), levelBefore,
                            levelAfter));
                     }

                     membersDegraded++;
                  } else {
                     // User has no XP. Skip.
                     logger.trace(format("[%s] No XP found for member: %s", guildDegrading,
                         member));
                  }
               }
               XpUtil.updateLevelRoleAssignments(guildDegrading);
               logger.info(format("[%s] Finished degrading XP for %s members.",
                   guildDegrading, membersDegraded));
            }
         }
      };
   }
}
