package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.ArrayList;
import java.util.List;

public class AkkaMainSystem extends AbstractBehavior<AkkaMainSystem.Create> {

    Song songD1 = new Song("Drake","Forever", 12);
    Song songD2 = new Song("Drake","Headlines", 7);
    Song songD3 = new Song("Drake","Best i eveer had", 9);
    Song songBB1 = new Song("Bad Bunny","Diles", 10);
    Song songES1 = new Song("Ed Sheeran","Perfect", 9);
    Song songTS1 = new Song("Taylor Swift","Love Story", 8);
    private final List<Song> songList = new ArrayList<>();


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
        songList.add(songD1);
        songList.add(songD2);
        songList.add(songD3);
        songList.add(songBB1);
        songList.add(songES1);
        songList.add(songTS1);

        ActorRef<Library.Message> library = this.getContext().spawn(Library.create(songList), "library");
        ActorRef<QueueManager.Message> queueManager = this.getContext().spawn(QueueManager.create(), "queueManager");
        ActorRef<Spawner.Message> spawner = this.getContext().spawn(Spawner.create(library,queueManager), "spawner");
        ActorRef<PlaybackClient.Message> playbackClient = this.getContext().spawn(PlaybackClient.create(queueManager), "playbackClient");

        //#create-actors
        // library.tell(new ExampleActor.ExampleMessage(this.getContext().getSelf(),"Test123"));
        return this;
    }
}