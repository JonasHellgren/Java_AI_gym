package java_ai_gym.models_common;

import java_ai_gym.models_pong.PongAgentRandomSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public abstract class AgentSearch {

    protected final static Logger logger = Logger.getLogger(AgentSearch.class.getName());

    public class SearchResults {
        public double bestReturn;
        public List<StepReturn> bestStepReturnSequence;
        public List<Integer> bestActionSequence;
        public int nofEpisodes;
        List<Integer> discreteActionsSpace;

        public SearchResults(double bestReturn, List<Integer> discreteActionsSpace) {
            this.bestReturn = bestReturn;
            this.bestStepReturnSequence = new ArrayList<>();
            this.bestActionSequence = new ArrayList<>();
            this.nofEpisodes = 0;
            this.discreteActionsSpace = discreteActionsSpace;
        }

        public boolean isResultOk() {
            for (StepReturn sr : bestStepReturnSequence) {
                if (sr.termState)
                    return false;
            }
            return true;
        }

        public int firstAction() {
            if (bestActionSequence.size() == 0) {
                logger.warning("No actionSequence defined");
                return chooseRandomAction(discreteActionsSpace);
            } else {
                return bestActionSequence.get(0);
            }
        }
    }

    protected long timeBudget;
    protected Environment env;
    protected EnvironmentParametersAbstract envParams;
    protected SearchResults searchResults;
    protected final Random random;

    public AgentSearch(long timeBudget, Environment env, EnvironmentParametersAbstract envParams) {
        this.timeBudget = timeBudget;
        this.env = env;
        this.envParams = envParams;
        this.searchResults = new SearchResults(-Double.MAX_VALUE, envParams.discreteActionsSpace);
        random = new Random();
    }

    public abstract SearchResults search(final State startState);

    protected double calcSumRewards(List<StepReturn> stepReturnSequence) {
        return stepReturnSequence.stream()
                .map(StepReturn::getReward)
                .reduce(0d, Double::sum);
    }

    public int chooseRandomAction(List<Integer> actions) {
        return actions.get(random.nextInt(actions.size()));
    }

    protected boolean depthNotExceedAndFailStateNotEncountered(int depth, int searchDepth, StepReturn stepReturn) {
        return depth < searchDepth && !stepReturn.termState;
    }

    protected boolean timeNotExceeded(long startTime) {
        return System.currentTimeMillis() < startTime + timeBudget;
    }

    protected void logWarningIfNoFeasibleActionSequenceFound() {
        if (!searchResults.isResultOk()) {
            logger.warning("No feasible action sequence found");
        }
    }

}
