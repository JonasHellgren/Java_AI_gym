package java_ai_gym.models_agent_search;

import java_ai_gym.models_common.StateForSearch;

public interface CanSelectState {

    boolean isStateSelectorOfPrimaryType();
    StateForSearch selectState();

}
