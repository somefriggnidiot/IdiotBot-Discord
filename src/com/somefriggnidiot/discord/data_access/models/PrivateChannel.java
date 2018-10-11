package com.somefriggnidiot.discord.data_access.models;

import javax.persistence.Id;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.User;

public class PrivateChannel {

   @Id
   private final Long channelId;
   private Channel channel;
   private User channelOwner;

   public PrivateChannel(Channel channel, User owner) {
      channelId = channel.getIdLong();
      this.channel = channel;
      this.channelOwner = owner;
   }


}
