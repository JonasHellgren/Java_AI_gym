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
    final double EF_LIMIT=0.5;
    final double PROB_SELECT_STATE_FROM_NEW_DEPTH_STEP=0.95;

    int searchDepthStep;

    int searchDepthPrev;
    int searchDepth;
    List<Integer> evaluatedSearchDepths;
    VisitedStatesBuffer vsb;
    VisitedStatesBuffer trimmedVSB;
    VisitedStatesBuffer vsbForNewDepthSet;
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
        startTime = System.currentTimeMillis();  //starting time, long <=> minimum value of 0
        //this.state = new StateForSearch(env.getTemplateState());


    }


    @Override
    public SearchResults search(final StateForSearch startState) {

        //StateForSearch startState=(StateForSearch) startState0;

        setUpVsb(startState);
        trimmedVSB=vsb;
        vsbForNewDepthSet =new VisitedStatesBuffer();
        int nofActions = envParams.discreteActionsSpace.size();

        searchDepth = searchDepthStep;
        searchDepthPrev=0;
        int nofSteps = 0;
        double explorationFactor = 0;
        int nofStatesVsbSpecificPrev=1;

        startTime = System.currentTimeMillis();  //starting time, long <=> minimum value of 0

        //searchDepth=100;  //TODO remove
        while (!timeExceeded() && nofSteps<2000) {  //TODO remove nofSteps
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
            vsbForNewDepthSet.addNewStateAndExperienceFromStep(selectedState.id, action, stepReturn);

            }


          //  if (nofSteps % K ==0) {
            if ((double) vsbForNewDepthSet.size()/(double)nofStatesVsbSpecificPrev >K)  {
                System.out.println("statesPerDepth vsb = "+vsb.calcStatesPerDepth(searchDepth));
                System.out.println("statesPerDepth vsbForSpecificDepthStep= "+ vsbForNewDepthSet.calcStatesPerDepth(searchDepth));

                 nofStatesVsbSpecificPrev= vsbForNewDepthSet.size();
                startTimerForSpeedTest();
                explorationFactor= vsbForNewDepthSet.calcExplorationFactor(searchDepth);
                showElapsedTimeSpeedTest("calcExplorationFactor",true);
                logger.info("nofSteps = "+nofSteps+", explorationFactor = "+explorationFactor);
            }

            //if (explorationFactor>=EF_LIMIT) {

            startTimerForSpeedTest();
            vsb.getDepthMax();
            showElapsedTimeSpeedTest("getMaxDepth",false);

            if (vsb.getDepthMax()>=searchDepth && explorationFactor>=EF_LIMIT)  {
                increaseSearchDepth();
                vsbForNewDepthSet.clear();
                logger.fine("ExplorationFactor = "+ explorationFactor);
                explorationFactor =0;
                logger.info("searchDept increased to = "+searchDepth+". VSB size = "+vsb.size()+", nofSteps = "+ nofSteps);
                System.out.println("statesPerDepth vsb = "+vsb.calcStatesPerDepth(searchDepth));
                System.out.println("statesPerDepth vsbForSpecificDepthStep= "+ vsbForNewDepthSet.calcStatesPerDepth(searchDepth));
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
        System.out.println(methodName + " time (millis) = "+(System.nanoTime()- startTimeForSpeedTesting)/1000000);
    }

    private void performDynamicProgramming() {

        //todo, avoid passing in this, for timeExceeded access, bidirection dep
        trimmedVSB =  vsb.createNewVSBWithNoLooseNodesBelowDepth(searchDepthPrev,this);
        if (trimmedVSB.anyLooseNodeBelowDepth(trimmedVSB, searchDepthPrev)) {
        //    logger.warning("removeLooseNodesBelowDepth failed, still loose node(s).");
        }

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
      //  int i = 0;
        selectedState = vsb.selectRandomState();

        /*
        do {
            selectedState = vsb.selectRandomState();
            if (i > MAX_NOF_SELECTION_TRIES) {
                logger.warning("MAX_NOF_SELECTION_TRIES exceeded");
                logger.warning("isTerminal = "+vsb.isExperienceOfStateTerminal(selectedState.id)+", nofActionsTested = "+vsb.nofActionsTested(selectedState.id)+", depth = "+selectedState.depth);

               // System.out.println(vsbForSpecificDepthStep);
                //selectedState = this.startState;
                break;
            }
            i++;
        } while (isTerminalStateOrAllActionsTestedOrIsAtSearchDepth(selectedState));  */

        for (int j = 0; j < MAX_NOF_SELECTION_TRIES; j++) {

            if ( MathUtils.calcRandomFromIntervall(0,1)<PROB_SELECT_STATE_FROM_NEW_DEPTH_STEP && vsbForNewDepthSet.size()>0) {
             //   System.out.println(vsbForSpecificDepthStep);
                selectedState = vsbForNewDepthSet.selectRandomState();
              //  System.out.println(selectedState);
            } else
            {
                selectedState = vsb.selectRandomState();
            }

            if (!isNullOrTerminalStateOrAllActionsTestedOrIsAtSearchDepth(selectedState)) {
                break;
            }
        }

      return selectedState;
    }

    public boolean isNullOrTerminalStateOrAllActionsTestedOrIsAtSearchDepth(StateForSearch state) {

        if (state == null) {
            return true;
        }

        if (state.depth== searchDepth) {
            return true;
        }

        if (vsb.nofActionsTested(state.id) == state.nofActions) {
            return true;
        }

        if (vsb.isExperienceOfStateTerminal(state.id)) {
            return true;
        }

        return false;

        /*
        boolean isTerminal=vsb.isExperienceOfStateTerminal(state.id);
        int nofActionsTested = vsb.nofActionsTested(state.id);
        boolean isAtSearchDepth=(state.depth== searchDepth);
        return (isTerminal || (nofActionsTested == state.nofActions) || isAtSearchDepth); */
    }


}
