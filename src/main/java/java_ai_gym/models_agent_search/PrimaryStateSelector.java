package java_ai_gym.models_agent_search;

import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.StateForSearch;

import java.util.logging.Logger;

public class PrimaryStateSelector implements StateSelector {

    protected final static Logger logger = Logger.getLogger(PrimaryStateSelector.class.getName());

    AgentDPSearch agent;

    public PrimaryStateSelector(AgentDPSearch agent) {
        this.agent = agent;
    }

    @Override
    public boolean isStateSelectorOfPrimaryType() {
        return true;
    }

    @Override
    public StateForSearch selectState() {  //todo vsbForNewDepthSet

        StateForSearch selectedState = null;  //hopefully will change type later

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
                  return selectedState;
            }
        }

        this.logsForFailedToFindState(selectedState);
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

    protected void logsForFailedToFindState(StateForSearch selectedState) {
        logger.warning("MAX_NOF_SELECTION_TRIES exceeded !!! Probably is explorationFactorLimit high = "+agent.dpSearchServant.explorationFactorLimit);
        logger.warning("id =" + selectedState.id +
                ", depth =" + selectedState.depth +
                // ", null status =" + (selectedState == null) +
                ", depth status =" + (selectedState.depth == agent.searchDepth) +
                ", nofActionsTested status =" + (agent.vsb.nofActionsTested(selectedState.id) == selectedState.nofActions) +
                ",isExperienceOfStateTerminal =" + agent.vsb.isExperienceOfStateTerminal(selectedState.id));
    }

}
