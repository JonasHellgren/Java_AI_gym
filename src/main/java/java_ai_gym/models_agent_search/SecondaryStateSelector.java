package java_ai_gym.models_agent_search;

import java_ai_gym.models_common.StateForSearch;

public class SecondaryStateSelector implements CanSelectState {
    @Override
    public boolean isStateSelectorOfPrimaryType() {
        return false;
    }

    @Override
    public StateForSearch selectState() {
        return null;
    }
}
