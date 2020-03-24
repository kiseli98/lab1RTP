package actors;

import handlers.Handler;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Actor extends Thread {
    private volatile ConcurrentLinkedQueue<Object> inbox;
    private String actorName;
    private Handler handler;
    private volatile boolean isRunning = true;

    String getActorName() {
        return actorName;
    }

    void stopThread() {
        isRunning = false;
    }

    Actor(String name, Handler handler) {
        this.actorName = name;
        this.handler = handler;
        this.inbox = new ConcurrentLinkedQueue<>();
    }

    Actor(Actor actor) {
        this.inbox = actor.inbox;
        this.handler = actor.handler;
        this.actorName = actor.actorName;
    }


    public Queue<Object> getInbox() {
        return inbox;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                handler.receive(!inbox.isEmpty() ? inbox.remove() : null);
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    handler.system.resurrect(this);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
