package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Singer extends AbstractBehavior<Singer.Message> {

    public interface Message {};

    public record ArtistsMessage(ActorRef<AkkaMainSystem.Create> someReference) implements Message {  }
    public record SongsMessage(ActorRef<AkkaMainSystem.Create> someReference) implements Message {  }
    public record StartSingingMessage(ActorRef<AkkaMainSystem.Create> someReference) implements Message {  }

    public static Behavior<Message> create() {
        return Behaviors.setup(context -> new Singer(context));
    }

    private Singer(ActorContext<Message> context) {
        super(context);
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(ArtistsMessage.class, this::onArtistsMessage)
                .onMessage(SongsMessage.class, this::onSongsMessage)
                .onMessage(StartSingingMessage.class, this::onStartSingingMessage)
                .build();
    }

    private Behavior<Message> onArtistsMessage(ArtistsMessage msg) {
        return this;
    }

    private Behavior<Message> onSongsMessage(SongsMessage msg) {
        return this;
    }

    private Behavior<Message> onStartSingingMessage(StartSingingMessage msg) {
        return this;
    }
}
