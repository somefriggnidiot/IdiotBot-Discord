package com.somefriggnidiot.discord.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.DatabaseConnector;
import com.somefriggnidiot.discord.data_access.DatabaseConnector.Table;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.data_access.models.UserWarning;
import java.awt.Color;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

public class GetWarningsCommand extends Command {

   public GetWarningsCommand() {
      this.name = "getwarnings";
      this.category = new Category("Moderation");
      this.help = "Returns a list of warnings assigned to a user.";
      this.arguments = "<user>";
      this.aliases = new String[]{"getwarns", "warns"};
   }

   @Override
   protected void execute(CommandEvent event) {
      User target = event.getMessage().getMentionedUsers().get(0);
      EntityManager em = new DatabaseConnector().getEntityManager(Table.DATABASE_USER);

      DatabaseUser dbu = em.find(DatabaseUser.class, target.getIdLong());
      List<UserWarning> warnings = new ArrayList<>();

      if (dbu != null) { //Ensure DBU exists.
         List<String> warningIds = dbu.getWarnings();

         for (String id : warningIds) {
            EntityManager em2 = new DatabaseConnector().getEntityManager(Table.USER_WARNING);
            UserWarning warning = em2.find(UserWarning.class, id);

            if (warning != null) { //Ensure warning is returned.
               warnings.add(warning);
            }
         }
      }

      if (warnings.size() > 0) {
         EmbedBuilder eb = new EmbedBuilder()
             .setTitle(String.format("**Warnings for %s (%s)**",
                 target.getName(),
                 target.getId()))
             .setColor(Color.BLUE)
             .setThumbnail("http://www.foundinaction.com/wp-content/uploads/2018/08/Neon_600x600_Transparent.png")
             .setDescription("Warnings are universal for any user that may have been warned to "
                 + "this bot regardless of guild.")
             .addBlankField(false)
             .addField("Expires - Warned By - Reason", "", true);

         //add warnings as fields
         for (UserWarning warning : warnings) {
            if(warning.getExpires().after(Timestamp.valueOf(LocalDateTime.now()))) {
               eb.addField(warning.getTimestamp().toString(), String.format("%s - %s - %s",
                   warning.getExpires().toString(),
                   event.getGuild().getMemberById(warning.getWarnerId()),
                   warning.getReason()), false);
            }
         }

         event.reply(eb.build());
      }
   }
}
