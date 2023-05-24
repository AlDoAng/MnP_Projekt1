package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import java.util.concurrent.ThreadLocalRandom;

import java.time.Duration;


public class Spawner extends AbstractBehavior<Spawner.Message> {

    public interface Message {}


    public record GenNewSinger(int time) implements Message {  }

    public static Behavior<Message> create(ActorRef<Library.Message> library, ActorRef<QueueManager.Message> queueManager) {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new Spawner(context, timers, library, queueManager)));
    }

    private final TimerScheduler<Message> timers;
    private final ActorRef<Library.Message> libraryActorRef;
    private final ActorRef<QueueManager.Message> queueManagerActorRef;

    int i;
    /*
    * Ablauf:
    * 1) In constructor wird die erste Nachricht an sich gesendet nach einem zufalligen Timer.
    * 2) Beim Erhalten der Nachricht wird ein Singer generiert.
    * 3) Danach wird direkt ein neuer Timer eingesetzt und die Schritte wiederholen sich.
    *
    * */
    private Spawner(ActorContext<Message> context, TimerScheduler<Message> timers, ActorRef<Library.Message> library, ActorRef<QueueManager.Message> queueManager) {
        super(context);
        this.timers = timers;
        this.libraryActorRef = library;
        this.queueManagerActorRef = queueManager;
        this.i = 0;
        this.doGenNewSinger();
    }

    // Funktion für die Generierung der Singers mit Timer
    private void doGenNewSinger() {
        this.i += 1;
        int randomTime = ThreadLocalRandom.current().nextInt(2, 12 + 1);
        Message msg = new GenNewSinger(randomTime);
        this.timers.startSingleTimer(msg, Duration.ofSeconds(randomTime));
    }


    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(GenNewSinger.class, this::onGenNewSinger)
                .build();
    }

    private Behavior<Message> onGenNewSinger(GenNewSinger msg) {
        this.getContext().spawn(Singer.create(libraryActorRef, queueManagerActorRef), Integer.toString(this.i));
        getContext().getLog().info("Singer number {} created after {} seconds", this.i, msg.time);
        this.doGenNewSinger();
        return this;
    }
}
