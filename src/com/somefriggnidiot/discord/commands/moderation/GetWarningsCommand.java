package com.somefriggnidiot.discord.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.DatabaseConnector;
import com.somefriggnidiot.discord.data_access.DatabaseConnector.Table;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.data_access.models.UserWarning;
import java.awt.Color;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

public class GetWarningsCommand extends Command {

   public GetWarningsCommand() {
      this.name = "getwarnings";
      this.category = new Category("Moderation");
      this.help = "Returns a list of warnings assigned to a user.";
      this.arguments = "<user>";
      this.aliases = new String[]{"getwarns", "warns"};
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(CommandEvent event) {
      User target = event.getMessage().getMentionedUsers().get(0);
      EntityManager em = new DatabaseConnector().getEntityManager(Table.DATABASE_USER);

      DatabaseUser dbu = em.find(DatabaseUser.class, target.getIdLong());
      List<UserWarning> warnings = new ArrayList<>();

      if (dbu != null) { //Ensure DBU exists.
         List<String> warningIds = dbu.getWarnings();

         if (warningIds != null && !warningIds.isEmpty()) {
            for (String id : warningIds) {
               EntityManager em2 = new DatabaseConnector().getEntityManager(Table.USER_WARNING);
               UserWarning warning = em2.find(UserWarning.class, id);

               if (warning != null) { //Ensure warning is returned.
                  warnings.add(warning);
               }
            }
         }
      }

      EmbedBuilder eb = new EmbedBuilder()
          .setTitle(String.format("**Warnings for %s (%s)**",
              target.getName(),
              target.getId()))
          .setColor(Color.RED)
          .setThumbnail(
              "http://www.foundinaction.com/wp-content/uploads/2018/08/Neon_600x600_Transparent.png")
          .setDescription("Warnings are universal for any user that may have been warned to "
              + "this bot regardless of guild.")
          .addBlankField(true);

      //add warnings as fields
      if (!warnings.isEmpty()) {
         for (UserWarning warning : warnings) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(Date.from((warning.getTimestamp().toInstant()))); //FIXME throwing NPEs
            if (warning.getExpires().after(Timestamp.valueOf(LocalDateTime.now()))) {
               eb.addField(timestamp,
                   String.format("**Warned By: ** %s\n **Expires:** %s\n **Reason:** %s",
                       event.getGuild().getMemberById(warning.getWarnerId()),
                       new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                           .format(Date.from((warning.getExpires().toInstant()))),
                       warning.getReason()), false);
            }
         }
      } else {
         eb.addField("No Warnings", "This user does not have any active warnings.", false);
      }

      eb.setFooter("Provided to you by IdiotBot. The most idiotic of bots.",
          "http://www.foundinaction.com/wp-content/uploads/2018/08/Neon_600x600_Transparent.png");

      event.reply(eb.build());
   }
}
