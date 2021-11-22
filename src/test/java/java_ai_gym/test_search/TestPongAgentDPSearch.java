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
    public void ShowInit() {
        System.out.println(agent.getVsb());

    }

    @SneakyThrows
    @Test
    @Ignore
    public void CreateVSBSize10() {

        final int NOF_STEPS=10;
        createVSB(NOF_STEPS);

        System.out.println(agent.getVsb());
        env.upperPLotPanel.createTreeFromVisitedStatesBuffer(agent.getVsb());
        env.upperPLotPanel.expandTree();
        Assert.assertEquals(NOF_STEPS +1, agent.getVsb().nofStates());
        TimeUnit.MILLISECONDS.sleep(25000);

    }

    @SneakyThrows
    @Test
    public void CreateVSBSize10AndCutLooseNodes() {

        final int NOF_STEPS=10;
        createVSB(NOF_STEPS);

        System.out.println(agent.getVsb());
        env.upperPLotPanel.createTreeFromVisitedStatesBuffer(agent.getVsb());
        env.upperPLotPanel.expandTree();

        VisitedStatesBuffer trimmedVSB= agent.getVsb().removeLooseNodesBelowDepth(3);
        System.out.println(trimmedVSB);
        env.middlePLotPanel.createTreeFromVisitedStatesBuffer(trimmedVSB);
        env.middlePLotPanel.expandTree();

        Assert.assertTrue(trimmedVSB.nofStates() <= agent.getVsb().nofStates());

        TimeUnit.MILLISECONDS.sleep(25000);

    }

    private void createVSB(int NOF_STEPS) {
        agent.setUpVsb(state);
        Assert.assertEquals(1, agent.getVsb().nofStates());
        System.out.println(agent.getVsb());
        int nofActions = p.discreteActionsSpace.size();

        for (int i = 0; i < NOF_STEPS; i++) {
            StateForSearch selectedState = agent.selectState();
            int action = agent.chooseAction(selectedState);
            StepReturn stepReturn = env.step(action, selectedState);
            StateForSearch stateNew = (StateForSearch) stepReturn.state;
            stateNew.setDepthNofActions(selectedState.depth + 1, nofActions);
            agent.getVsb().addNewStateAndExperienceFromStep(selectedState.id, action, stepReturn);
        }

    }

}
