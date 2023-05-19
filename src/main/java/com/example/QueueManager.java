package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashMap;

public class QueueManager extends AbstractBehavior<QueueManager.Message> {

    public interface Message {};

    public record ReadyMessage(ActorRef<PlaybackClient.Message> replyTo) implements Message {  }
    public record AddMessage(ActorRef<Singer.Message> replyTo, Song songToAdd) implements Message {  }

    public static Behavior<Message> create() {
        return Behaviors.setup(QueueManager::new);
    }

    private HashMap<Song, ActorRef<Singer.Message>> songSingerList;

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
            Song song = songSingerList.keySet().iterator().next();
            ActorRef<Singer.Message> singerRef = songSingerList.remove(song);
            msg.replyTo.tell(new PlaybackClient.Play(singerRef, song, this.getContext().getSelf()));
        }
        return this;
    }

    private Behavior<Message> onAddMessage(AddMessage msg) {
        this.songSingerList.put(msg.songToAdd, msg.replyTo);
        return this;
    }
}
