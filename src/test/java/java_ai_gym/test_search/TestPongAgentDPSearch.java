package java_ai_gym.test_search;

import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.AgentSearch;
import java_ai_gym.models_common.StateForSearch;
import java_ai_gym.models_common.StepReturn;
import java_ai_gym.models_pong.HistogramDataSetGenerator;
import java_ai_gym.models_pong.VisitedStatesBuffer;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;


public class TestPongAgentDPSearch extends TestSearchBase {

    final int SLEEP_TIME=1000;

    @Before
    public void setup() {
        super.setupMoves();
        env.setRandomStateValuesStart(state);
        state.setIdDepthNofActions(state.START_STATE_ID, 0, 0);
    }


    @SneakyThrows
    @Test
    //@Ignore
    public void CreateVSBSize10() {

        final int NOF_STEPS = 10;
        final int MAX_DEPTH = 3; //agent.getVsb().getMaxDepth();
        createVSB(NOF_STEPS,MAX_DEPTH);

        System.out.println(agent.getVsb());
        env.leftTreePanel.createTreeFromVisitedStatesBuffer(agent.getVsb(),agent.getSearchDepthPrev());
        env.leftTreePanel.expandTree();
        Assert.assertEquals(NOF_STEPS + 1, agent.getVsb().nofStates());
        TimeUnit.MILLISECONDS.sleep(25000);

    }

    @SneakyThrows
    @Test
   // @Ignore
    public void testSearchBallAndRacketInMiddle() {
        setBallAndRacketInMiddleBallFallingDown();

        AgentSearch.SearchResults searchResults=agent.search(state);
        copyVSBsToFrame(agent.getVsb());
        printSummary(searchResults);


        Assert.assertEquals(1,searchResults.firstAction());
        Assert.assertFalse(agent.wasSearchFailing());

        TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
    }


    @SneakyThrows
    @Test
   // @Ignore
    public void testSearchBallMiddleAndRacketRight() {
        setBallInMiddleAndRacketInRightBallFallingDown();
        AgentSearch.SearchResults searchResults=agent.search(state);
        copyVSBsToFrame(agent.getVsb());
        printSummary(searchResults);

        for (StepReturn sr:searchResults.getBestStepReturnSequence()) {
            System.out.print(sr.reward+", ");
        }

        Assert.assertTrue(searchResults.bestActionSequence.subList(0,3).contains(moves.get("left")));
        Assert.assertFalse(agent.wasSearchFailing());


        TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
    }

    @SneakyThrows
    @Test
   // @Ignore
    public void testSearchBallLeftAndRacketRightHasNoSolution() {
        setBallLeftAndRacketRightHasNoSolution();
        AgentSearch.SearchResults searchResults=agent.search(state);
        copyVSBsToFrame(agent.getVsb());
        printSummary(searchResults);

        Assert.assertTrue(agent.wasSearchFailing());
        TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
        //System.out.println(searchResults.getBestStepReturnSequence());

    }


    @SneakyThrows
    @Test
    @Ignore("Takes time")
    public void testAnimate() {

        env.setRandomStateValuesStart(state);
        //setBallInMiddleAndRacketInRightBallFallingDown();

        StepReturn stepReturn;
        for (int i = 0; i <1000 ; i++) {
            System.out.println("i = "+i);
            AgentSearch.SearchResults sr=agent.search(state);
            stepReturn=env.step(sr.firstAction(),state);
           // copyVSBsToFrame(agent.getVsb());

            state.copyState(stepReturn.state);
            if (MathUtils.calcRandomFromIntervall(0, 1)<1.0) {
                env.render(state, sr.bestReturn, sr.firstAction());
            }

        }


        TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
    }


    private void copyVSBsToFrame(VisitedStatesBuffer vsb) {

        HistogramDataSetGenerator histogramDataSetGenerator =new HistogramDataSetGenerator();
        histogramDataSetGenerator.updateDatasetForDepthStatistics(env.leftChartPanel.getDataset(),vsb,agent.getEvaluatedSearchDepths());
        histogramDataSetGenerator.updateDatasetForStatesPerDepth(env.rightChartPanel.getDataset(),vsb);

    //    env.leftTreePanel.createTreeFromVisitedStatesBuffer(vsb,vsb.getDepthMax());
     //   env.leftTreePanel.expandTree();
      //  env.rightTreePanel.createTreeFromVisitedStatesBuffer(trimmedVSB,agent.getSearchDepthPrev());
     //   env.rightTreePanel.expandTree();

      //  env.createHistogramsFromVisitedStatesBuffer(vsb,agent.getEvaluatedSearchDepths());
      /// env.createHistogramFromVisitedStatesBufferFromStatesPerDepth(vsb,agent.getEvaluatedSearchDepths());
    }

    private void printVSBs(VisitedStatesBuffer vsb, VisitedStatesBuffer trimmedVSB) {
    //    System.out.println(vsb);
    //    System.out.println("original VSB depth = " + agent.getVsb().getDepthMax() + ", original VSB nof nodes = " + agent.getVsb().getStateVisitsDAO().size());
        System.out.println(trimmedVSB);
        System.out.println("trimmedVSB depth = " + trimmedVSB.getDepthMax() + ", trimmed VSB nof nodes = " + trimmedVSB.getStateVisitsDAO().size());
    }

    private void createVSB(int nofSteps, int maxDepth) {

        agent.getDpSearchServants().initInstanceVariables(state);
        agent.setSearchDepth(maxDepth);
        Assert.assertEquals(1, agent.getVsb().nofStates());
        System.out.println(agent.getVsb());
        int nofActions = p.discreteActionsSpace.size();

        for (int i = 0; i < nofSteps; i++) {
            StateForSearch selectedState = (StateForSearch) agent.getDpSearchStateSelector().selectState();
            int action = agent.chooseAction(selectedState,agent.getVsb());
            StepReturn stepReturn = env.step(action, selectedState);
            StateForSearch stateNew = (StateForSearch) stepReturn.state;
            stateNew.setDepthNofActions(selectedState.depth + 1, nofActions);
            agent.getVsb().addNewStateAndExperienceFromStep(selectedState.id, action, stepReturn);
        }

        agent.getDpSearchServants().addEvaluatedSearchDepth(maxDepth);

    }

    private void printSummary(AgentSearch.SearchResults searchResults) {
        System.out.println(searchResults.bestActionSequence+", depth = "+ searchResults.bestActionSequence.size()+", evaluatedSearchDepths = "+agent.getEvaluatedSearchDepths());

        System.out.println("bestReturn = "+ searchResults.bestReturn);

    }


}
