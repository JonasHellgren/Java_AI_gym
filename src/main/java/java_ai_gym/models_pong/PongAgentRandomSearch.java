package java_ai_gym.models_pong;
import java_ai_gym.models_common.*;
import java.util.ArrayList;
import java.util.List;


public class PongAgentRandomSearch extends AgentSearch {

    int searchDepth;
    List<StepReturn> stepReturnSequence;
    List<Integer> actionSequence;
    State state;

    public PongAgentRandomSearch(long timeBudget, int searchDepth, Environment env, EnvironmentParametersAbstract envParams) {
        super(timeBudget,env,envParams);
        this.searchDepth = searchDepth;
        stepReturnSequence = new ArrayList<>();
        actionSequence = new ArrayList<>();
    }

    @Override
    public SearchResults search(final State startState) {
        long startTime = System.currentTimeMillis();  //starting time, long <=> minimum value of 0
        state = new State(startState);

        while (timeNotExceeded(startTime)) {
            state.copyState(startState);
            runEpisode();
            setNewSearchResultsIfBetterCandidateFound();
            searchResults.nofEpisodes++;
        }
        logWarningIfNoFeasibleActionSequenceFound();
        return searchResults;
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

