package com.example;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;


public class Spawner extends AbstractBehavior<Spawner.Message> {

    public interface Message {};


    public record ExampleMessage(String someString) implements Message {  }

    public static Behavior<Message> create() {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new Spawner(context, timers)));
    }

    private final TimerScheduler<Message> timers;

    private Spawner(ActorContext<Message> context, TimerScheduler<Message> timers) {
        super(context);
        this.timers = timers;

        Message msg = new ExampleMessage("test123");
        this.timers.startSingleTimer(msg, msg, Duration.ofSeconds(10));
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
