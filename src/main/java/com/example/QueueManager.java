package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashMap;
import java.util.List;

public class QueueManager extends AbstractBehavior<QueueManager.Message> {

    public interface Message {};

    public record ReadyMessage(ActorRef<AkkaMainSystem.Create> replyTo) implements Message {  }
    public record AddMessage(ActorRef<AkkaMainSystem.Create> replyTo, Song songToAdd) implements Message {  }

    public static Behavior<Message> create(/*List<Song> songList*/) {
        return Behaviors.setup(context -> new QueueManager(context/*, songList*/));
    }

    //private final List<Song> songList;
    private HashMap<Song, ActorRef<AkkaMainSystem.Create>> songSingerList;

    private QueueManager(ActorContext<Message> context/*, List<Song> songList*/) {
        super(context);
       // this.songSingerList = songList;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadyMessage.class, this::onReadyMessage)
                .onMessage(AddMessage.class, this::onAddMessage)
                .build();
    }

    private Behavior<Message> onReadyMessage(ReadyMessage msg) {
        return this;
    }

    private Behavior<Message> onAddMessage(AddMessage msg) {
        this.songSingerList.put(msg.songToAdd, msg.replyTo);
        return this;
    }
}
