package priv.wzw.onw.statemachine;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StateMachine<S extends Enum<S>, E extends Enum<E>, C extends StateMachineContext> {
    private final List<Transition<S, E, C>> transitions;
    @Getter
    private S currentState;

    public StateMachine(S initialState, List<Transition<S, E, C>> transitions) {
        this.currentState = initialState;
        this.transitions = Objects.nonNull(transitions) ? transitions : Collections.emptyList();
    }

    public void sendEvent(E event, C context) {
        transitions.stream()
                .filter(transition -> transition.canHandle(currentState, event, context))
                .findFirst()
                .ifPresent(transition -> {
                    currentState = transition.handle(currentState, event, context);
                    context.setTransitioned(true);
                });
    }
}
