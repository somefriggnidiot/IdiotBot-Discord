package com.somefriggnidiot.discord.commands.channels;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.somefriggnidiot.discord.data_access.DatabaseConnector;
import com.somefriggnidiot.discord.data_access.DatabaseConnector.Table;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.events.MessageListener;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.restaction.ChannelAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreatePrivateChannelCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);
   private final EventWaiter waiter;

   public CreatePrivateChannelCommand(EventWaiter waiter) {
      this.waiter = waiter;
      this.name = "createPrivateChannel";
      this.help = "Creates a private voice channel for use by a user and those invited to it.";
      this.arguments = "<owner> <name>";
      this.aliases = new String[] {"cpc", "createprivatechannel"};
      this.ownerCommand = true;
      this.botPermissions = new Permission[]{Permission.MANAGE_SERVER, Permission.MANAGE_CHANNEL};
      this.category = new Category("VIP");
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      User channelOwner = event.getMessage().getMentionedUsers().get(0);

      if (hasChannel(channelOwner)) {
         new ButtonMenu.Builder()
             .setChoices("✅", "❌")
             .setAction(
                reactionEmote -> {
                   if (reactionEmote.getName().equalsIgnoreCase("✅")) {
                      createChannel(event);
                   } else {
                      logger.info(String.format("[%s] Aborting channel creation.", event.getGuild()));
                   }
             })
             .setEventWaiter(waiter)
             .setText(String.format("%s already owns a channel. Reset?", channelOwner.getAsMention()))
             .build().display(event.getTextChannel());
      } else {
         createChannel(event);
      }
   }

   private void createChannel(CommandEvent event) {
      User channelOwner = event.getMessage().getMentionedUsers().get(0);
      String name = event.getMessage().getContentDisplay().split("\\s", 3)[2];

      logger.info(String.format("[%s] Attempting to create voice channel named \"%s\" for %s",
          event.getGuild(),
          name,
          channelOwner.getName()));

      List<Permission> allowed = new ArrayList<>();
      allowed.add(Permission.MANAGE_CHANNEL);
      allowed.add(Permission.VOICE_CONNECT);
      allowed.add(Permission.VOICE_SPEAK);
      allowed.add(Permission.VIEW_CHANNEL);

      List<Permission> denied = new ArrayList<>();

      List<Permission> allDenied = new ArrayList<>();
      allDenied.add(Permission.VOICE_CONNECT);
      allDenied.add(Permission.VOICE_SPEAK);
      allDenied.add(Permission.VIEW_CHANNEL);

      ChannelAction action =
          event.getGuild().getCategoriesByName("\uD83D\uDE0E VIP", true).get(0)
              .createVoiceChannel(name)
              .addPermissionOverride(event.getGuild().getMember(channelOwner), allowed, denied)
              .addPermissionOverride(event.getGuild().getPublicRole(), denied, allDenied)
              .setBitrate(64000);

      action.queue(channel -> {
         logger.info(String.format("[%s] Created voice channel named \"%s\" for %s",
             event.getGuild(),
             name,
             channelOwner.getName()));

         updateDatabase(channelOwner, channel);

         logger.info(String.format("[%s] Added %s as private channel of %s.",
             event.getGuild(),
             channel,
             channelOwner.getName()));
      });
   }

   private boolean hasChannel(User user) {
      DatabaseConnector c = new DatabaseConnector();
      EntityManager em = c.getEntityManager(Table.DATABASE_USER);

      DatabaseUser dbu = em.find(DatabaseUser.class, user.getIdLong());

      return dbu != null && dbu.getPrivateChannel() != null;
   }

   private void updateDatabase(User user, Channel channel) {
      DatabaseConnector c = new DatabaseConnector();
      EntityManager em = c.getEntityManager(Table.DATABASE_USER);

      DatabaseUser dbu = em.find(DatabaseUser.class, user.getIdLong());

      if (dbu != null) {
         em.getTransaction().begin();
         dbu.setPrivateChannel(channel.getIdLong());
         em.getTransaction().commit();
      } else {
         dbu = new DatabaseUser(user.getIdLong()).withPrivateChannel(channel.getIdLong());

         em.getTransaction().begin();
         em.persist(dbu);
         em.getTransaction().commit();
      }
   }
}
