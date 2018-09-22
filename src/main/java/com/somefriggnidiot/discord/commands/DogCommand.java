package com.somefriggnidiot.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.awt.Color;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;

public class DogCommand extends Command {
   public DogCommand() {
      this.name = "dog";
      this.help = "Returns dogs.";
      this.aliases = new String[]{"dogs", "bork", "woof"};
      this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
      this.guildOnly = false;
   }

   @Override
   protected void execute(final CommandEvent event) {
      Unirest.get("https://dog.ceo/api/breeds/image/random").asJsonAsync(new Callback<JsonNode>(){

         // The API call was successful
         public void completed(HttpResponse<JsonNode> hr)
         {
            event.reply(new EmbedBuilder()
                .setColor(Color.BLACK)
                .setImage(hr.getBody().getObject().getString("message"))
                .build());
         }

         // The API call failed
         public void failed(UnirestException ue)
         {
            event.reactError();
         }

         // The API call was cancelled (this should never happen)
         public void cancelled()
         {
            event.reactError();
         }
      });
   }
}
