package java_ai_gym.test_search;
import java_ai_gym.models_common.AgentSearch;
import java_ai_gym.models_common.StepReturn;
import java_ai_gym.models_pong.PongAgentRandomSearch;
import lombok.SneakyThrows;
import org.jcodec.common.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestPongAgentRandomSearch extends TestSearchBase {

    final long TIME_BUDGET=50;
    int SEARCH_DEPTH;

    @Before
    public void setup() {
        super.setupMoves();

        p.MAX_SPEED_RACKET=.1;
        SEARCH_DEPTH= (int) (p.MAX_X_POSITION/p.MAX_SPEED_RACKET);

        logger.info("Search depth = "+SEARCH_DEPTH);


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

        AgentSearch agent=new PongAgentRandomSearch(TIME_BUDGET,SEARCH_DEPTH,env,env.parameters);
        AgentSearch.SearchResults sr=agent.search(state);

        //somePrints(sr);
        Assert.assertEquals(moves.get("still"),sr.firstAction());
    }


    @SneakyThrows
    @Test
    public void RacketAtRightBallDownShallGiveLeftMove() {

        state.setVariable("xPosRacket", p.MAX_X_POSITION);

        AgentSearch agent=new PongAgentRandomSearch(TIME_BUDGET,SEARCH_DEPTH,env,env.parameters);
        AgentSearch.SearchResults sr=agent.search(state);

        //somePrints(sr);
        Assert.assertEquals(moves.get("left"),sr.firstAction());
    }

    private void somePrints(PongAgentRandomSearch.SearchResults sr) {
        System.out.println("nofEpisodes:"+ sr.nofEpisodes);
        System.out.println(sr.bestStepReturnSequence);
        System.out.println("firstAction:"+ sr.firstAction());
    }


    @SneakyThrows
    @Test
    //@Ignore("Takes time")
    public void testAnimate() {

        env.setRandomStateValuesStart(state);

        StepReturn stepReturn;
        for (int i = 0; i <10000 ; i++) {

            AgentSearch agent=new PongAgentRandomSearch(TIME_BUDGET,SEARCH_DEPTH,env,env.parameters);
            AgentSearch.SearchResults sr=agent.search(state);
            stepReturn=env.step(sr.firstAction(),state);

            state.copyState(stepReturn.state);
            env.render(state,sr.bestReturn,sr.firstAction());

        }

    }

}
