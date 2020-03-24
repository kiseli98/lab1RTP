package handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.ResponseSensor1;
import model.ResponseSensor2;

public class Handlers {
    static ObjectMapper mapper = new ObjectMapper();


    public static Handler dataReceiver = msg -> {
        if (msg == null) {
            return;
        }
        String data = msg.toString();
        Handler.system.sendMessage("processor", data);
    };

    public static Handler processor = msg -> {
        if (msg == null) {
            return;
        }
        if (msg.toString().contains("panic")) {
            throw new Exception("Panic exception");
        }
        try {
            Handler.system.sendMessage("forecaster_1",
                    mapper.readValue(msg.toString(), ResponseSensor1.class).getMessage()
            );

            Handler.system.sendMessage("forecaster_2",
                    mapper.readValue(msg.toString(), ResponseSensor2.class).getMessage()
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    };


    public static Handler printer = msg -> {
        if (msg == null) return;
//            system.getLoad();
        System.out.println("---------------------------------------");
        System.out.println(msg);
        System.out.println("---------------------------------------");
    };


}
