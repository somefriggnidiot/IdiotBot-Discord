package com.somefriggnidiot.discord.data_access.util;

import com.somefriggnidiot.discord.core.Main;
import com.somefriggnidiot.discord.data_access.DatabaseConnector;
import com.somefriggnidiot.discord.data_access.DatabaseConnector.Table;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.models.RaffleObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaffleUtil {

   private static EntityManager raffleManager = new DatabaseConnector().getEntityManager(Table.RAFFLE);
   private static EntityManager guildManager = new DatabaseConnector()
       .getEntityManager(Table.GUILD_INFO);
   private Logger logger = LoggerFactory.getLogger(RaffleUtil.class);
   private RaffleObject raffle;
   private GuildInfo gi;

   public RaffleUtil(Long raffleId) {
      this.raffle = getRaffleObject(raffleId);
      this.gi = GuildInfoUtil.getGuildInfo(raffle.getGuildId());
   }

   public Long getRaffleId() {
      return raffle.getId();
   }

   public HashMap<Long, Integer> getRaffleEntryMap() {
      return raffle.getEntryMap();
   }

   public Boolean raffleIsActive() {
      return raffle.isActive();
   }

   public void closeRaffle() {
      raffleManager.getTransaction().begin();
      raffle.setActive(false);
      raffleManager.persist(raffle);
      raffleManager.getTransaction().commit();
   }

   public Integer getEntriesForUser(User user) {
      Integer entries =  raffle.getEntryMap().get(user.getIdLong());

      return entries == null ? 0 : entries;
   }

   /**
    * Creates a new raffle.
    *
    * @param guildId The ID of the guild the raffle is beign created in.
    * @param raffleName The name/description given to the raffle.
    * @param ticketCost The cost of each ticket/entry for the raffle.
    * @param maxTicketsPerPerson The maximum entries a single user may hold.
    * @return The newly created {@link RaffleObject}
    */
   public static RaffleObject createRaffle(Long guildId, String raffleName,
       Long requiredRoleId, Integer ticketCost, Integer maxTicketsPerPerson) {
      RaffleObject raffle = new RaffleObject(guildId, raffleName, ticketCost,
          maxTicketsPerPerson, requiredRoleId);
      GuildInfoUtil giu = new GuildInfoUtil(guildId);

      raffleManager.getTransaction().begin();
      raffleManager.persist(raffle);
      giu.addRaffleId(raffle.getId());
      raffleManager.getTransaction().commit();

      return raffle;
   }

   /**
    * Handles logic for a user "purchasing" entries for a raffle. Users will not be able to
    * purchase more than the maximum entries for the raffle or more entries than they have tokens
    * for.
    *
    * @param user The user purchasing tickets.
    * @param addedEntries The number of tickets being purchased.
    * @return the actual amount of entries purchased.
    */
   public Integer addEntriesForUser(User user, Integer addedEntries) {
      Integer currentEntries = getEntriesForUser(user);
      Integer maxEntries = raffle.getMaxTicketsPerPerson();
      Integer effectiveEntries;
      DatabaseUserUtil dbuu = new DatabaseUserUtil(user.getIdLong());

      //We'll be under or at max.
      if (currentEntries + addedEntries <= maxEntries) {
         effectiveEntries = addedEntries;
      } else { //We're going over max.
         effectiveEntries = maxEntries - currentEntries;
      }

      if (effectiveEntries > 0) {
         //Add entries.
         Integer totalTokenCost = effectiveEntries * raffle.getTicketCost();
         Integer tokenBalance = dbuu.getTokens(raffle.getGuildId());

         if (tokenBalance < raffle.getTicketCost()) {
            //Balance too low to buy any tickets.

            logger.info(String.format("[%s] %s attempted to buy %s tickets (%s tokens each, %s "
                + "total) for the \"%s\" raffle, but only had %s tokens.",
                Main.jda.getGuildById(raffle.getGuildId()),
                user.getName(),
                addedEntries,
                raffle.getTicketCost(),
                totalTokenCost,
                raffle.getRaffleName(),
                tokenBalance));

            return 0;
         } else if (tokenBalance < totalTokenCost) {
            //Not enough for all tickets.
            Integer maxBuyable = tokenBalance / raffle.getTicketCost();
            totalTokenCost = maxBuyable * raffle.getTicketCost();

            dbuu.addTokens(raffle.getGuildId(), 0 - totalTokenCost);
            raffle.setUserTickets(user.getIdLong(), currentEntries + maxBuyable);

            logger.info(String.format("[%s] %s attempted to buy %s tickets for %s tokens, but "
                    + "could only afford %s tickets for %s tokens.",
                Main.jda.getGuildById(raffle.getGuildId()),
                user.getName(),
                addedEntries,
                addedEntries * raffle.getTicketCost(),
                maxBuyable,
                totalTokenCost));
         } else if (tokenBalance >= totalTokenCost) {
            //Can buy all tickets.

            //Deduct tokens, then update ticket count.
            dbuu.addTokens(raffle.getGuildId(), 0 - totalTokenCost);
            raffle.setUserTickets(user.getIdLong(), currentEntries + effectiveEntries);

            logger.info(String.format("[%s] %s purchased %s tickets for %s tokens.",
                Main.jda.getGuildById(raffle.getGuildId()),
                user.getName(),
                effectiveEntries,
                totalTokenCost));
         }
      } else {
         //No entries added.
         logger.info(String.format("[%s] %s attempted to buy %s tickets, but already had %s. "
             + "Maximum for this raffle is %s.",
             Main.jda.getGuildById(raffle.getGuildId()),
             user.getName(),
             addedEntries,
             currentEntries,
             maxEntries));
      }

      raffleManager.getTransaction().begin();
      raffleManager.persist(raffle);
      logger.debug("Saving raffle object.");
      raffleManager.getTransaction().commit();

      return effectiveEntries;
   }

   /**
    * Draws winnings tickets for a raffle. Allows the caller to specify how many tickets to draw,
    * and whether a single user may win multiple times.
    *
    * @param quantity The amount of wining tickets to draw.
    * @param usersCanWinMultiple Whether or not a user may win multiple times for a single
    * raffle. <br/>
    * If true, only the winning entry is removed before the next drawing. <br/>
    * If false, all entries belonging to the owner of the winning ticket are removed before the
    * next drawing.
    * @return a {@link List} of User IDs belonging to the winning entries.
    */
   public List<Long> draw(Integer quantity, Boolean usersCanWinMultiple) {
      List<Long> winners = new ArrayList<>();
      List<Long> entries = new ArrayList<>();
      Integer entrySize = 0;

      if (usersCanWinMultiple) {
         //Get total number of entries by totalling all users' entry counts.
         for (Integer userEntryCount : raffle.getEntryMap().values()) {
            entrySize+= userEntryCount;
         }

         //Add user's ID to list of entries for each ticket they have.
         for (Long userId : raffle.getEntryMap().keySet()) {
            for (int i = 0 ; i < raffle.getEntryMap().get(userId); i++) {
               entries.add(userId);
            }
         }

         //Draw tickets, removing single entry each time.
         for (int i = 0 ; i < quantity ; i++) {
            try {
               Integer winningTicket = ThreadLocalRandom.current().nextInt(0, entrySize--);
               winners.add(entries.remove(winningTicket.intValue()));
            } catch (Exception e) {
               logger.error(String.format("[%s] Tried to draw %s raffle entries, only had %s.",
                   Main.jda.getGuildById(gi.getGuildId()),
                   quantity,
                   entrySize));
            }
         }
      } else {
         HashMap<Long, Integer> modifiedEntriesMap = raffle.getEntryMap();
         //For each drawing,
         for (int i = 0; i < quantity ; i++) {
            //Get total number of entries by totalling all users' entry counts.
            for (Integer userEntryCount : modifiedEntriesMap.values()) {
               entrySize+= userEntryCount;
            }

            //Add user's ID to list of entries for each ticket they have.
            for (Long userId : raffle.getEntryMap().keySet()) {
               for (int j = 0 ; j < raffle.getEntryMap().get(userId); j++) {
                  entries.add(userId);
               }
            }

            //Draw tickets, removing single entry each time.
            for (int k = 0 ; k < quantity ; k++) {
               Integer winningTicket = ThreadLocalRandom.current().nextInt(0, entrySize--);
               Long winnerId = entries.get(winningTicket);

               winners.add(winnerId);
               modifiedEntriesMap.remove(winnerId);
            }
         }
      }

      return winners;
   }

   public RaffleObject getRaffle() {
      return raffle;
   }

   private RaffleObject getRaffleObject(Long raffleId) {
      return raffleManager.find(RaffleObject.class, raffleId);
   }

   public static List<RaffleObject> getRaffles(Boolean active) {
      List<RaffleObject> raffleList;

      raffleList = raffleManager
          .createQuery("SELECT r FROM RaffleObject r", RaffleObject.class)
          .getResultList();

      raffleList = raffleList.stream().filter(RaffleObject::isActive)
          .collect(Collectors.toList());

      return raffleList;
   }
}
