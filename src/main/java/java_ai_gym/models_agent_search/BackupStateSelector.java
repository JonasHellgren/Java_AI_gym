package java_ai_gym.models_agent_search;

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

            for (StateForSearch state : agent.vsbForNewDepthSet.stateVisitsDAO.getAll()) {
                if (!agent.vsbForNewDepthSet.areAllActionsTriedInStateWithId(state.id) &&
                        !agent.vsbForNewDepthSet.isExperienceOfStateTerminal(state.id) &&
                        state.depth!=agent.searchDepth) {
                    return agent.vsbForNewDepthSet.stateVisitsDAO.get(state.id);
                }
            }
            return null;
        }

}
