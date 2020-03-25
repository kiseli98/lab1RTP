package actors;

import handlers.Handler;

import java.util.concurrent.CopyOnWriteArrayList;

public class ActorGroup {
    Handler handler;
    CopyOnWriteArrayList<Actor> actors = new CopyOnWriteArrayList<>();
    String groupName;
    int inboxCapacity;

    public ActorGroup(String groupName, Handler handler, int inboxCapacity) {
        this.handler = handler;
        this.groupName = groupName;
        this.inboxCapacity = inboxCapacity;
    }

    public void addActor() {
        Actor createdActor = new Actor(groupName, handler, inboxCapacity);
        createdActor.start();
        actors.add(createdActor);
    }

    public synchronized void removeActor(Actor removingActor) {
        actors.remove(removingActor);
        removingActor.stopThread();
    }

    public CopyOnWriteArrayList<Actor> getActors() {
        return actors;
    }
}
