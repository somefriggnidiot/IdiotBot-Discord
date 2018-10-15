package com.somefriggnidiot.discord.commands.channels;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.somefriggnidiot.discord.data_access.DatabaseConnector;
import com.somefriggnidiot.discord.data_access.DatabaseConnector.Table;
import com.somefriggnidiot.discord.data_access.models.DatabaseUser;
import com.somefriggnidiot.discord.events.MessageListener;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InviteToPrivateChannelCommand extends Command {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   public InviteToPrivateChannelCommand() {
      this.name = "inviteToPrivateChannel";
      this.help = "Grants a user access to your private voice channel.";
      this.arguments = "<user>";
      this.aliases = new String[] {"i2pc", "ipc", "inv", "invite"};
      this.botPermissions = new Permission[]{Permission.MANAGE_SERVER, Permission.MANAGE_CHANNEL};
      this.category = new Category("VIP");
      this.cooldown = 1;
      this.cooldownScope = CooldownScope.USER;
   }

   @Override
   protected void execute(final CommandEvent event) {
      User invoker = event.getMessage().getAuthor();
      User invitee = event.getMessage().getMentionedUsers().get(0);
      List<Role> roles = event.getGuild().getMember(invoker).getRoles();
      Boolean isVip = false;

      for (Role role : roles) {
         if (role.getName().contains("VIP")) {
            isVip = true;
            break;
         }
      }

      if (!isVip) {
         EmbedBuilder eb = new EmbedBuilder()
             .setColor(Color.RED)
             .setThumbnail("http://www.foundinaction.com/wp-content/uploads/2018/08/Neon_600x600_Transparent.png")
             .setTitle("Become a VIP today!", "https://www.patreon.com/foundinaction")
             .setDescription("Private voice channels are a benefit of being a financial supporter"
                 + " of this community.");

         event.reply(eb.build());
      } else if (!hasChannel(invoker)) {
         event.reply("You don't appear to own a channel. If you believe this to be in error, "
             + "please contact " + event.getGuild().getOwner().getAsMention());
      } else {
         //Get invoker's channel.
         Channel userChannel = event.getGuild().getVoiceChannelById(getChannelId(invoker));

         //Set perm overrides.
         List<Permission> denied = new ArrayList<>();
         List<Permission> allowed = new ArrayList<>();
         allowed.add(Permission.VOICE_CONNECT);
         allowed.add(Permission.VOICE_SPEAK);
         allowed.add(Permission.VIEW_CHANNEL);

         PermissionOverrideAction permissionOverrideAction = userChannel
             .putPermissionOverride(event.getGuild().getMember(invitee))
             .setPermissions(allowed, denied);

         permissionOverrideAction.queue(success -> logger.info(String.format("[%s] Gave %s access to %s.",
             event.getGuild(),
             invitee.getName(),
             userChannel.getName())));
      }
   }

   private boolean hasChannel(User user) {
      DatabaseConnector c = new DatabaseConnector();
      EntityManager em = c.getEntityManager(Table.DATABASE_USER);

      DatabaseUser dbu = em.find(DatabaseUser.class, user.getIdLong());

      return dbu != null && dbu.getPrivateChannel() != null;
   }

   private Long getChannelId(User user) {
      DatabaseConnector c = new DatabaseConnector();
      EntityManager em = c.getEntityManager(Table.DATABASE_USER);

      DatabaseUser dbu = em.find(DatabaseUser.class, user.getIdLong());

      if (dbu != null) {
         return dbu.getPrivateChannel();
      } else {
         dbu = new DatabaseUser(user.getIdLong());

         em.getTransaction().begin();
         em.persist(dbu);
         em.getTransaction().commit();

         return -1L;
      }
   }
}
