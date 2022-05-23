package com.somefriggnidiot.discord.gdbridge;

import static com.somefriggnidiot.discord.core.Main.gClient;
import static java.lang.String.format;

import com.google.common.eventbus.Subscribe;
import com.somefriggnidiot.discord.core.Main;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vip.floatationdevice.guilded4j.event.ChatMessageCreatedEvent;
import vip.floatationdevice.guilded4j.rest.MemberManager;
import vip.floatationdevice.guilded4j.rest.ServerChannelManager;

public class GDBridgeListener {

   Logger logger = LoggerFactory.getLogger(this.getClass().getName());

   @Subscribe
   public void onMessage(ChatMessageCreatedEvent event) {
      if (event.getChatMessageObject().getCreatorId().equals("myxa9b0d")) return;
      if (event.getChatMessageObject()
          .getChannelId().equals("da353090-ce72-4284-bb07-1eb5567491e3")) {
         TextChannel destination = Main.jda.getGuildById("742782301532061758")
             .getTextChannelById(976593263119204372L);
         MessageBuilder messageBuilder = new MessageBuilder();
         String creatorId = event.getChatMessageObject().getCreatorId();
         String serverId = event.getServerID();
         MemberManager memberManager = gClient.getMemberManager();
         ServerChannelManager channelManager = gClient.getServerChannelManager();

         String content = event.getChatMessageObject().getContent();
         String authorName = memberManager.getServerMember(serverId, creatorId).getNickname();
         String channelName = channelManager
             .getServerChannel(event.getChatMessageObject().getChannelId()).getName();
         String format = "**[#%s]** %s: %s";

         messageBuilder.setContent(format(format, channelName, authorName, content));

         destination.sendMessage(messageBuilder.build()).queue();

      }
//      cmm.createChannelMessage("da353090-ce72-4284-bb07-1eb5567491e3",
//          event.getChatMessageObject().getContent(), null, null, null, null);
   }

}
