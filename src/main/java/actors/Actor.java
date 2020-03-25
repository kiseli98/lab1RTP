package actors;

import handlers.Handler;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Actor extends Thread {
    private volatile ConcurrentLinkedQueue<Object> inbox;
    private String actorName;
    private Handler handler;
    private int inboxCapacity;

    private volatile boolean isRunning = true;
    private double birthTime = System.currentTimeMillis();

    String getActorName() {
        return actorName;
    }

    public int getInboxCapacity() {
        return inboxCapacity;
    }

    public double getBirthTime() {
        return birthTime;
    }

    void stopThread() {
        isRunning = false;
    }

    Actor(String name, Handler handler, int inboxCapacity) {
        this.actorName = name;
        this.handler = handler;
        this.inbox = new ConcurrentLinkedQueue<>();
        this.inboxCapacity = inboxCapacity;
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
                handler.system.sendMessage("logger", "Actor <" + this.actorName + "> has crashed \n" + ex.getMessage());
                try {
                    handler.system.resurrect(this);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
