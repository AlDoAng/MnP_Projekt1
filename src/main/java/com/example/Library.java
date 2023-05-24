package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class Library extends AbstractBehavior<Library.Message> {

    public interface Message {}

    public record ListArtists(ActorRef<Singer.Message> replyTo) implements Message {  }
    public record GetSongs(ActorRef<Singer.Message> replyTo, String choosenArtist) implements Message {  }

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
//        List<String> artistList = new ArrayList<String>();
//        for (Song song: songList) {
//            artistList.add(song.getArtist());
//        }
//        return this;
        LinkedHashSet<String> artistlist = new LinkedHashSet<>();
        for (Song song: songList)
            artistlist.add(song.getArtist());
       // getContext().getLog().info("Artist list sent to {}", msg.replyTo);
        msg.replyTo.tell(new Singer.ArtistsMessage(this.getContext().getSelf(), artistlist));
        return this;
    }

    private Behavior<Message> onGetSongs(GetSongs msg) {
        ArrayList<Song> songsofArtist = new ArrayList<>();
        for (Song song: songList){
            if (song.getArtist().equals(msg.choosenArtist)){
                songsofArtist.add(song);
            }
        }
        //getContext().getLog().info("Artist {} choosen by {}", msg.choosenArtist, msg.replyTo);
        msg.replyTo.tell(new Singer.SongsMessage(this.getContext().getSelf(), songsofArtist));
        return this;
    }
}
