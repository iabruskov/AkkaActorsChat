package ivan.akka.Actors;

import akka.actor.*;
import akka.event.*;
import com.typesafe.config.ConfigValue;
import ivan.akka.Model.*;
import ivan.akka.condition.*;
import ivan.akka.messages.*;
import ivan.spring.service.ActorsConditions2;
import ivan.util.MyUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;


@Component
@org.springframework.context.annotation.Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ChatFsm extends AbstractFSM<ChatCondition, FsmData> {

//    static public Props props() {
//        return Props.create(ChatFsm.class, ChatFsm::new);
//    }

    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    {
        startWith(ChatCondition.Available, Uninitialized.Uninitialized);

        when(ChatCondition.Available,
                matchEvent(Login.class, FsmData.class,
                        (loginObj, data) -> {
                            ChatData chatData = new ChatData(getSender());
                            loginPerson(getSender(), chatData);
                            return goTo(ChatCondition.AwaitingRecepient).using(chatData);
                        }
                )
        );
        when(ChatCondition.AwaitingRecepient,
                matchEvent(Login.class, ChatData.class,
                        (loginObj, data) -> {
                            loginPerson(getSender(), data);
                            return goTo(ChatCondition.Online).using(data);
                        }
                ).
                event(MessageFromPerson.class, FsmData.class,
                        (messageFromPersonObj, data) -> {
                            matchMessageFromPersonNoRecipient(getSender(), messageFromPersonObj.getMessage());
                            return stay();
                        }
                ).
                event(Logout.class, ChatData.class,
                        (logoutObj, dataObj) -> {
                            logoutPerson(getSender(), getContext(), dataObj);
                            return goTo(ChatCondition.Available).using(dataObj);
                        }
                )
        );

        when(ChatCondition.Online,
                matchEvent(MessageFromPerson.class, ChatData.class,
                        (messageFromPersonObj, data) -> {
                            matchMessageFromPerson(getSender(), messageFromPersonObj.getMessage(), data);
                            return stay();
                        }
                ).
                event(Login.class, ChatData.class,
                        (loginObj, data) -> {
                            loginPerson(getSender(), data);
                            return stay().using(data);
                        }
                ).
                event(MessageFromPerson.class, ChatData.class,
                        (messageFromPersonObj, data) -> {
                            matchMessageFromPerson(getSender(), messageFromPersonObj.getMessage(), data);
                            return stay();
                        }
                ).
                event(Logout.class, ChatData.class,
                        (logoutObj, dataObj) -> {
                            logoutPerson(getSender(), getContext(), dataObj);
                            State newState = null;
                            if(dataObj.getPersons().size()>1){
                                newState = stay();
                            } else {
                                newState = goTo(ChatCondition.AwaitingRecepient);
                            }
                            return newState.using(dataObj);
                        }
                )
        );

        onTransition(
                matchState(null, null, (from, to) -> {
                    ActorsConditions2.chatStatus= to.name();
                } )
        );

        initialize();
    }

    @Override
    public void preStart(){
        logMessage("-Start Chat-");
    }

    @Override
    public void postStop(){
        logMessage("-Finish Chat-");
    }

    private void loginPerson(ActorRef loggedInPerson, ChatData chatData){
        String personName = loggedInPerson.path().name();
        chatData.addPerson(loggedInPerson);
        loggedInPerson.tell(new LoggedIn(), getSelf());

        chatData.getPersons().stream().filter(person -> !person.equals(loggedInPerson)).forEach(person -> person.tell(new SomeOneLoggedIn(personName), getSelf()));
        memAndLogMessage(personName + " connected to chat");
    }

    private void logoutPerson(ActorRef loggedOutPerson, AbstractActor.ActorContext context, ChatData chatData){
        String personName = loggedOutPerson.path().name();
        chatData.removePerson(loggedOutPerson);
        context.stop(loggedOutPerson);

        chatData.getPersons().forEach(person -> person.tell(new SomeOneLoggedOut(personName), getSelf()));
        memAndLogMessage(personName + " disconnected from chat");
    }

    private void matchMessageFromPersonNoRecipient(ActorRef messagePerson, String message){
        memAndLogMessage(messagePerson.path().name() + ": " + message);
        messagePerson.tell(new MessageFromChat("no recipient"), getSelf());
        memAndLogMessage("no recipient");
    }

    private void matchMessageFromPerson(ActorRef messagePerson, String Message, ChatData chatData){
        String senderName = messagePerson.path().name();
        chatData.getPersons().stream().filter(person -> !person.equals(messagePerson)).forEach(person -> person.tell(new MessageFromPerson(Message, senderName), getSelf()));
        messagePerson.tell(new MessageDelivered(), getSelf());
        memAndLogMessage(senderName + ": " + Message);
    }

    //------------------------
    private void memAndLogMessage(String message){
        logMessage(message);
        ActorsConditions2.chatBody.add(message);
    }

    private void logMessage(String message){
        ConfigValue fileNameObj = getContext().getSystem().settings().config().getObject("akka").get("chat_history_file");
        String fileName = (String) fileNameObj.unwrapped();
        MyUtils.memAndLogChatMessage(message, fileName, log);
    }
}
