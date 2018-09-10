import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import ivan.akka.Actors.ChatFsm;
import ivan.akka.Actors.PersonFsmAutoBehavior;
import ivan.akka.messages.Login;
import ivan.akka.messages.Logout;
import ivan.akka.messages.MessageFromPerson;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class ChatTest  {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("ChatTest");
    }

    @AfterClass
    public static void tearDown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }


    @Test
    public void testAutoBehavior() throws InterruptedException {
        new TestKit(system) {{
            final ActorRef chat =  system.actorOf(Props.create(ChatFsm.class));


            ActorRef Alice = system.actorOf(PersonFsmAutoBehavior.props(), "Alice");
            chat.tell(new Login(), Alice);
            chat.tell(new MessageFromPerson("Hi"), Alice);
            ActorRef Bob = system.actorOf(PersonFsmAutoBehavior.props(), "Bob");
            chat.tell(new Login(), Bob);

            Thread.sleep(3000);
            chat.tell(new Logout(), Alice);
            Thread.sleep(1000);

            system.stop(chat);
        }};
    }


}
