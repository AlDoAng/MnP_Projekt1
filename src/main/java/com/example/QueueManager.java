package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashMap;

public class QueueManager extends AbstractBehavior<QueueManager.Message> {

    public interface Message {}

    public record ReadyMessage(ActorRef<PlaybackClient.Message> replyTo) implements Message {  }
    public record AddMessage(ActorRef<Singer.Message> replyTo, Song songToAdd) implements Message {  }

    public static Behavior<Message> create() {
        return Behaviors.setup(QueueManager::new);
    }

    private HashMap<ActorRef<Singer.Message>, Song> songSingerList;

    private QueueManager(ActorContext<Message> context) {
        super(context);
        songSingerList = new HashMap<>();
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadyMessage.class, this::onReadyMessage)
                .onMessage(AddMessage.class, this::onAddMessage)
                .build();
    }

    private Behavior<Message> onReadyMessage(ReadyMessage msg) {
        if(!songSingerList.isEmpty()) {
            ActorRef<Singer.Message> singerRef = songSingerList.keySet().iterator().next();
            Song song = songSingerList.remove(singerRef);
           // Song song = songSingerList.keySet().iterator().next();
           // ActorRef<Singer.Message> singerRef = songSingerList.remove(song);
            getContext().getLog().info("Send PlaybackClient a song {}", song.getTitle());
            msg.replyTo.tell(new PlaybackClient.Play(singerRef, song, this.getContext().getSelf()));
        }else{
         // getContext().getLog().info("No song in songList");
         }
        return this;
    }

    private Behavior<Message> onAddMessage(AddMessage msg) {
        getContext().getLog().info("QueueManager added {} \n", msg.songToAdd.getTitle());
        this.songSingerList.put(msg.replyTo, msg.songToAdd);
        return this;
    }
}
