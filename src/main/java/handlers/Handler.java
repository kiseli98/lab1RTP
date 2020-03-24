package handlers;

import actors.ActorSystem;

public interface Handler {
    ActorSystem system = new ActorSystem();

    void receive(Object msg) throws Exception;
}
