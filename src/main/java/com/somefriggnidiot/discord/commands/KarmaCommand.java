package com.somefriggnidiot.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.DatabaseConnector;
import com.somefriggnidiot.discord.data_access.DatabaseConnector.Table;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.events.MessageListener;
import javax.persistence.EntityManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KarmaCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   public KarmaCommand() {
      this.name = "karma";
      this.help = "Gives another user a fake point.";
      this.arguments = "<user>";
      this.cooldownScope = CooldownScope.USER;
      this.cooldown = 30;
      this.botPermissions = new Permission[]{Permission.MESSAGE_READ};
   }

   @Override
   protected void execute(final CommandEvent event) {
      User user = event.getMessage().getMentionedUsers().get(0);
      Integer karma = updateDatabase(user);
      event.reply(String.format("%s now has %s karma!", user.getName(), karma));

      logger.info(String.format("[%s] Karma given to %s by %s.",
          event.getGuild(),
          event.getAuthor().getName(),
          event.getMessage().getMentionedUsers().get(0).getName()));
   }

   private Integer updateDatabase(User user) {
      DatabaseConnector connector = new DatabaseConnector();
      EntityManager em = connector.getEntityManager(Table.DATABASE_USER);
      Integer karma = 0;

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
