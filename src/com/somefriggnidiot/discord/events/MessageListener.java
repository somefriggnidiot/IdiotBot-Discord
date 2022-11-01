package com.somefriggnidiot.discord.events;

import static com.somefriggnidiot.discord.core.Main.gClient;
import static java.lang.String.format;

import com.somefriggnidiot.discord.data_access.models.BotModeEntry;
import com.somefriggnidiot.discord.data_access.models.GuildInfo;
import com.somefriggnidiot.discord.data_access.util.GuildInfoUtil;
import com.somefriggnidiot.discord.util.BotModeUtil;
import com.somefriggnidiot.discord.util.MessageListenerUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vip.floatationdevice.guilded4j.object.Embed;
import vip.floatationdevice.guilded4j.object.misc.EmbedField;

public class MessageListener extends ListenerAdapter {

   private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

   @Override
   public void onMessageReceived(final MessageReceivedEvent event) {
      User author = event.getAuthor();
      MessageChannel channel = event.getChannel();
      String msg = event.getMessage().getContentDisplay();

      gdBridgeTest(event);
      logEvent(event, author, channel, msg);
      handleXp(event, msg);
      handleBotChannel(event);
   }

   /**
    * Routes messages to the {@link MessageListenerUtil} for XP Gains when applicable.
    *
    * @param event a {@link MessageReceivedEvent} to be checked
    * @param msg the message contents as a {@link String}
    */
   private void handleXp(final MessageReceivedEvent event, final String msg) {
      GuildInfo gi = event.getGuild().isAvailable()? GuildInfoUtil.getGuildInfo(event.getGuild()
          .getIdLong()) : null;

      Boolean isGrantingXp;

      if (gi == null || gi.isGrantingMessageXp() == null) {
         isGrantingXp = false;
      } else {
         isGrantingXp = gi.isGrantingMessageXp();
      }

      if (isGrantingXp) {
         if (!msg.startsWith("!") && !msg.startsWith("$") && !msg.startsWith("~")) {
            MessageListenerUtil.handleXpGain(event);
         }
      }
   }

   /**
    * Logs the {@link MessageReceivedEvent} contents based on the current logging
    * configuration settings.
    *
    * @param event the {@link MessageReceivedEvent} being logged.
    * @param author the {@link User} that authored the message which created the
    * event.
    * @param channel the {@link MessageChannel} where the logged message was received.
    * @param msg the message contents as a {@link String}
    */
   private void logEvent(final MessageReceivedEvent event, final User author,
       final MessageChannel channel, final String msg) {
      Guild guild = event.getGuild();
      if (event.isFromType(ChannelType.TEXT) &&
          !event.getChannel().getName().equalsIgnoreCase("log")) {

         String userName;
         if (guild.getMember(author) == null) {
            userName = author.getName();
         } else {
            userName = guild.getMember(author).getEffectiveName();
         }

         if (event.getMessage().getEmbeds().size() > 0) {
            String embedDisplay = "[EMBEDS] ";
            List<MessageEmbed> embeds = event.getMessage().getEmbeds();

            for (MessageEmbed embed : embeds) {
               embedDisplay = embedDisplay.concat(
                   format("TITLE: %s \n CONTENT: %s",
                       embed.getTitle(),
                       embed.getDescription()));
            }

            logger.info(format("[%s] [#%s] %s: %s",
                guild == null ? "DIRECT MESSAGE" : event.getGuild(),
                channel.getName(),
                userName,
                embedDisplay));
         } else {
            logger.info(format("[%s] [#%s] %s: %s",
                guild == null ? "DIRECT MESSAGE" : event.getGuild(),
                channel.getName(),
                userName,
                msg));
         }
      }
   }

   private void handleBotChannel(final MessageReceivedEvent event) {
      GuildInfo gi = GuildInfoUtil.getGuildInfo(event.getGuild());

      //Exit if no BotMode entries.
      if (gi.getBotModeEntryIds().size() > 0) {
         //Retrieve list of entries for current channel.
         List<BotModeEntry> bmes = gi.getBotModeEntryIds().stream()
             .map(BotModeUtil::getBotModeEntry)
             .filter(bme -> bme.getChannelId().equals(event.getChannel().getIdLong()))
             .collect(Collectors.toList());

         logger.info(bmes.toString());

         List<String> allowedPrefixes = new ArrayList<>();
         bmes.forEach(bme -> allowedPrefixes.add(bme.getCommandPrefix()));

         Boolean allowed = false;

         for (String prefix : allowedPrefixes) {
            if (event.getMessage().getContentRaw().startsWith(prefix)) {
               allowed = true;
            }
         }

         if (!allowed) {
            event.getMessage().delete().queue();
         }
      }
   }

