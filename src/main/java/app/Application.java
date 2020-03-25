package app;

import handlers.Forecaster;
import org.apache.log4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.SseEventSource;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static handlers.Handler.system;
import static handlers.Handlers.*;

public class Application {
    public static Logger logger = Logger.getLogger(Application.class);


    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter ip address of your machine where /iot is running " +
                "(or press Enter if localhost): \n");

        String ip = scanner.nextLine();
        ip = "".equals(ip.trim()) ? "localhost" : ip;

        System.out.println("Enter output interval for sensors 1 and 2 : \n");

        System.out.print("Sensor 1 interval in seconds - ");
        float interval1 = scanner.nextFloat();
        system.createActorGroup("forecaster_1",
                new Forecaster(interval1),
                20
        );

        System.out.print("Sensor 2 interval in seconds - ");
        float interval2 = scanner.nextFloat();
        system.createActorGroup("forecaster_2",
                new Forecaster(interval2),
                20
        );

        system.createActorGroup("dataReceiver", dataReceiver, 20);
        system.createActorGroup("processor", processor, 20);
        system.createActorGroup("printer", printer, 20);
        system.createActorGroup("logger", loggerHandler, 20);
        system.start();

        system.sendMessage("logger", "SSE is running on " + ip);
        system.sendMessage("logger", "Sensor 1 time interval is set to " + interval1 + " seconds");
        system.sendMessage("logger", "Sensor 2 time interval is set to " + interval2 + " seconds");


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
