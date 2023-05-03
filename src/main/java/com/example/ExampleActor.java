package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class ExampleActor extends AbstractBehavior<ExampleActor.Message> {

    public interface Message {};

    public record ExampleMessage(ActorRef<AkkaMainSystem.Create> someReference, String someString) implements Message {  }

    public static Behavior<Message> create(String name) {
        return Behaviors.setup(context -> new ExampleActor(context, name));
    }

    private final String name;

    private ExampleActor(ActorContext<Message> context, String name) {
        super(context);
        this.name = name;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(ExampleMessage.class, this::onExampleMessage)
                .build();
    }

    private Behavior<Message> onExampleMessage(ExampleMessage msg) {
        getContext().getLog().info("I ({}) got a message: ExampleMessage({},{})", this.name, msg.someReference, msg.someString);
        return this;
    }
}
