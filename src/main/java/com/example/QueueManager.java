package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;

public class QueueManager extends AbstractBehavior<QueueManager.Message> {

    public interface Message {};

    public record ReadyMessage(ActorRef<AkkaMainSystem.Create> someReference) implements Message {  }
    public record AddMessage(ActorRef<AkkaMainSystem.Create> someReference) implements Message {  }

    public static Behavior<Message> create(List<Song> songList) {
        return Behaviors.setup(context -> new QueueManager(context, songList));
    }

    private final List<Song> songList;

    private QueueManager(ActorContext<Message> context, List<Song> songList) {
        super(context);
        this.songList = songList;
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
        return this;
    }
}
