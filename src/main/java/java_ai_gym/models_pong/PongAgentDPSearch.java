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

    final int MAX_NOF_SELECTION_TRIES = 10;
    final int ACTION_DEFAULT = 1;
    final int K=100;
    final double EF_LIMIT=0.2;

    int searchDepthStep;

    int previousSearchDepth;
    int searchDepth;
    List<Integer> evaluatedSearchDepths;
    VisitedStatesBuffer vsb;
    VisitedStatesBuffer trimmedVSB;
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
        trimmedVSB=vsb;
        int nofActions = envParams.discreteActionsSpace.size();

        int nofSteps = 0;
        searchDepth = searchDepthStep;
        double explorationFactor = 0;

        long startTime = System.currentTimeMillis();  //starting time, long <=> minimum value of 0

        searchDepth=100;  //TODO remove
        while (timeNotExceeded(startTime) && nofSteps<2500000) {  //TODO remove nofSteps
            long startTime1 = System.nanoTime();  //starting time, long <=> minimum value of 0
            StateForSearch selectedState = this.selectState();
            System.out.println("selectState time = "+(System.nanoTime()-startTime1)/1000);


            int action = this.chooseAction(selectedState);
            startTime1 = System.nanoTime();  //starting time, long <=> minimum value of 0
            StepReturn stepReturn = env.step(action, selectedState);
            System.out.println("step time = "+(System.nanoTime()-startTime1)/1000);
            StateForSearch stateNew = (StateForSearch) stepReturn.state;

            stateNew.setDepthNofActions(selectedState.depth + 1, nofActions);

             startTime1 = System.nanoTime();  //starting time, long <=> minimum value of 0
           vsb.addNewStateAndExperienceFromStep(selectedState.id, action, stepReturn);
            System.out.println("add time = "+(System.nanoTime()-startTime1)/1000);

/*
            if (nofSteps % K ==0) {
                explorationFactor=vsb.calcExplorationFactor(searchDepth);
                logger.fine("explorationFactor = "+explorationFactor);
            }

            //if (explorationFactor>=EF_LIMIT) {
            if (vsb.getMaxDepth()>=searchDepth && explorationFactor>=EF_LIMIT)  {
                increaseSearchDepth();
                logger.info("ExplorationFactor = "+ explorationFactor);
                explorationFactor =0;
                logger.info("searchDept increased to = "+searchDepth+". VSB size = "+vsb.size()+", nofSteps = "+ nofSteps);
                performDynamicProgramming();
            }
             */
            nofSteps++;
        }

        logger.info("Search finished, nofSteps = "+nofSteps);

        return null;
    }

    private void performDynamicProgramming() {
        trimmedVSB =  vsb.createNewVSBWithNoLooseNodesBelowDepth(previousSearchDepth);
        logger.info(". VSB trimmed size = "+trimmedVSB.size());
    }

    private void increaseSearchDepth() {
        if (vsb.getMaxDepth() > searchDepth) {
             logger.warning("vsb.getMaxDepth() > searchDept");
         }
        addEvaluatedSearchDepth(searchDepth);
        previousSearchDepth=searchDepth;
        searchDepth=searchDepth+searchDepthStep;
    }

    public void addEvaluatedSearchDepth(int searchDepth) {
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
       // selectedState = vsb.selectRandomState();

        do {
            selectedState = vsb.selectRandomState();
            if (i > MAX_NOF_SELECTION_TRIES) {
                logger.warning("MAX_NOF_SELECTION_TRIES exceeded");
                selectedState = this.startState;
                break;
            }
            i++;
        } while (isTerminalStateOrAllActionsTestedOrIsAtSearchDepth(selectedState));

        /*   */
      return selectedState;
    }

    public boolean isTerminalStateOrAllActionsTestedOrIsAtSearchDepth(StateForSearch state) {
       // StateExperience exp = vsb.searchExperienceOfSteppingToState(state.id);
      //  int nofActionsTested = vsb.nofActionsTested(state.id);
        boolean isAtSearchDepth=(state.depth== searchDepth);
     //   return (exp.termState || (nofActionsTested == state.nofActions) || isAtSearchDepth);
       return (false || false || isAtSearchDepth);
    }


}
