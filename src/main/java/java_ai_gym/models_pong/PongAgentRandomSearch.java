package java_ai_gym.models_pong;
import java_ai_gym.models_common.*;
import java_ai_gym.models_agent_search.AgentSearch;

import java.util.ArrayList;
import java.util.List;


public class PongAgentRandomSearch extends AgentSearch {

    final int ACTION_DEFAULT = 1;

    int searchDepth;
    List<StepReturn> stepReturnSequence;
    List<Integer> actionSequence;
    State state;

    public PongAgentRandomSearch(SinglePong env,long timeBudget ,int searchDepth) {
        super(timeBudget,env,env.parameters);
        this.searchDepth = searchDepth;
        stepReturnSequence = new ArrayList<>();
        actionSequence = new ArrayList<>();
    }

    @Override
    public SearchResults search(final StateForSearch startState) {
        startTime = System.currentTimeMillis();  //starting time, long <=> minimum value of 0  //TODO needed?
        state = new StateForSearch(startState);

        while (!timeExceeded()) {
            state.copyState(startState);
            runEpisode();
            setNewSearchResultsIfBetterCandidateFound();
            searchResults.nofEpisodes++;
        }
        logWarningIfNoFeasibleActionSequenceFound();
        return searchResults;
    }

    @Override
    public int getActionDefault(StateForSearch selectState) {
        return ACTION_DEFAULT;
    }

    @Override
    public List<Integer> getActionSet (StateForSearch selectState) {
        return envParams.discreteActionsSpace;
    }


    private void runEpisode() {
        StepReturn stepReturn;
        stepReturnSequence.clear();
        actionSequence.clear();
        int depth = 0;
        do {
           int action = chooseRandomAction(envParams.discreteActionsSpace);
           stepReturn = env.step(action, state);
           stepReturnSequence.add(stepReturn);
           actionSequence.add(action);
           state.copyState(stepReturn.state);
           depth++;
       } while (depthNotExceedAndFailStateNotEncountered(depth,searchDepth,stepReturn));
    }



    private void setNewSearchResultsIfBetterCandidateFound() {
        double sumRewards = calcSumRewards(stepReturnSequence);
        if (sumRewards > searchResults.bestReturn) {
            searchResults.bestReturn = sumRewards;
            searchResults.bestStepReturnSequence = new ArrayList<>(stepReturnSequence);
            searchResults.bestActionSequence = new ArrayList<>(actionSequence);
            logger.finest("nofEpisodes:" + searchResults.nofEpisodes + ". Better sumRewards found:" + sumRewards);
        }
    }






}

