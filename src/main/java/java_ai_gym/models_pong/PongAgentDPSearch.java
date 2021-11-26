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

    final int MAX_NOF_SELECTION_TRIES = 1000;
    final int ACTION_DEFAULT = 1;
    double K=2.0;
    final double EF_LIMIT=0.1;

    int searchDepthStep;

    int searchDepthPrev;
    int searchDepth;
    List<Integer> evaluatedSearchDepths;
    VisitedStatesBuffer vsb;
    VisitedStatesBuffer trimmedVSB;
    VisitedStatesBuffer vsbForSpecificDepthStep;
    //StateForSearch state;
    StateForSearch startState;

    long startTimeForSpeedTesting;

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
        vsbForSpecificDepthStep=new VisitedStatesBuffer();
        int nofActions = envParams.discreteActionsSpace.size();

        searchDepth = searchDepthStep;
        searchDepthPrev=0;
        int nofSteps = 0;
        double explorationFactor = 0;
        int nofStatesVsbSpecificPrev=1;

        long startTime = System.currentTimeMillis();  //starting time, long <=> minimum value of 0

        //searchDepth=100;  //TODO remove
        while (timeNotExceeded(startTime) && nofSteps<200000) {  //TODO remove nofSteps
            startTimerForSpeedTest();
            StateForSearch selectedState = this.selectState();
            showElapsedTimeSpeedTest("selectState",false);


            int action = this.chooseAction(selectedState);
            startTimerForSpeedTest();
            StepReturn stepReturn = env.step(action, selectedState);
            showElapsedTimeSpeedTest("step",false);
            StateForSearch stateNew = (StateForSearch) stepReturn.state;
            stateNew.setDepthNofActions(selectedState.depth + 1, nofActions);

            startTimerForSpeedTest();
            vsb.addNewStateAndExperienceFromStep(selectedState.id, action, stepReturn);
            showElapsedTimeSpeedTest("addNewStateAndExperienceFromStep",false);

            if (stateNew.depth>=searchDepthPrev) {
            vsbForSpecificDepthStep.addNewStateAndExperienceFromStep(selectedState.id, action, stepReturn);

            }


          //  if (nofSteps % K ==0) {
            if ((double)vsbForSpecificDepthStep.size()/(double)nofStatesVsbSpecificPrev >K)  {
                System.out.println("statesPerDepth vsb = "+vsb.calcStatesPerDepth(searchDepth));
                System.out.println("statesPerDepth vsbForSpecificDepthStep= "+vsbForSpecificDepthStep.calcStatesPerDepth(searchDepth));

                 nofStatesVsbSpecificPrev=vsbForSpecificDepthStep.size();
                startTimerForSpeedTest();
                explorationFactor=vsbForSpecificDepthStep.calcExplorationFactor(searchDepth);
                showElapsedTimeSpeedTest("calcExplorationFactor",true);
                logger.info("nofSteps = "+nofSteps+", explorationFactor = "+explorationFactor);
            }

            //if (explorationFactor>=EF_LIMIT) {

            startTimerForSpeedTest();
            vsb.getDepthMax();
            showElapsedTimeSpeedTest("getMaxDepth",false);

            if (vsb.getDepthMax()>=searchDepth && explorationFactor>=EF_LIMIT)  {
                increaseSearchDepth();
                vsbForSpecificDepthStep.clear();
                logger.fine("ExplorationFactor = "+ explorationFactor);
                explorationFactor =0;
                logger.info("searchDept increased to = "+searchDepth+". VSB size = "+vsb.size()+", nofSteps = "+ nofSteps);
                System.out.println("statesPerDepth vsb = "+vsb.calcStatesPerDepth(searchDepth));
                System.out.println("statesPerDepth vsbForSpecificDepthStep= "+vsbForSpecificDepthStep.calcStatesPerDepth(searchDepth));
                System.out.println("searchDepthPrev = "+searchDepthPrev);

                startTimerForSpeedTest();
                performDynamicProgramming();
                showElapsedTimeSpeedTest("performDynamicProgramming",true);
            }

            nofSteps++;
        }

        logger.info("Search finished, nofSteps = "+nofSteps);

        return null;
    }



    private void startTimerForSpeedTest() {
        startTimeForSpeedTesting = System.nanoTime();  //starting time, long <=> minimum value of 0
    }

    private void showElapsedTimeSpeedTest(String methodName, boolean flag) {
        if (flag)
        System.out.println(methodName + " time = "+(System.nanoTime()- startTimeForSpeedTesting)/1000);
    }

    private void performDynamicProgramming() {

        trimmedVSB =  vsb.createNewVSBWithNoLooseNodesBelowDepth(searchDepthPrev);
        logger.info(". VSB trimmed size = "+trimmedVSB.size());
    }

    private void increaseSearchDepth() {
        if (vsb.getDepthMax() > searchDepth) {
             logger.warning("vsb.getMaxDepth() > searchDept");
         }
        addEvaluatedSearchDepth(searchDepth);
        searchDepthPrev =searchDepth;
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
        //selectedState = vsb.selectRandomState();

        do {
            selectedState = vsb.selectRandomState();
            if (i > MAX_NOF_SELECTION_TRIES) {
                logger.warning("MAX_NOF_SELECTION_TRIES exceeded");
                logger.warning("isTerminal = "+vsb.isExperienceOfStateTerminal(selectedState.id)+", nofActionsTested = "+vsb.nofActionsTested(selectedState.id)+", depth = "+selectedState.depth);

               // System.out.println(vsbForSpecificDepthStep);
                selectedState = this.startState;
                break;
            }
            i++;
        } while (isTerminalStateOrAllActionsTestedOrIsAtSearchDepth(selectedState));

      return selectedState;
    }

    public boolean isTerminalStateOrAllActionsTestedOrIsAtSearchDepth(StateForSearch state) {

   //     long startTime1 = System.nanoTime();  //starting time, long <=> minimum value of 0
   //     StateExperience exp = vsb.searchExperienceOfSteppingToState(state.id);
        boolean isTerminal=vsb.isExperienceOfStateTerminal(state.id);
   //     System.out.println("searchExperienceOfSteppingToState time = "+(System.nanoTime()-startTime1)/1000);
        int nofActionsTested = vsb.nofActionsTested(state.id);
        boolean isAtSearchDepth=(state.depth== searchDepth);
      //  return (exp.termState || (nofActionsTested == state.nofActions) || isAtSearchDepth);
        return (isTerminal || (nofActionsTested == state.nofActions) || isAtSearchDepth);
    }


}
