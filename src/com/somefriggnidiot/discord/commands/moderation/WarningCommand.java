package com.somefriggnidiot.discord.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.DatabaseConnector;
import com.somefriggnidiot.discord.data_access.DatabaseConnector.Table;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.data_access.models.UserWarning;
import com.somefriggnidiot.discord.events.MessageListener;
import javax.persistence.EntityManager;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarningCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   public WarningCommand() {
      this.name = "warn";
      this.category = new Category("Moderation");
      this.help = "Adds a warning to a user.";
      this.arguments = "<user> <reason>";
      this.requiredRole = "Staff";
   }

   @Override
   protected void execute(CommandEvent event) {
      User target = event.getMessage().getMentionedUsers().get(0);
      String reason = event.getMessage().getContentDisplay().split("\\s", 3)[2];
      EntityManager em = new DatabaseConnector().getEntityManager(Table.DATABASE_USER);
      EntityManager em2 = new DatabaseConnector().getEntityManager(Table.USER_WARNING);

      DatabaseUser dbu = em.find(DatabaseUser.class, target.getIdLong());
      UserWarning warning = new UserWarning(target.getIdLong(), reason, event.getAuthor().getIdLong());

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

      Integer totalWarnings = dbu.getWarnings().size();

      logger.info(String.format("[%s] %s has warned %s for \"%s\". %s now has %s warnings.",
          event.getGuild(),
          event.getAuthor().getName(),
          target.getName(),
          reason,
          target.getName(),
          totalWarnings));

      target.openPrivateChannel().queue(success ->
          {
             MessageAction messageAction = success
                 .sendMessage(String.format("You have been warned for \"%s\" in %s. \n"
                         + "You have %s total warning%s now.",
                     reason,
                     event.getGuild().getName(),
                     totalWarnings,
                     totalWarnings > 1 ? "s" : ""));

             messageAction.queue();
          }
      );
   }
}
