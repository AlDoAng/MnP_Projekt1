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
        ActorRef<Library.Message> library = this.getContext().spawn(Library.create("library"), "library");
        ActorRef<QueueManager.Message> queueManager = this.getContext().spawn(QueueManager.create("QueueManager"), "queueManager");
        ActorRef<Spawner.Message> spawner = this.getContext().spawn(Spawner.create("Spawner"), "spawner");
        ActorRef<PlaybackClient.Message> playbackClient = this.getContext().spawn(PlaybackClient.create(), "playbackClient");
        //#create-actors


        // library.tell(new ExampleActor.ExampleMessage(this.getContext().getSelf(),"Test123"));
        return this;
    }
}