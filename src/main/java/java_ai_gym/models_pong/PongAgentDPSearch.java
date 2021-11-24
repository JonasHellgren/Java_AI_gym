package java_ai_gym.models_pong;

import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PongAgentDPSearch extends AgentSearch {

    final int MAX_NOF_SELECTION_TRIES = 100;
    final int ACTION_DEFAULT = 1;

    int searchDepthStep;
    int searchDepth;
    List<Integer> evaluatedSearchDepths;
    VisitedStatesBuffer vsb;
    //StateForSearch state;
    StateForSearch startState;

    public PongAgentDPSearch(SinglePong env,
                             long timeBudget,
                             int searchDepthStep) {
        super(timeBudget, env, env.parameters);
        this.searchDepthStep = searchDepthStep;
        this.searchDepth = searchDepthStep;
        this.evaluatedSearchDepths=new ArrayList<>();
        //this.state = new StateForSearch(env.getTemplateState());


    }


    @Override
    public SearchResults search(final StateForSearch startState) {

        //StateForSearch startState=(StateForSearch) startState0;

        setUpVsb(startState);
        int nofActions = envParams.discreteActionsSpace.size();

        int nofSteps = 0;
        int searchDepth = searchDepthStep;
        int explorationFactor = 0;

        return null;
    }

    public void addSearchDepth(int searchDepth) {
        evaluatedSearchDepths.add(searchDepth);
    }

    public void setUpVsb(StateForSearch startState) {
        this.startState = new StateForSearch(startState);
        int nofActions = envParams.discreteActionsSpace.size();
        this.startState.setIdDepthNofActions(this.startState.START_STATE_ID, 0, nofActions);
        vsb = new VisitedStatesBuffer(this.startState);
    }

    public int chooseAction(StateForSearch selectState) {
        int action;
        if (vsb.nofActionsTested(selectState.id) == 0) {
            action = ACTION_DEFAULT;
        } else {
            List<Integer> grossActions=envParams.discreteActionsSpace;
            List<Integer> testedActions= vsb.testedActions(selectState.id);
            List<Integer> nonTestedActions = MathUtils.getDifferenceBetweenLists(grossActions, testedActions);
            if (nonTestedActions.isEmpty()) {
                action = ACTION_DEFAULT;
                logger.warning("nonTestedActions is empty");
            } else {
                action = chooseRandomAction(nonTestedActions);
            }
        }
        return action;
    }

    public StateForSearch selectState() {
        StateForSearch selectedState;
        int i = 0;
        do {
            selectedState = vsb.selectRandomState();
            if (i > MAX_NOF_SELECTION_TRIES) {
                logger.warning("MAX_NOF_SELECTION_TRIES exceeded");
                selectedState = this.startState;
                break;
            }
            i++;
        } while (isTerminalStateOrAllActionsTestedOrIsAtMaxDepth(selectedState));
      return selectedState;
    }

    public boolean isTerminalStateOrAllActionsTestedOrIsAtMaxDepth(StateForSearch state) {
        StateExperience exp = vsb.searchExperienceOfSteppingToState(state.id);
        int nofActionsTested = vsb.nofActionsTested(state.id);
        boolean isAtMaxDepth=state.depth== searchDepth;
        return (exp.termState || (nofActionsTested == state.nofActions) || isAtMaxDepth);
    }
}
