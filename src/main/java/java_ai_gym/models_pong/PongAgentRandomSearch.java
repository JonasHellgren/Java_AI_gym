package java_ai_gym.models_pong;

import java_ai_gym.models_common.Environment;
import java_ai_gym.models_common.EnvironmentParametersAbstract;
import java_ai_gym.models_common.State;
import java_ai_gym.models_common.StepReturn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PongAgentRandomSearch {

    public class SearchResults {
        double bestReturn;
        List<Integer> bestActionSequence;

        public SearchResults(double bestReturn, List<Integer> bestActionSequence) {
            this.bestReturn = bestReturn;
            this.bestActionSequence = bestActionSequence;
        }
    }

    long timeBudget;
    int searchDepth;
    Environment env;
    EnvironmentParametersAbstract envParams;

    SearchResults searchResults;
    private final Random random = new Random();

    public PongAgentRandomSearch(long timeBudget, int searchDepth, Environment env, EnvironmentParametersAbstract envParams ) {
        this.timeBudget = timeBudget;
        this.searchDepth = searchDepth;
        this.env = env;
        this.envParams = envParams;

        searchResults=new SearchResults(-Double.MAX_VALUE,new ArrayList<>( ));
    }


    public SearchResults search(final State startState) {

        long startTime = System.currentTimeMillis();  //starting time, long <=> minimum value of 0

        List<Double> rewardSequence=new ArrayList<>();
        List<Integer> actionSequence=new ArrayList<>();
        State state=new State(startState);
        StepReturn stepReturn= new StepReturn();

    //    while (System.currentTimeMillis() < startTime+timeBudget)  {

            rewardSequence.clear();
            actionSequence.clear();
            state.copyState(startState);


            int depth=0;

            while (depth<searchDepth && !stepReturn.termState) {

                int action=chooseRandomAction(envParams.discreteActionsSpace);
                stepReturn=env.step(action,state);

                rewardSequence.add(stepReturn.reward);
                actionSequence.add(action);

                System.out.println(state);

                state.copyState(stepReturn.state);
                depth++;
            }


        System.out.println(actionSequence);

        System.out.println(rewardSequence);

     //   }


        return searchResults;


    }

    public int chooseRandomAction(List<Integer> actions) {
        return actions.get(random.nextInt(actions.size()));
    }

}

