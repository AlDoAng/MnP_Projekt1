package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Random;

/*
 * Actor: Singer
 */
public class Singer extends AbstractBehavior<Singer.Message> {

    public interface Message {}

    public record ArtistsMessage(ActorRef<Library.Message> msgFrom, LinkedHashSet<String> artistList) implements Message {  }
    public record SongsMessage(ActorRef<Library.Message> msgFrom, ArrayList<Song> songsOfArtist) implements Message {  }
    public record StartSingingMessage(ActorRef<PlaybackClient.Message> msgFrom, Song songToSing) implements Message {  }

    public static Behavior<Message> create(ActorRef<Library.Message> library, ActorRef<QueueManager.Message> queueManager) {
        return Behaviors.setup(context -> new Singer(context, library, queueManager));
    }

    private final ActorRef<Library.Message> libraryActorRef;
    private final ActorRef<QueueManager.Message> queueManagerActorRef;

    /*
     * Nachdem Singer erstellt wurde, sendet er eine Anfrage nach alle 'Artists' an die Library
     */
    private Singer(ActorContext<Message> context, ActorRef<Library.Message> library, ActorRef<QueueManager.Message> queueManager) {
        super(context);
        this.libraryActorRef = library;
        this.queueManagerActorRef = queueManager;
        this.libraryActorRef.tell(new Library.ListArtists(this.getContext().getSelf()));
    }

    @Override
    public Receive<Message> createReceive() {
        //getContext().getLog().info("Singer created");
        return newReceiveBuilder()
                .onMessage(ArtistsMessage.class, this::onArtistsMessage)
                .onMessage(SongsMessage.class, this::onSongsMessage)
                .onMessage(StartSingingMessage.class, this::onStartSingingMessage)
                .build();
    }

    /*
     * Nachdem Singer eine Liste aller 'Artists' bekommen hat,
     * wird ein 'Artists' zufällig ausgewählt und
     * Lieder vom ausgewählten 'Artists' angefordert
     */
    private Behavior<Message> onArtistsMessage(ArtistsMessage msg) {
        Random random = new Random(System.currentTimeMillis());
        String choosenArtist = null;
        int randomNumber = random.nextInt(msg.artistList.size());
        int count = 0;
        for (String s: msg.artistList){
            if (count == randomNumber){
                choosenArtist = s;
                break;
            }
            count++;
        }
        msg.msgFrom.tell(new Library.GetSongs(this.getContext().getSelf(), choosenArtist));
        return this;
    }

    /*
     * Nachdem Singer angeforderte Lieder bekommen hat,
     * wird ein Lied zufällig ausgewählt und an den
     * QueueManager gesendet
     */
    private Behavior<Message> onSongsMessage(SongsMessage msg) {
        Random random = new Random(System.currentTimeMillis());
        Song choosenSong = null;
        int randomNumber = random.nextInt(msg.songsOfArtist.size());
        int count = 0;
        for (Song s: msg.songsOfArtist){
            if (count == randomNumber){
                choosenSong = s;
                break;
            }
            count++;
        }
        queueManagerActorRef.tell(new QueueManager.AddMessage(this.getContext().getSelf(), choosenSong));
        return this;
    }

    /*
     * Nachdem Singer eine Nachricht vom PlaybackClient bekommen hat,
     * gibt er folgenes aus:
     */
    private Behavior<Message> onStartSingingMessage(StartSingingMessage msg) {
        getContext().getLog().info("Start singing: " + msg.songToSing.getArtist()+
                " - "+msg.songToSing.getTitle()+" for "+msg.songToSing.getDuration()+" seconds");
        return this;
    }
}
