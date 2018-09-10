package ivan.spring.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ivan.akka.Actors.*;
import ivan.akka.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
public class ActorsService {
    @Autowired
    ApplicationContext context;

    private ActorRef chat;

    @PostConstruct
    public void postConstructMethod() {
        ActorSystem system = context.getBean(ActorSystem.class);

        chat = system.actorOf(Props.create(ChatFsm.class));

//        autoBehavior();
    }

    public void autoBehavior(){

        ActorRef Alice = addPerson("Alice");
        sendMessageToChat(Alice, "hi");
        ActorRef Bob = addPerson("Bob");

//        chat.tell(new Logout(), Alice);
//        chat.tell(new Logout(), Bob);
        int a=0;
    }


    public ActorRef addPerson(String peronName){
        ActorSystem system = context.getBean(ActorSystem.class);
        ActorRef person = system.actorOf(PersonFsm.props(), peronName);
        chat.tell(new Login(), person);
        return person;
    }

    public void sendMessageToChat(ActorRef fromPerson, String message){
        chat.tell(new MessageFromPerson(message), fromPerson);
    }

    public void removePerson(ActorRef person){
        chat.tell(new Logout(), person);
    }
}
