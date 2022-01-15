package com.somefriggnidiot.discord.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DailyRunnerDaemon {
   private final Runnable dailyTask;
   private final int hour;
   private final int minute;
   private final int second;
   private final String runThreadName;

   /**
    * Starts a daemon to run a scheduled task at a 24-hour interval.
    *
    * @param timeOfDay Calendar time-of-day for when the task should run each day.
    * @param dailyTask the Runnable task to be executed each day.
    * @param runThreadName name of the thread for the daily task to be run on.
    */
   DailyRunnerDaemon(Calendar timeOfDay, Runnable dailyTask, String runThreadName) {
      this.dailyTask = dailyTask;
      this.hour = timeOfDay.get(Calendar.HOUR_OF_DAY);
      this.minute = timeOfDay.get(Calendar.MINUTE);
      this.second = timeOfDay.get(Calendar.SECOND);
      this.runThreadName = runThreadName;
   }

   public void start() {
      startTimer();
   }

   private void startTimer() {
      new Timer(runThreadName, true).schedule(new TimerTask() {
         @Override
         public void run() {
            dailyTask.run();
            startTimer();
         }
      }, getNextRunTime());
   }

   private Date getNextRunTime() {
      Calendar startTime = Calendar.getInstance();
      Calendar now = Calendar.getInstance();
      startTime.set(Calendar.HOUR_OF_DAY, hour);
      startTime.set(Calendar.MINUTE, minute);
      startTime.set(Calendar.SECOND, second);
      startTime.set(Calendar.MILLISECOND, 0);

      if(startTime.before(now) || startTime.equals(now)) {
         startTime.add(Calendar.DATE, 1);
      }

      return startTime.getTime();
   }
}
