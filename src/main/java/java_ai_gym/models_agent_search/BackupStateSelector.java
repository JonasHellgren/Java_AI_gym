package java_ai_gym.models_agent_search;

import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.StateForSearch;

public class BackupStateSelector implements StateSelector {

    AgentDPSearch agent;

    public BackupStateSelector(AgentDPSearch agent) {
        this.agent = agent;
    }

    @Override
    public boolean isStateSelectorOfPrimaryType() {
        return false;
    }

    @Override
    public StateForSearch selectState() {

           VisitedStatesBuffer  vsb=agent.vsbForNewDepthSet;

            for (StateForSearch state : vsb.stateVisitsDAO.getAll()) {
                if (!vsb.areAllActionsTriedInStateWithId(state.id) &&
                        !vsb.isExperienceOfStateTerminal(state.id) &&
                        state.depth!=agent.searchDepth) {
                    return vsb.stateVisitsDAO.get(state.id);
                }
            }
            return null;
        }

}
