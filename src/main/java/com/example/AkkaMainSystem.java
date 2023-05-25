package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.ArrayList;
import java.util.List;

/*
 * Gruppe:
 *  Alina Ignatova 226735,
 *  Ha Phuong Ta 230655,
 *  Valeriya Mikhalskaya 229099,
 *  Janis Melon 209928
 */

// AkkaMainSystem: erstellt die Aktoren und startet alles
public class AkkaMainSystem extends AbstractBehavior<AkkaMainSystem.Create> {
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
        fillSongList();
        //#create-actors: Library, PlaybackClient, QueueManager und Spawner werden erstellt
        ActorRef<Library.Message> library = this.getContext().spawn(Library.create(songList), "library");
        ActorRef<PlaybackClient.Message> playbackClient = this.getContext().spawn(PlaybackClient.create(), "playbackClient");
        ActorRef<QueueManager.Message> queueManager = this.getContext().spawn(QueueManager.create(playbackClient), "queueManager");
        ActorRef<Spawner.Message> spawner = this.getContext().spawn(Spawner.create(library,queueManager), "spawner");
        //#create-actors
        return this;
    }

    // Songliste für die Library
    private void fillSongList() {
        Song songD1 = new Song("Drake","Forever", 12);
        Song songD2 = new Song("Drake","Headlines", 7);
        Song songD3 = new Song("Drake","Best I Ever Had", 9);
        Song songBB1 = new Song("Bad Bunny","Diles", 10);
        Song songBB2 = new Song("Bad Bunny","Soy Peor", 9);
        Song songBB3 = new Song("Bad Bunny","I Like It", 8);
        Song songES1 = new Song("Ed Sheeran","Thinking Out Loud", 10);
        Song songES2 = new Song("Ed Sheeran","Perfect", 9);
        Song songES3 = new Song("Ed Sheeran","Photograph", 9);
        Song songTS1 = new Song("Taylor Swift","Shake it off", 7);
        Song songTS2 = new Song("Taylor Swift","Love Story", 8);
        Song songTS3 = new Song("Taylor Swift","Blank Space", 8);

        songList.add(songD1);
        songList.add(songD2);
        songList.add(songD3);
        songList.add(songBB1);
        songList.add(songBB2);
        songList.add(songBB3);
        songList.add(songES1);
        songList.add(songES2);
        songList.add(songES3);
        songList.add(songTS1);
        songList.add(songTS2);
        songList.add(songTS3);
    }
}