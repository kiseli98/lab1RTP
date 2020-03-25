package actors;


import app.Application;
import handlers.Handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ActorSystem {
    Map<String, ActorGroup> actorGroups = new HashMap<>();


    public void createActorGroup(String name, Handler handler, int inboxCapacity) {
        actorGroups.put(name, new ActorGroup(name, handler, inboxCapacity));
    }


    void resurrect(Actor deadActor) throws InterruptedException {
        // concurrent array list
        ActorGroup actorGroup = actorGroups.get(deadActor.getActorName());
        CopyOnWriteArrayList<Actor> actors = actorGroup.getActors();
        int indexOfDeadActor = actors.indexOf(deadActor);
        actorGroup.removeActor(deadActor);

        if (indexOfDeadActor >= actors.size() || indexOfDeadActor < 0) {
            actors.add(new Actor(deadActor));
            actors.get(actors.size() - 1).start();
        } else {
            actors.add(indexOfDeadActor, new Actor(deadActor));
            actors.get(indexOfDeadActor).start();
        }

        sendMessage("logger", "Actor " + deadActor.getActorName() + indexOfDeadActor + " was resurrected after failure");
    }


    public void sendMessage(String toActorGroup, Object msg) {
        if (msg == null) return;

        ActorGroup actorGroup = actorGroups.get(toActorGroup);
        CopyOnWriteArrayList<Actor> actors = actorGroup.getActors();

        if (actors.isEmpty()) {
            actorGroup.addActor();
        }

        boolean messageIsSend = false;

        for (Actor actor : actors) {
            // send if inbox is not full
            if (actor.getInbox().size() < actor.getInboxCapacity() && !messageIsSend) {
                actor.getInbox().add(msg);
                messageIsSend = true;
                continue;
            }
            // removing actors on low load, kill after one min
            if (messageIsSend && actor.getInbox().isEmpty() ) {
//                &&
//                (Math.abs(actor.getBirthTime() - System.currentTimeMillis()) / 1000) > 60
                actorGroup.removeActor(actor);
            }
        }

        // creating new actor on high load
        if (!messageIsSend) {
            actorGroup.addActor();

            // sending message to last created actor
            actors.get(actors.size() - 1)
                    .getInbox()
                    .add(msg);
        }

    }

    public void getLoad() {
        System.out.println("==========================");
        sendMessage("logger", "Load for each actor : ");
        actorGroups.values().forEach(actorGroup ->
                {
                    for (int i = 0; i < actorGroup.getActors().size(); i++) {
                        Actor actor = actorGroup.getActors().get(i);
                        sendMessage("logger", actor.getActorName() + "_" + i + " - load is - " + actor.getInbox().size());
                    }
                }
        );
        System.out.println("==========================");

    }

    public void start() {
        actorGroups.values()
                .forEach(actorGroup ->
                        actorGroup.getActors()
                                .forEach(Actor::start)
                );
    }

    public void shutDown() throws InterruptedException {
        for (ActorGroup actorGroup : actorGroups.values()) {
            for (Actor actor : actorGroup.getActors()) {
                actor.stopThread();
                actor.join();
            }
        }
    }
}
