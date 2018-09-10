package ivan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;


@SpringBootApplication
public class ChatApplication {
    public static void main(String[] args) throws IOException {
        SpringApplication.run(ChatApplication.class, args);

        System.out.println(">>> Press ENTER to exit <<<");
        System.in.read();
        System.exit(0);
    }
}
