package actors;

import handlers.Handler;

import java.util.concurrent.CopyOnWriteArrayList;

public class ActorGroup {
    Handler handler;
    CopyOnWriteArrayList<Actor> actors = new CopyOnWriteArrayList<>();
    String groupName;

    public ActorGroup(String groupName, Handler handler) {
        this.handler = handler;
        this.groupName = groupName;
    }

    public void addActor() {
        Actor createdActor = new Actor(groupName, handler);
        createdActor.start();
        actors.add(createdActor);
    }

    public void removeActor(Actor removingActor) {
        actors.remove(removingActor);
        removingActor.stopThread();
    }

    public CopyOnWriteArrayList<Actor> getActors() {
        return actors;
    }
}
