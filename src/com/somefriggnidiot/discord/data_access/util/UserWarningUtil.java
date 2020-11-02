package com.somefriggnidiot.discord.data_access.util;

import com.somefriggnidiot.discord.data_access.DatabaseConnector;
import com.somefriggnidiot.discord.data_access.DatabaseConnector.Table;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.data_access.models.UserWarning;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.persistence.EntityManager;
import net.dv8tion.jda.api.entities.User;

public class UserWarningUtil {

   public static Integer warnUser(User target, String reason, Long authorId) {
      EntityManager em = new DatabaseConnector().getEntityManager(Table.DATABASE_USER);
      EntityManager em2 = new DatabaseConnector().getEntityManager(Table.USER_WARNING);

      DatabaseUser dbu = em.find(DatabaseUser.class, target.getIdLong());
      UserWarning warning = new UserWarning(target.getIdLong(), reason, authorId,
          Timestamp.from(Instant.now()),
          Timestamp.from(Instant.now().plus(30, ChronoUnit.DAYS)));

      if (dbu != null) {
         em.getTransaction().begin();
         dbu.addWarning(warning.getId());
         em.persist(dbu);
         em.getTransaction().commit();

         em2.getTransaction().begin();
         em2.persist(warning);
         em2.getTransaction().commit();
      } else {
         dbu = new DatabaseUser(target.getIdLong());

         em.getTransaction().begin();
         dbu.addWarning(warning.getId());
         em.persist(dbu);
         em.getTransaction().commit();

         em2.getTransaction().begin();
         em2.persist(warning);
         em2.getTransaction().commit();
      }

      return dbu.getWarnings().size();
   }

   public static UserWarning getWarning(String warningId) {
      EntityManager em = new DatabaseConnector().getEntityManager(Table.USER_WARNING);

      UserWarning warning = em.find(UserWarning.class, warningId);

      return warning;
   }
}
