package com.somefriggnidiot.discord.util;

import com.somefriggnidiot.discord.data_access.DatabaseConnector;
import com.somefriggnidiot.discord.data_access.DatabaseConnector.Table;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import javax.persistence.EntityManager;
import net.dv8tion.jda.core.entities.User;

public class KarmaUtil {

   public static Integer updateUser(User user) {
      DatabaseConnector connector = new DatabaseConnector();
      EntityManager em = connector.getEntityManager(Table.DATABASE_USER);
      Integer karma;

      DatabaseUser dbu = em.find(DatabaseUser.class, user.getIdLong());

      if (dbu != null) {
         em.getTransaction().begin();
         karma = dbu.getKarma();
         dbu.setKarma(++karma);
         em.getTransaction().commit();
      } else {
         dbu = new DatabaseUser(user.getIdLong()).withKarma(1);

         em.getTransaction().begin();
         em.persist(dbu);
         em.getTransaction().commit();
         karma = 1;
      }
      return karma;
   }
}
