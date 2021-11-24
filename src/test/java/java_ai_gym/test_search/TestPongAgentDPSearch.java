package java_ai_gym.test_search;

import java_ai_gym.models_common.StateForSearch;
import java_ai_gym.models_common.StepReturn;
import java_ai_gym.models_pong.VisitedStatesBuffer;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TestPongAgentDPSearch extends TestSearchBase {

    @Before
    public void setup() {
        super.setupMoves();
        p.MAX_SPEED_RACKET = .01;
        env.setRandomStateValuesStart(state);
        state.setIdDepthNofActions(state.START_STATE_ID, 0, 0);
    }


    @Test
    @Ignore
    public void ShowInit() {
        System.out.println(agent.getVsb());

    }

    @SneakyThrows
    @Test
    @Ignore
    public void CreateVSBSize10() {

        final int NOF_STEPS = 10;
        final int MAX_DEPTH = 3; //agent.getVsb().getMaxDepth();
        createVSB(NOF_STEPS,MAX_DEPTH);

        System.out.println(agent.getVsb());
        env.upperTreePanel.createTreeFromVisitedStatesBuffer(agent.getVsb());
        env.upperTreePanel.expandTree();
        Assert.assertEquals(NOF_STEPS + 1, agent.getVsb().nofStates());
        TimeUnit.MILLISECONDS.sleep(25000);

    }

    @SneakyThrows
    @Test
    public void CreateSingleVSBSize10AndCutLooseNodes() {
        final int NOF_STEPS = 100;
        final int MAX_DEPTH = 8; //agent.getVsb().getMaxDepth();

        createVSB(NOF_STEPS,MAX_DEPTH);
        logger.info("VSB size = " + agent.getVsb().size());
        VisitedStatesBuffer trimmedVSB = agent.getVsb().removeLooseNodesBelowDepth(MAX_DEPTH);   //agent.getVsb().getMaxDepth()
       // printVSBs(agent.getVsb(), trimmedVSB);
        copyVSBsToFrame(agent.getVsb(), trimmedVSB);
        TimeUnit.MILLISECONDS.sleep(15000);

        Assert.assertTrue(trimmedVSB.nofStates() <= agent.getVsb().nofStates());
        Assert.assertFalse(trimmedVSB.anyLooseNodeBelowDepth(trimmedVSB, MAX_DEPTH));

        agent.getVsb().clear();
    }

    @SneakyThrows
    @Test
    @Ignore
    public void CreateManyVSBSize100AndCutLooseNodes() {
        final int NOF_STEPS = 100;
        final int MAX_DEPTH = 5; //agent.getVsb().getMaxDepth();

        for (int i = 0; i < 10; i++) {
            createVSB(NOF_STEPS,MAX_DEPTH);
            logger.info("VSB size = " + agent.getVsb().size());

            VisitedStatesBuffer trimmedVSB = agent.getVsb().removeLooseNodesBelowDepth(MAX_DEPTH);   //agent.getVsb().getMaxDepth()
            printVSBs(agent.getVsb(), trimmedVSB);

            if (trimmedVSB.anyLooseNodeBelowDepth(trimmedVSB, MAX_DEPTH)) {
                TimeUnit.MILLISECONDS.sleep(15000);
                copyVSBsToFrame(agent.getVsb(), trimmedVSB);
            }
            Assert.assertTrue(trimmedVSB.nofStates() <= agent.getVsb().nofStates());
            Assert.assertFalse(trimmedVSB.anyLooseNodeBelowDepth(trimmedVSB, MAX_DEPTH));

            agent.getVsb().clear();
        }

        //TimeUnit.MILLISECONDS.sleep(10000);

    }

    private void copyVSBsToFrame(VisitedStatesBuffer vsb, VisitedStatesBuffer trimmedVSB) {
        env.upperTreePanel.createTreeFromVisitedStatesBuffer(vsb);
        env.upperTreePanel.expandTree();
        env.lowerTreePanel.createTreeFromVisitedStatesBuffer(trimmedVSB);
        env.lowerTreePanel.expandTree();
        env.createHistogramFromVisitedStatesBuffer(vsb,agent.getEvaluatedSearchDepths());
        env.createHistogramFromVisitedStatesBufferFromStatesPerDepth(vsb,agent.getEvaluatedSearchDepths());
    }

    private void printVSBs(VisitedStatesBuffer vsb, VisitedStatesBuffer trimmedVSB) {
        System.out.println(vsb);
        System.out.println("original VSB depth = " + agent.getVsb().getMaxDepth() + ", original VSB nof nodes = " + agent.getVsb().getStateVisitsDAO().size());
        System.out.println(trimmedVSB);
        System.out.println("trimmedVSB depth = " + trimmedVSB.getMaxDepth() + ", trimmed VSB nof nodes = " + trimmedVSB.getStateVisitsDAO().size());
    }

    private void createVSB(int nofSteps, int maxDepth) {

        agent.setUpVsb(state);
        agent.setSearchDepth(maxDepth);
        Assert.assertEquals(1, agent.getVsb().nofStates());
        System.out.println(agent.getVsb());
        int nofActions = p.discreteActionsSpace.size();

        for (int i = 0; i < nofSteps; i++) {
            StateForSearch selectedState = agent.selectState();
            int action = agent.chooseAction(selectedState);
            StepReturn stepReturn = env.step(action, selectedState);
            StateForSearch stateNew = (StateForSearch) stepReturn.state;
            stateNew.setDepthNofActions(selectedState.depth + 1, nofActions);
            agent.getVsb().addNewStateAndExperienceFromStep(selectedState.id, action, stepReturn);
        }

        agent.addSearchDepth(maxDepth);

    }

}
