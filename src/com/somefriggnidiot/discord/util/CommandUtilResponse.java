package com.somefriggnidiot.discord.util;

public class CommandUtilResponse {
      private Boolean succeeded;
      private CommandResponseMessage response;

      public CommandUtilResponse(Boolean succeeded, CommandResponseMessage response) {
         this.succeeded = succeeded;
         this.response = response;
      }

      public Boolean getSucceeded() {
         return succeeded;
      }

      public CommandResponseMessage getCommandResponseMessage() {
         return response;
      }

      public enum CommandResponseMessage {
         MISSING_ARGS("One or more arguments are missing."),
         INVALID_ARG("One or more arguments provided are invalid."),
         TOO_MANY_ROLE_RESULTS("Too many results were found when looking up role."),
         NO_ROLE_RESULTS_FOUND("No results were found when looking up role."),
         PERMISSION_DENIED("You do not have permission to do that."),
         NOT_IMPLEMENTED("That functionality has not yet been implemented."),
         UNKNOWN_ERROR("An unknown error as occurred."),
         SUCCESS("Command successfully executed!");

         public final String info;

         CommandResponseMessage(String info) {
            this.info = info;
         }
      }
}
