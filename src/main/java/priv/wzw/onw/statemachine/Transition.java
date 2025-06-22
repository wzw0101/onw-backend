package priv.wzw.onw.statemachine;

import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class Transition<S extends Enum<S>, E extends Enum<E>, C extends StateMachineContext> {
    private final EnumSet<S> sourceSet;
    private final E event;
    private final S target;
    private final Predicate<C> predicate;
    private final Consumer<C> onAccept;

    public Transition(EnumSet<S> sourceSet, E event, S target, Consumer<C> onAccept) {
        this(sourceSet, event, target, c -> true, onAccept);
    }

    public Transition(EnumSet<S> sourceSet, E event, S target) {
        this(sourceSet, event, target, c -> true, c -> {
        });
    }

    public Transition(S source, E event, S target, Predicate<C> predicate, Consumer<C> onAccept) {
        this(EnumSet.of(source), event, target, predicate, onAccept);
    }

    public Transition(S source, E event, S target, Predicate<C> predicate) {
        this(EnumSet.of(source), event, target, predicate, c -> {
        });
    }

    public Transition(S source, E event, S target, Consumer<C> onAccept) {
        this(EnumSet.of(source), event, target, onAccept);
    }

    public Transition(S source, E event, S target) {
        this(source, event, target, c -> true, c -> {
        });
    }

    public boolean canHandle(S source, E event, C context) {
        return this.sourceSet.contains(source) && this.event == event && predicate.test(context);
    }

    public final S handle(S source, E event, C context) {
        if (!canHandle(source, event, context)) {
            return null;
        }
        onAccept.accept(context);
        return target;
    }
}