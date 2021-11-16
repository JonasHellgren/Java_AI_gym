package java_ai_gym.test_search;
import java_ai_gym.models_common.StepReturn;
import java_ai_gym.models_pong.PongAgentRandomSearch;
import lombok.SneakyThrows;
import org.jcodec.common.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestPongAgentRandomSearch extends TestSearchBase {

    final long TIME_BUDGET=500;
    final int SEARCH_DEPTH=10;

    @Before
    public void setup() {
        super.setupMoves();

        state.setVariable("xPosBall", p.MAX_X_POSITION/2);
        state.setVariable("yPosBall",  p.SPEED_BALL*SEARCH_DEPTH*0.75);
        state.setVariable("xSpdBall", 0.0);
        state.setVariable("ySpdBall", -p.SPEED_BALL);
        state.setVariable("xPosRacket", p.MAX_X_POSITION/2);
        state.setVariable("xSpdRacket", 0.0);
        state.setVariable("nofSteps", 0);

    }

    @SneakyThrows
    @Test
    public void RacketInMiddleBallDownShallGiveNoMove() {

        PongAgentRandomSearch agent=new PongAgentRandomSearch(TIME_BUDGET,SEARCH_DEPTH,env,env.parameters);
        PongAgentRandomSearch.SearchResults sr=agent.search(state);

        System.out.println("nofEpisodes:"+sr.nofEpisodes);
        System.out.println(sr.bestStepReturnSequence);
        System.out.println("firstAction:"+sr.firstAction());
        Assert.assertEquals(moves.get("still"),sr.firstAction());
    }


    @SneakyThrows
    @Test
    public void RacketAtRightBallDownShallGiveLeftMove() {

        state.setVariable("xPosRacket", p.MAX_X_POSITION);

        PongAgentRandomSearch agent=new PongAgentRandomSearch(TIME_BUDGET,SEARCH_DEPTH,env,env.parameters);
        PongAgentRandomSearch.SearchResults sr=agent.search(state);

        System.out.println("nofEpisodes:"+sr.nofEpisodes);
        System.out.println(sr.bestStepReturnSequence);
        System.out.println("firstAction:"+sr.firstAction());
        Assert.assertEquals(moves.get("left"),sr.firstAction());
    }


    @SneakyThrows
    @Test
    //@Ignore("Takes time")
    public void testAnimate() {

        env.setRandomStateValuesStart(state);

        StepReturn stepReturn;
        for (int i = 0; i <10000 ; i++) {

            PongAgentRandomSearch agent=new PongAgentRandomSearch(50,10,env,env.parameters);
            PongAgentRandomSearch.SearchResults sr=agent.search(state);
            stepReturn=env.step(sr.firstAction(),state);

            state.copyState(stepReturn.state);
            System.out.println(stepReturn);
            env.render(state,0.0,0);
            //TimeUnit.MILLISECONDS.sleep(10);
        }

    }

}
