package ivan.akka.Actors;

import akka.actor.Props;
import ivan.akka.messages.Logout;
import ivan.akka.messages.MessageFromPerson;


public class PersonFsmAutoBehavior extends PersonFsm {

    static public Props props() {
        return Props.create(PersonFsmAutoBehavior.class, PersonFsmAutoBehavior::new);
    }

    @Override
    protected void someOneLoggedInAutoBehavior(String personName){
        getSender().tell(new MessageFromPerson("hi, " + personName), getSelf());
    }

    @Override
    protected void messageFromPersonAutoBehavior(String senderNamej) {
        getSender().tell(new MessageFromPerson("hi, " + senderNamej), getSelf());
    }

    @Override
    protected void someOneLoggedOutAutoBehavior() {
        getSender().tell(new Logout(), getSelf());
    }
}
