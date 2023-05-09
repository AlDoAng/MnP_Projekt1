package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class AkkaMainSystem extends AbstractBehavior<AkkaMainSystem.Create> {

    public static class Create {
    }

    public static Behavior<Create> create() {
        return Behaviors.setup(AkkaMainSystem::new);
    }

    private AkkaMainSystem(ActorContext<Create> context) {
        super(context);
    }

    @Override
    public Receive<Create> createReceive() {
        return newReceiveBuilder().onMessage(Create.class, this::onCreate).build();
    }

    private Behavior<Create> onCreate(Create command) {
        //#create-actors
        ActorRef<ExampleActor.Message> a = this.getContext().spawn(ExampleActor.create("Alice"), "alice");
        ActorRef<ExampleTimerActor.Message> b = this.getContext().spawn(ExampleTimerActor.create(), "timeractor");
        //#create-actors

        a.tell(new ExampleActor.ExampleMessage(this.getContext().getSelf(),"Test123"));
        return this;
    }
}
