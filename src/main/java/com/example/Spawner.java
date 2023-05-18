package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;


public class Spawner extends AbstractBehavior<Spawner.Message> {

    public interface Message {};


    public record ExampleMessage(String someString) implements Message {  }

    public static Behavior<Message> create(ActorRef<Library.Message> library, ActorRef<QueueManager.Message> queueManager) {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new Spawner(context, timers, library, queueManager)));
    }

    private final TimerScheduler<Message> timers;
    private final ActorRef<Library.Message> libraryActorRef;
    private final ActorRef<QueueManager.Message> queueManagerActorRef;

    private Spawner(ActorContext<Message> context, TimerScheduler<Message> timers, ActorRef<Library.Message> library, ActorRef<QueueManager.Message> queueManager) {
        super(context);
        this.timers = timers;
        this.libraryActorRef = library;
        this.queueManagerActorRef = queueManager;

       // Message msg = new ExampleMessage("test123");
        //this.timers.startSingleTimer(msg, msg, Duration.ofSeconds(10));
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(ExampleMessage.class, this::onExampleMessage)
                .build();
    }

    private Behavior<Message> onExampleMessage(ExampleMessage msg) {
        getContext().getLog().info("I have send myself this message after 10 Seconds: {}", msg.someString);
        return this;
    }
}
