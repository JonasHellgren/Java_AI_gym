package java_ai_gym.models_agent_search;

import java_ai_gym.models_common.StateForSearch;

public class PrimaryStateSelector implements CanSelectState {
    @Override
    public boolean isStateSelectorOfPrimaryType() {
        return true;
    }

    @Override
    public StateForSearch selectState() {
        return null;
    }
}
