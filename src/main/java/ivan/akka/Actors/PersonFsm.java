package ivan.akka.Actors;

import akka.actor.AbstractFSM;
import akka.actor.Props;
import ivan.akka.Model.*;
import ivan.akka.condition.*;
import ivan.akka.messages.*;
import ivan.spring.service.ActorsConditions2;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PersonFsm extends AbstractFSM<PersonCondition, FsmData> {

    static public Props props() {
        return Props.create(PersonFsm.class, PersonFsm::new);
    }

    private boolean read = false;
    private boolean written =  false;

    {
        startWith(PersonCondition.NeedChat, Uninitialized.Uninitialized);

        when(PersonCondition.NeedChat,
                matchEvent(LoggedIn.class, FsmData.class,
                        (loginObj, data) -> goTo(PersonCondition.Connected)
                )

        );
        when(PersonCondition.Connected,
                matchEvent(MessageFromChat.class, FsmData.class,
                        (messageFromChatObj, data) -> stay()
                ).
                event(SomeOneLoggedIn.class, FsmData.class,
                        (someOneLoggedIn, data) -> {
                            someOneLoggedInAutoBehavior(someOneLoggedIn.getPersonName());
                            return stay();
                        }
                ).
                event(MessageFromPerson.class, FsmData.class,
                        (messageFromChatObj, data) -> {
                            messageFromPersonAutoBehavior(messageFromChatObj.getSenderName());
                            read = true;
                            return goTo(PersonCondition.Communicating);
                        }
                )
                .event(MessageDelivered.class, FsmData.class,
                        (messageDeliveredObj, data) -> {
                            written = true;
                            return goTo(PersonCondition.Communicating);
                        }
                 )

        );
        when(PersonCondition.Communicating,
                matchEvent(MessageFromPerson.class, FsmData.class,
                        (MessageFromPersonObj, data) -> {
                            read = true;
                            return getStateFromCommunicated();
                        }
                )
                .event(MessageDelivered.class, FsmData.class,
                        (messageDeliveredObj, data) -> {
                            written = true;
                            return getStateFromCommunicated();
                        }
                )

        );
        when(PersonCondition.Talked,
                matchEvent(SomeOneLoggedOut.class, FsmData.class,
                        (someOneLoggedOut, data) -> {
                            someOneLoggedOutAutoBehavior();
                            return stay();
                        })
        );


        onTransition(
                matchState(null, null, (from, to) -> {
                    ActorsConditions2.status.put(getSelf().path().name(), to.name());
                } )
        );

        initialize();
    }

    private State<PersonCondition, FsmData> getStateFromCommunicated(){
        if(read && written) {
            return goTo(PersonCondition.Talked);
        } else {
            return stay();
        }
    }

    @Override
    public void postStop() {
        ActorsConditions2.status.remove(getSelf().path().name());
    }

    protected void someOneLoggedInAutoBehavior(String personName){

    }

    protected void messageFromPersonAutoBehavior(String senderName){

    }

    protected void someOneLoggedOutAutoBehavior(){

    }
}
