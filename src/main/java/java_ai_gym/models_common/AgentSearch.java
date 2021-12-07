package java_ai_gym.models_common;

import java_ai_gym.helpers.CpuTimer;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

@Getter
public abstract class AgentSearch {

    protected final static Logger logger = Logger.getLogger(AgentSearch.class.getName());

    @Getter
    public class SearchResults {
        public double bestReturn;
        public List<StepReturn> bestStepReturnSequence;
        public List<Integer> bestActionSequence;
        public int nofEpisodes;
        List<Integer> discreteActionsSpace;


        public SearchResults() {
            this.bestReturn=0;
            this.bestStepReturnSequence = new ArrayList<>();
            this.bestActionSequence = new ArrayList<>();
            nofEpisodes=0;
            this.discreteActionsSpace = new ArrayList<>();
        }

        public SearchResults(double bestReturn, List<Integer> discreteActionsSpace) {
            this();
            this.bestReturn = bestReturn;
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

    protected long timeBudget; //long <=> minimum value of 0
    protected long startTime;
    protected Environment env;
    protected EnvironmentParametersAbstract envParams;
    protected SearchResults searchResults;
    protected final Random random;
    protected CpuTimer timeBudgetChecker;


    public AgentSearch(long timeBudget, Environment env, EnvironmentParametersAbstract envParams) {
        this.timeBudget = timeBudget;
        this.startTime = System.currentTimeMillis();
        this.env = env;
        this.envParams = envParams;
        this.searchResults = new SearchResults(-Double.MAX_VALUE, envParams.discreteActionsSpace);
        this.random = new Random();
        this.timeBudgetChecker =new CpuTimer(timeBudget);  //time budget defined in sub class

    }

    public void setTimeBudgetMillis(long time) {
        this.timeBudget=time;
        timeBudgetChecker.setTimeBudgetMillis(time);
    }

    public abstract SearchResults search(final StateForSearch startState);

    protected double calcSumRewards(List<StepReturn> stepReturnSequence) {
        return stepReturnSequence.stream()
                .map(StepReturn::getReward)
                .reduce(0d, Double::sum);
    }

    public int chooseRandomAction(List<Integer> actions) {
        return actions.get(random.nextInt(actions.size()));
    }

    public boolean timeExceeded() {
        return timeBudgetChecker.isTimeExceeded();
    }

    protected boolean depthNotExceedAndFailStateNotEncountered(int depth, int searchDepth, StepReturn stepReturn) {
        return depth < searchDepth && !stepReturn.termState;
    }


    protected void logWarningIfNoFeasibleActionSequenceFound() {
        if (!searchResults.isResultOk()) {
            logger.warning("No feasible action sequence found");
        }
    }

    public SearchResults defineSearchResults(List<Integer> actionsOptimalPath, StateForSearch startState) {

        SearchResults searchResults = new SearchResults();
        if (actionsOptimalPath==null) {
            logger.warning("actionsOptimalPath is null");
        }
        else if (actionsOptimalPath.size()==0) {
            logger.warning("actionsOptimalPath is empty");
        }  else {

            StateForSearch state = new StateForSearch(startState);

            searchResults.bestActionSequence = actionsOptimalPath;
            double bestReturn = 0;
            for (Integer action : actionsOptimalPath) {
                StepReturn stepReturn = env.step(action, state);
                state.copyState(stepReturn.state);
                searchResults.bestStepReturnSequence.add(stepReturn);
                bestReturn = bestReturn + stepReturn.reward;

            }
            searchResults.nofEpisodes = 0;
            searchResults.bestReturn = bestReturn;
        }
        return searchResults;
    }



}
