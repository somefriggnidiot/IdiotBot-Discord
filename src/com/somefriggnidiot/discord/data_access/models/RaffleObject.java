package com.somefriggnidiot.discord.data_access.models;

import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import java.util.HashMap;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class RaffleObject {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   private Long guildId;
   private String raffleName;
   private Integer ticketCost;
   private Integer maxTicketsPerPerson;
   /**
    * Long = User ID,
    * Integer = Tickets
    */
   private HashMap<Long, Integer> entryMap;
   private Boolean isActive;
   private Long requiredRoleId;

   public RaffleObject(Long guildId, String raffleName, Integer ticketCost,
       Integer maxTicketsPerPerson, Long requiredRoleId) {
      this.id = new GuildInfoUtil(guildId).getRaffleIds().size() + 1L;
      this.guildId = guildId;
      this.raffleName = raffleName;
      this.ticketCost = ticketCost;
      this.maxTicketsPerPerson = maxTicketsPerPerson;
      this.entryMap = new HashMap<>();
      this.isActive = true;
      this.requiredRoleId = requiredRoleId;
   }

   public Long getId() {
      return id;
   }

   public Long getGuildId() {
      return guildId;
   }

   public String getRaffleName() {
      return raffleName;
   }

   public Integer getTicketCost() {
      return ticketCost;
   }

   public void setTicketCost(Integer ticketCost) {
      this.ticketCost = ticketCost;
   }

   public Integer getMaxTicketsPerPerson() {
      return maxTicketsPerPerson;
   }

   public void setMaxTicketsPerPerson(Integer maxTicketsPerPerson) {
      this.maxTicketsPerPerson = maxTicketsPerPerson;
   }

   public void setUserTickets(Long userId, Integer tickets) {
      entryMap.put(userId, tickets);
   }

   public Integer removeUserTickets(Long userId) {
      return entryMap.remove(userId);
   }

   public HashMap<Long, Integer> getEntryMap() {
      return entryMap;
   }

   public void setEntryMap(HashMap<Long, Integer> entryMap) {
      this.entryMap = entryMap;
   }

   public Boolean isActive() {
      return isActive;
   }

   public void setActive(Boolean isActive) {
      this.isActive = isActive;
   }

   public Long getRequiredRoleId() {
      return requiredRoleId;
   }
}
