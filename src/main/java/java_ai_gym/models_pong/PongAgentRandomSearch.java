package java_ai_gym.models_pong;

import java_ai_gym.models_common.Environment;
import java_ai_gym.models_common.EnvironmentParametersAbstract;
import java_ai_gym.models_common.State;
import java_ai_gym.models_common.StepReturn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class PongAgentRandomSearch {

    private final static Logger logger = Logger.getLogger(PongAgentRandomSearch.class.getName());

    public static class SearchResults {
        public double bestReturn;
        public List<StepReturn> bestStepReturnSequence;
        public List<Integer> bestActionSequence;
        public int nofEpisodes;

        public SearchResults(double bestReturn, List<StepReturn> bestActionSequence) {
            this.bestReturn = bestReturn;
            this.bestStepReturnSequence = bestActionSequence;
            this.bestActionSequence = new ArrayList<>();
            this.nofEpisodes = 0;
        }

        private boolean isResultOk() {
            for (StepReturn sr : bestStepReturnSequence) {
                if (sr.termState)
                    return false;
            }
            return true;
        }

        public int firstAction() {
            if (bestActionSequence.size() == 0) {
                logger.warning("No actionSequence defined");
                return 0;
            } else {
                return bestActionSequence.get(0);
            }
        }

    }

    long timeBudget;
    int searchDepth;
    Environment env;
    EnvironmentParametersAbstract envParams;
    SearchResults searchResults;
    private final Random random = new Random();

    public PongAgentRandomSearch(long timeBudget, int searchDepth, Environment env, EnvironmentParametersAbstract envParams) {
        this.timeBudget = timeBudget;
        this.searchDepth = searchDepth;
        this.env = env;
        this.envParams = envParams;

        searchResults = new SearchResults(-Double.MAX_VALUE, new ArrayList<>());
    }


    public SearchResults getSearchResults() {
        return searchResults;
    }

    public SearchResults search(final State startState) {
        long startTime = System.currentTimeMillis();  //starting time, long <=> minimum value of 0
        List<StepReturn> stepReturnSequence = new ArrayList<>();
        List<Integer> actionSequence = new ArrayList<>();
        State state = new State(startState);
        StepReturn stepReturn;

        while (System.currentTimeMillis() < startTime + timeBudget) {
            //for (int i = 0; i <10 ; i++) {

            stepReturnSequence.clear();
            actionSequence.clear();
            state.copyState(startState);
            int depth = 0;
             do {
                int action = chooseRandomAction(envParams.discreteActionsSpace);
                stepReturn = env.step(action, state);
                stepReturnSequence.add(stepReturn);
                actionSequence.add(action);
                state.copyState(stepReturn.state);
                depth++;
            } while (depthNotExceedAndFailStateNotEncountered(stepReturn, depth));

            setNewSearchResultsIfBetterCandidateFound(stepReturnSequence, actionSequence);
            searchResults.nofEpisodes++;
        }
        logWarningIfNoFeasibleActionSequenceFound();
        return searchResults;
    }

    private boolean depthNotExceedAndFailStateNotEncountered(StepReturn stepReturn, int depth) {
        return depth < searchDepth && !stepReturn.termState;
    }

    private void logWarningIfNoFeasibleActionSequenceFound() {
        if (!searchResults.isResultOk()) {
            logger.warning("No feasible action sequence found");
        }
    }

    private void setNewSearchResultsIfBetterCandidateFound(List<StepReturn> stepReturnSequence, List<Integer> actionSequence) {
        double sumRewards = calcSumRewards(stepReturnSequence);
        if (sumRewards > searchResults.bestReturn) {
            searchResults.bestReturn = sumRewards;
            searchResults.bestStepReturnSequence = new ArrayList<>(stepReturnSequence);
            searchResults.bestActionSequence = new ArrayList<>(actionSequence);
            logger.info("nofEpisodes:" + searchResults.nofEpisodes + ". Better sumRewards found:" + sumRewards);
        }
    }

    private int chooseRandomAction(List<Integer> actions) {
        return actions.get(random.nextInt(actions.size()));
    }

    private double calcSumRewards(List<StepReturn> stepReturnSequence) {
        return stepReturnSequence.stream()
                .map(StepReturn::getReward)
                .reduce(0d, Double::sum);
    }


}

