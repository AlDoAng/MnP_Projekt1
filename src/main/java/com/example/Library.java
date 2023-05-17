package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.ArrayList;
import java.util.List;

public class Library extends AbstractBehavior<Library.Message> {

    public interface Message {};

    public record ListArtists(ActorRef<AkkaMainSystem.Create> someReference) implements Message {  }
    public record GetSongs(ActorRef<AkkaMainSystem.Create> someReference) implements Message {  }

    public static Behavior<Message> create(List<Song> songList) {
        return Behaviors.setup(context -> new Library(context, songList));
    }

    private final List<Song> songList;

    private Library(ActorContext<Message> context, List<Song> songList) {
        super(context);
        this.songList = songList;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(ListArtists.class, this::onListArtists)
                .onMessage(GetSongs.class, this::onGetSongs)
                .build();
    }

    private Behavior<Message> onListArtists(ListArtists msg) {
        List<String> artistList = new ArrayList<String>();
        for (Song song: songList) {
            artistList.add(song.getArtist());
        }
        return this;
    }

    private Behavior<Message> onGetSongs(GetSongs msg) {
        return this;
    }
}
