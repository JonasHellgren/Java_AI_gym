package java_ai_gym.models_agent_search;

import java_ai_gym.models_common.StateForSearch;

import java.util.logging.Logger;

/***
 * This class is used for state selection of the AgentDPSearch class.
 */


public class DPSearchStateSelector {

    protected final static Logger logger = Logger.getLogger(DPSearchStateSelector.class.getName());

    AgentDPSearch agent;
    boolean wasSelectStateFailing;
    StateSelector stateSelector;

    public DPSearchStateSelector(AgentDPSearch agentDPSearch) {
        this.agent = agentDPSearch;
        this.wasSelectStateFailing=false;
        this.stateSelector =new PrimaryStateSelector(this.agent);
    }

    public void setStateSelectorAsPrimaryType() {
        this.stateSelector =new PrimaryStateSelector(this.agent);
    }

    public void setStateSelectorAsBackupType() {
        this.stateSelector=new BackupStateSelector(this.agent);
    }

    public boolean wasPrimarySelectStateFailing() {
        return wasSelectStateFailing && stateSelector.isStateSelectorOfPrimaryType();
    }

    public boolean wasBackupSelectStateFailing() {
        return wasSelectStateFailing && !stateSelector.isStateSelectorOfPrimaryType();
    }

    public void setWasSelectStateFailing(boolean wasSelectStateFailing) {
        this.wasSelectStateFailing = wasSelectStateFailing;
    }

    public StateForSearch selectState() {
        agent.timeAccumulatorSelectState.play();
        StateForSearch state=stateSelector.selectState();
        agent.timeAccumulatorSelectState.pause();
        wasSelectStateFailing=(state==null);
        return state;
    }



}
