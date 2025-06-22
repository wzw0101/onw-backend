package priv.wzw.onw.statemachine;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class StateMachineContext {
    private boolean transitioned;
}
