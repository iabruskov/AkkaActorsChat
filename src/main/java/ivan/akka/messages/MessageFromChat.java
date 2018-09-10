package ivan.akka.messages;


public class MessageFromChat {
    private final String message;

    public MessageFromChat(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }


}
