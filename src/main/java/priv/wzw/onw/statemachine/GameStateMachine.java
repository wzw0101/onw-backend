package priv.wzw.onw.statemachine;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import priv.wzw.onw.GameEvent;
import priv.wzw.onw.GameState;

import java.util.List;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class GameStateMachine extends StateMachine<GameState, GameEvent, GameContext> {
    public GameStateMachine(List<Transition<GameState, GameEvent, GameContext>> transitions) {
        super(GameState.INIT, transitions);
    }
}
