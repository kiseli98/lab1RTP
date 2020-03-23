import actors.ActorSystem;
import handlers.Forecaster;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.SseEventSource;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import static handlers.Handlers.*;

public class Application {
    private static ActorSystem actorSystem = new ActorSystem();
    private static String url = "http://localhost:4000/iot";

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Enter output interval for sensors 1 and 2 : \n");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Sensor 1 interval in seconds - ");
        actorSystem.createActorGroup("forecaster_1",
                new Forecaster(scanner.nextFloat())
        );
        System.out.print("Sensor 2 interval in seconds - ");
        actorSystem.createActorGroup("forecaster_2",
                new Forecaster(scanner.nextFloat())
        );
        actorSystem.createActorGroup("dataReceiver", dataReceiver);
        actorSystem.createActorGroup("processor", processor);
        actorSystem.createActorGroup("printer",printer);
        actorSystem.start();

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(url);
        try (SseEventSource source = SseEventSource
                .target(target)
                .reconnectingEvery(5, TimeUnit.SECONDS)
                .build()) {
            source.register(inboundSseEvent -> {
                        String data = inboundSseEvent.readData();
                        actorSystem.sendMessage("dataReceiver", data);
                    }
            );
            source.open();
            Thread.sleep(10000);
        }

        actorSystem.shutDown();
    }
}
