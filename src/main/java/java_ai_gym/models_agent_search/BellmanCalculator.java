package java_ai_gym.models_agent_search;


import java_ai_gym.helpers.CpuTimer;
import java_ai_gym.models_common.NullState;
import java_ai_gym.models_common.State;
import java_ai_gym.models_common.StateForSearch;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/***
 * This class uses method setNodeValues to set values in nodes in vsb. Using edge costs defined in vsb.
 * Method findNodesOnOptimalPath extract the optimal path
 * The constructor parameter strategy defines if min or max shall be applied.
 */

@Setter
public class BellmanCalculator {
    private static final Logger logger = Logger.getLogger(BellmanCalculator.class.getName());

    VisitedStatesBuffer vsb;
    Strategy strategy;
    double discountFactor;
    int maxDepth;
    int minDepth;
    List<StateForSearch> nodesOnOptPath;
    List<Integer> actionsOptPath;
    CpuTimer timeChecker;
    boolean timeExceed;

    public BellmanCalculator(VisitedStatesBuffer vsb, Strategy strategy,  double discountFactor,CpuTimer timeBudgetChecker) {
        this.vsb = vsb;
        this.strategy=strategy;
        this.discountFactor = discountFactor;
        this.maxDepth = 0;  //defined then setNodeValues is called
        this.minDepth = 0;
        nodesOnOptPath = new ArrayList<>();
        actionsOptPath = new ArrayList<>();
        this.timeChecker=timeBudgetChecker;
        this.timeExceed=false;
        logger.fine("BellmanCalculator initiated. maxDepth = "+maxDepth);
    }

    public List<StateForSearch> getNodesOnOptPath() {
        return nodesOnOptPath;
    }

    public List<Integer> getActionsOptPath() {
        if (actionsOptPath==null) {
            logger.warning("actionsOptPath not defined");
            return new ArrayList<>();
        }
        return actionsOptPath;
    }

    public void setNodeValues(int searchDepth) {
        this.timeExceed=false;
        this.maxDepth = searchDepth;
        for (int depth = maxDepth - 1; depth >= minDepth; depth--) {

            if (timeChecker.isTimeExceeded()) {
                logger.warning("Time exceeded in setNodeValues !!!!!");
                this.timeExceed=true;
                break;
            }

            List<StateForSearch> nodesAtDepth = vsb.getAllStatesAtDepth(depth);
            for (StateForSearch np : nodesAtDepth) {
                List<Double> costs = findCostCandidatesForNode(np);
                np.value=(costs.size()==0)?strategy.badNumber():strategy.findBestInList(costs);
            }
        }

    }

    public boolean isTimeExceeded() {
        return timeExceed;
    }

    private List<Double> findCostCandidatesForNode(StateForSearch np) {

        List<Double> costList = new ArrayList<>();
        List<StateExperience> expList = vsb.getExperienceList(np.id);

        if (expList.size() == 0) {
            logger.fine("No experience (edges) for state id:" + np.id);
            return costList;
        }


        for (StateExperience edge : expList) {
            if (!vsb.getStateVisitsDAO().contains(edge.idNewState)) {
                logger.warning("For node " + np.id + ", is the destination node not defined: " + edge.idNewState);
            } else {
                double cost = calcLongCost(np, edge);
                costList.add(cost);
           }
        }
        return costList;
    }



    public double calcDiscountFactorPowerDepth(int depth) {
        return Math.pow(discountFactor, (depth - minDepth) + 1);
    }


    private double calcLongCost(StateForSearch np, StateExperience edge) {
        double discFactor = calcDiscountFactorPowerDepth(np.depth);
        return edge.reward + discFactor * vsb.getStateVisitsDAO().get(edge.idNewState).value;
    }


    public List<StateForSearch> findNodesOnOptimalPath(StateForSearch startNode) {
        nodesOnOptPath.clear();
        actionsOptPath.clear();
        addBestNodeAndFindNewBestNodeRecursive(startNode);
        return nodesOnOptPath;
    }

    public void addBestNodeAndFindNewBestNodeRecursive(State bestNode) {
        nodesOnOptPath.add((StateForSearch) bestNode);
        State newBestNode = findNewBestNodeAndAddActionBest((StateForSearch) bestNode);
        showLogIfBestNodeHasNoDestination((StateForSearch) bestNode, newBestNode);
        if (! (newBestNode instanceof NullState)) {
            addBestNodeAndFindNewBestNodeRecursive(newBestNode);
        }
    }

    private void showLogIfBestNodeHasNoDestination(StateForSearch bestNode, State newBestNode) {
        if (newBestNode instanceof NullState && bestNode.depth < this.maxDepth) {
            logger.warning("No destination node for node:" + bestNode.id);
        }
    }

    private State findNewBestNodeAndAddActionBest(StateForSearch bestNode) {
        State newBestNode = new NullState();
        double costBest = strategy.badNumber();
        String idBest="";
        int actionBest=0;
        List<StateExperience> expList = vsb.getExperienceList(bestNode.id);
        for (StateExperience edge : expList) {
            if (!vsb.getStateVisitsDAO().contains(edge.idNewState)) {
                logger.warning("For node " + bestNode.id + ", is the destination node not defined: " + edge.idNewState);
            } else {
                double cost = calcLongCost(bestNode, edge);
                if (strategy.isFirstBetterThanSecond(cost,costBest)) {
                    costBest = cost;
                    actionBest=edge.action;
                    idBest= edge.idNewState;
                    newBestNode = vsb.getStateVisitsDAO().get(edge.idNewState);
                }
            }
        }
        if (!hasNoExperienceOrTerminal(idBest, expList)) {
            actionsOptPath.add(actionBest);
        }
        return newBestNode;
    }

    private boolean hasNoExperienceOrTerminal(String idBest, List<StateExperience> expList) {
        return expList.size() == 0 || vsb.setOfTerminalStatesDAO.isTerminal(idBest);
    }
}


