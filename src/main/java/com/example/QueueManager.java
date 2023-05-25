package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashMap;
/*
 * Actor: QueueManager
 */
public class QueueManager extends AbstractBehavior<QueueManager.Message> {

    public interface Message {}

    public record ReadyMessage(ActorRef<PlaybackClient.Message> replyTo) implements Message {  }
    public record AddMessage(ActorRef<Singer.Message> replyTo, Song songToAdd) implements Message {  }
    public record ClientIsPlaying(ActorRef<PlaybackClient.Message> replyTo, boolean isPlaying, Song song, ActorRef<Singer.Message> replyToSinger) implements Message {}
    public static Behavior<Message> create(ActorRef<PlaybackClient.Message> playbackClient) {
        return Behaviors.setup(context -> new QueueManager(context, playbackClient));
    }

    /*
     * songSingerList ist für die Wiedergabe des Liedes verantwortlich
     */
    private HashMap<ActorRef<Singer.Message>, Song> songSingerList;
    private final ActorRef<PlaybackClient.Message> playbackClient;

    private QueueManager(ActorContext<Message> context, ActorRef<PlaybackClient.Message> playbackClient) {
        super(context);
        songSingerList = new HashMap<>();
        this.playbackClient = playbackClient;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadyMessage.class, this::onReadyMessage)
                .onMessage(AddMessage.class, this::onAddMessage)
                .onMessage(ClientIsPlaying.class, this::onClientIsPlaying)
                .build();
    }

    /*
     * QueueManager bekommt eine Nachricht vom PlaybackClient.
     * Falls die Wiedergabeliste nicht leer ist,
     * wird das erste Lied aus der Liste entfernt und
     * an den PlaybackClient gesendet.
     */
    private Behavior<Message> onReadyMessage(ReadyMessage msg) {
        if(!songSingerList.isEmpty()) {
            ActorRef<Singer.Message> singerRef = songSingerList.keySet().iterator().next();
            Song song = songSingerList.remove(singerRef);
            getContext().getLog().info("Send PlaybackClient a song {}", song.getTitle());
            msg.replyTo.tell(new PlaybackClient.Play(singerRef, song, this.getContext().getSelf()));
        }
        return this;
    }

    /*
     * QueueManager bekommt eine Nachricht vom Singer, die ein Lied enthält.
     * Ist die Wiedergabe leer, wird das Lied direkt an den PlaybackClient gesendet.
     * Sonst wird das Lied zur Wiedergabe hinzugefügt.
     */
    private Behavior<Message> onAddMessage(AddMessage msg) {
        if (songSingerList.isEmpty()){
            playbackClient.tell(new PlaybackClient.IsPlaying(this.getContext().getSelf(), msg.songToAdd, msg.replyTo));
        }else{
            songSingerList.put(msg.replyTo,msg.songToAdd);
            getContext().getLog().info("'{}': is added to the waitlist", msg.songToAdd.getTitle());
        }
        return this;
    }

    /*
     * Die Methode ist für die Information über den Zustand des Liedes verantwortlich
     */
    private Behavior<Message> onClientIsPlaying(ClientIsPlaying msg){
        if (!msg.isPlaying){
            playbackClient.tell(new PlaybackClient.Play(msg.replyToSinger, msg.song ,getContext().getSelf()));
            getContext().getLog().info("'{}': is played directly", msg.song.getTitle());
        }
        else {
            songSingerList.put(msg.replyToSinger, msg.song);
            getContext().getLog().info("'{}': is added to the waitlist", msg.song.getTitle());
        }
        return this;
    }
}
