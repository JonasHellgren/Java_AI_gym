package java_ai_gym.models_pong;

import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.StateForSearch;

/***
 * This class is used for state selection of the AgentDPSearch class.
 */

public class DPSearchStateSelector {

    AgentDPSearch agent;

    public DPSearchStateSelector(AgentDPSearch agentDPSearch) {
        this.agent = agentDPSearch;
    }

    public StateForSearch selectState() {
        StateForSearch selectedState = null;  //hopefully will change type later
        agent.timeAccumulatorSelectState.play();
        for (int j = 0; j < agent.MAX_NOF_SELECTION_TRIES; j++) {
            if (MathUtils.calcRandomFromIntervall(0, 1) < agent.PROB_SELECT_STATE_FROM_NEW_DEPTH_SET && agent.vsbForNewDepthSet.size() > 0) {
                selectedState = agent.vsbForNewDepthSet.selectRandomState();
            } else {
                if (MathUtils.calcRandomFromIntervall(0, 1) < agent.PROB_SELECT_FROM_OPTIMAL_PATH && agent.optimalStateSequence.size() > 0) {
                    if (MathUtils.calcRandomFromIntervall(0, 1) < agent.PROB_SELECT_FROM_PREVIOUS_DEPTH) {
                        selectedState = agent.optimalStateSequence.get(agent.optimalStateSequence.size() - 1);
                    } else {
                        selectedState = agent.optimalStateSequence.get(MathUtils.randInt(0, agent.optimalStateSequence.size() - 1));
                    }

                } else {
                    if (MathUtils.calcRandomFromIntervall(0, 1) < agent.PROB_SELECT_FROM_PREVIOUS_DEPTH) {
                        selectedState = agent.vsb.selectRandomStateFromDepth(agent.searchDepthPrev);
                    } else {
                        selectedState = agent.vsb.selectRandomState();
                    }
                }
            }

            if (!isTerminalStateOrAllActionsTestedOrIsAtSearchDepthOrNull(selectedState)) {
                agent.wasSelectStateFailing = false;
                agent.timeAccumulatorSelectState.pause();
                return selectedState;
            }
        }

        agent.timeAccumulatorSelectState.pause();
        agent.wasSelectStateFailing = true;
        agent.dpSearchServants.logsForFailedToFindState(selectedState);
        return null;
    }

    public boolean isTerminalStateOrAllActionsTestedOrIsAtSearchDepthOrNull(StateForSearch state) {

        //fail fast => speeding up
        if (state.depth == agent.searchDepth) {
            return true;
        }

        if (agent.vsb.nofActionsTested(state.id) == state.nofActions) {
            return true;
        }

        if (agent.vsb.isExperienceOfStateTerminal(state.id)) {
            return true;
        }

        return false;
    }

}
