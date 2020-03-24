import handlers.Forecaster;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.SseEventSource;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static handlers.Handler.system;
import static handlers.Handlers.*;

public class Application {
    private static String ip;
//    private static String ip = "192.168.1.9";

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Enter ip address of your machine where /iot is running: \n");
        Scanner scanner = new Scanner(System.in);
        ip = scanner.nextLine();

        System.out.println("Enter output interval for sensors 1 and 2 : \n");
        System.out.print("Sensor 1 interval in seconds - ");
        system.createActorGroup("forecaster_1",
                new Forecaster(scanner.nextFloat())
        );
        System.out.print("Sensor 2 interval in seconds - ");
        system.createActorGroup("forecaster_2",
                new Forecaster(scanner.nextFloat())
        );

        system.createActorGroup("dataReceiver", dataReceiver);
        system.createActorGroup("processor", processor);
        system.createActorGroup("printer", printer);
        system.start();

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://" + ip + ":4000/iot");
        try (SseEventSource source = SseEventSource
                .target(target)
                .reconnectingEvery(5, TimeUnit.SECONDS)
                .build()) {
            source.register(inboundSseEvent -> {
                        String data = inboundSseEvent.readData();
                        system.sendMessage("dataReceiver", data);
                    }
            );
            source.open();
            Thread.sleep(10000);
        }

        system.shutDown();
    }
}
