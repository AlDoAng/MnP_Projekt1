package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.TimerScheduler;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.Duration;
import java.util.Objects;


public class PlaybackClient extends AbstractBehavior<PlaybackClient.Message> {

    public interface Message {}

    /*
    * Klasse repräsentiert ein Nachricht, die bei PlaybackClient erhalten wird.
    * replyTo ist Singer-Objekt, an dem die Nachricht StartSinging geschickt wird
    * songToPlay ist Song-Objekt, PlaybackClient wartet die so lange, wie die Dauer des Songs ist
    * msgFrom ist das Objekt (hier QueueManager), das die Play-Nachricht gesendet hat
    */
    public static final class Play implements Message{
        public final ActorRef<Singer.Message> replyTo;
        public final Song songToPlay;
        public final ActorRef<QueueManager.Message> msgFrom;

        public Play(ActorRef<Singer.Message> replyTo, Song songToPlay, ActorRef<QueueManager.Message> msgFrom) {
            this.replyTo = replyTo;
            this.songToPlay = songToPlay;
            this.msgFrom = msgFrom;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Play) obj;
            return Objects.equals(this.replyTo, that.replyTo) &&
                    Objects.equals(this.songToPlay, that.songToPlay) &&
                    Objects.equals(this.msgFrom, that.msgFrom);
        }

        @Override
        public String toString() {
            return "Play[" +
                    "replyTo=" + replyTo + ", " +
                    "songToPlay=" + songToPlay + ", " +
                    "msgFrom=" + msgFrom + ']';
        }
    }

    public record SendPlayEnd(Play msg) implements Message {  }
    public record IsPlaying(ActorRef<QueueManager.Message> replyTo, Song song, ActorRef<Singer.Message> replyToSinger) implements Message {}
    public static Behavior<Message> create() {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new PlaybackClient(context, timers)));
    }

    private final TimerScheduler<PlaybackClient.Message> timers;

    private boolean isPlaying;

    private PlaybackClient(ActorContext<Message> context,TimerScheduler<Message> timers) {
        super(context);
        this.timers = timers;
        this.isPlaying = false;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Play.class, this::onPlay)
                .onMessage(SendPlayEnd.class, this::onSendPlayEnd)
                .onMessage(IsPlaying.class, this::onIsPlaying)
                .build();

    }

    /*
    * 1) sende Startsinging-Nachricht an dem Singer-Objekt
    * 2) warte Dauer des Songs
    * 3) schreibe Log-Nachricht
    * 4) sende Ready-Nachricht an dem QueueManager
    */
    private Behavior<Message> onPlay(Play msg){
        isPlaying = true;
        msg.replyTo.tell(new Singer.StartSingingMessage(this.getContext().getSelf(), msg.songToPlay)); // TODO: quick fix, must be checked
        this.timers.startSingleTimer(new SendPlayEnd(msg),
                Duration.ofSeconds(msg.songToPlay.getDuration()));

        return this;
    }

    private Behavior<Message> onIsPlaying(IsPlaying msg){
        msg.replyTo.tell(new QueueManager.ClientIsPlaying(getContext().getSelf(), isPlaying, msg.song, msg.replyToSinger));
        return this;
    }


    private Behavior<Message> onSendPlayEnd(SendPlayEnd sendPlayEndMsg){
        this.getContext().getLog().info("PlaybackClient played " + sendPlayEndMsg.msg.songToPlay.getTitle() + ": Done");
        isPlaying = false;
        sendPlayEndMsg.msg.msgFrom.tell(new QueueManager.ReadyMessage(getContext().getSelf()));
        return this;
    }
}