   //D->G
   private void gdBridgeTest(final MessageReceivedEvent event) {
      if (event.getChannel().getIdLong() != 742782354933940265L &&
         event.getChannel().getIdLong() != 976593263119204372L) return;
//      if (null != event.getMember() && event.getMember().getIdLong() == 486265419762630656L) return;

      if (event.isWebhookMessage()) {
         String content = event.getMessage().getContentRaw();
         String channelName = event.getChannel().getName();
         String format = "**[#%s]** %s: %s";
         List<Embed> embeds = new ArrayList<>();

         for (MessageEmbed embed : event.getMessage().getEmbeds()) {
            embeds.add(castDiscordEmbedToGuilded(embed));
         }

         gClient.getChatMessageManager().createChannelMessage(
             "da353090-ce72-4284-bb07-1eb5567491e3",
             format(format, channelName, event.getAuthor(), content),
             embeds.size() > 0 ? embeds.toArray(new Embed[0]) : null,
             null,
             null,
             null);
      } else {
         String content = event.getMessage().getContentRaw();
         String authorName = event.getAuthor().getName();
         String channelName = event.getChannel().getName();
         String format = "**[#%s]** %s: %s";
         List<Embed> embeds = new ArrayList<>();

         for (MessageEmbed embed : event.getMessage().getEmbeds()) {
            embeds.add(castDiscordEmbedToGuilded(embed));
         }

         gClient.getChatMessageManager().createChannelMessage(
             "da353090-ce72-4284-bb07-1eb5567491e3",
             format(format, channelName, authorName, content),
             embeds.size() > 0 ? embeds.toArray(new Embed[0]) : null,
             null,
             null,
             null);
      }
   }

   private Embed castDiscordEmbedToGuilded(MessageEmbed discEmbed) {
      Embed embed = new Embed();
      if (null != discEmbed.getAuthor()) {
         embed.setAuthorIconUrl(discEmbed.getAuthor().getIconUrl());
         embed.setAuthorName(discEmbed.getAuthor().getName());
         embed.setAuthorUrl(discEmbed.getAuthor().getUrl());
      }
      if (discEmbed.getColorRaw() <= 16777215 && discEmbed.getColorRaw() > 0) {
         embed.setColor(discEmbed.getColorRaw());
      }

      if (null != discEmbed.getDescription() && !discEmbed.getDescription().isEmpty()) {
         embed.setDescription(discEmbed.getDescription());
      }

      if (null != discEmbed.getFooter()) {
         embed.setFooterIconUrl(discEmbed.getFooter().getIconUrl());
         embed.setFooterText(discEmbed.getFooter().getText());
      }

      if (null != discEmbed.getImage()) {
         embed.setImageUrl(discEmbed.getImage().getUrl());
      }

      if (null != discEmbed.getThumbnail()) {
         embed.setThumbnailUrl(discEmbed.getThumbnail().getUrl());
      }

      if (null != discEmbed.getTitle()) {
         embed.setTitle(discEmbed.getTitle());
      }

      if (null != discEmbed.getUrl()) {
         embed.setUrl(discEmbed.getUrl());
      }

      if (discEmbed.getFields().size() > 0) {
         List<EmbedField> fields = new ArrayList<>();
         for (Field field : discEmbed.getFields()) {
            fields.add(new EmbedField()
                .setInline(field.isInline())
                .setName(field.getName())
                .setValue(field.getValue()));
         }
         embed.setFields(fields.toArray(new EmbedField[0]));
      }

      return embed;
   }

   /**
    * Deprecated. This shouldn't even exist in the first place.
    *
    * Method specific to Celestial Brothers guild, deletes any messages in the bot channel not
    * preceeded with specified known bot prefixes.
    *
    * @param guildId the ID of the guild within which a message was sent.
    * @param author the User object of the sender of the message.
    * @param message the Message which was sent.
    */
   @Deprecated
   private void handleBotChannel(final Long guildId, final User author, final Message message) {
      List<String> allowedPrefixes = new ArrayList<>();
      List<Long> moderatedChannelIds = new ArrayList<>();

      //TODO Finish BotModeEntry model / BotModeUtil / BotModeCommand and make this flexible.
      moderatedChannelIds.add(728748254275305482L);

      if (guildId == 217175952701128714L && !author.isBot()) {
         if (moderatedChannelIds.contains(message.getChannel().getIdLong())) {
            allowedPrefixes.add(".rs ");
            allowedPrefixes.add(".pc ");

            Boolean allowed = false;

            for(String prefix : allowedPrefixes) {
               if (message.getContentRaw().startsWith(prefix)) {
                  allowed = true;
               }
            }

            if (!allowed) {
               message.delete().queue();
            }
         }
      }
   }

}
