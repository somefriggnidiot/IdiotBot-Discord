package com.somefriggnidiot.discord.util;

import com.somefriggnidiot.discord.data_access.DatabaseConnector;
import com.somefriggnidiot.discord.data_access.DatabaseConnector.Table;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import javax.persistence.EntityManager;
import net.dv8tion.jda.core.entities.User;

public class KarmaUpdate {

   public static Integer updateUser(User user, Boolean addKarma) {
      Integer karma = 0;
      DatabaseConnector dbc = new DatabaseConnector();
      EntityManager em = dbc.getEntityManager(Table.DATABASE_USER);
      DatabaseUser dbu = em.find(DatabaseUser.class, user.getIdLong());

      if (dbu != null) {
         em.getTransaction().begin();
         karma = dbu.getKarma();
         karma = addKarma ? ++karma : --karma;
         dbu.setKarma(karma);
         em.getTransaction().commit();
      } else {
         karma = addKarma ? 1 : -1;
         dbu = new DatabaseUser(user.getIdLong()).withKarma(karma);

         em.getTransaction().begin();
         em.persist(dbu);
         em.getTransaction().commit();
      }

      return karma;
   }


}
