package java_ai_gym.models_pong;


import java_ai_gym.models_common.NullState;
import java_ai_gym.models_common.State;
import java_ai_gym.models_common.StateForSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/***
 * This class uses method setNodeValues to set values in nodes in vsb. Using edge costs defined in vsb.
 * Method findNodesOnOptimalPath extract the optimal path
 * The constructor parameter strategy defines if min or max shall be applied.
 */

public class BellmanCalculator {
    private static final Logger logger = Logger.getLogger(BellmanCalculator.class.getName());

    VisitedStatesBuffer vsb;
    Strategy strategy;
    double discountFactor;
    int maxDepth;
    int minDepth;
    List<StateForSearch> nodesOnOptPath;

    public BellmanCalculator(VisitedStatesBuffer vsb, Strategy strategy, int searchDepth,  double discountFactor) {
        this.vsb = vsb;
        this.strategy=strategy;
        this.discountFactor = discountFactor;
        this.maxDepth = searchDepth;
        this.minDepth = 0;

        logger.info("BellmanCalculator initiated. maxDepth = "+maxDepth);
    }

    public List<StateForSearch> getNodesOnOptPath() {
        return nodesOnOptPath;
    }

    public void setNodeValues() {

        for (int depth = maxDepth - 1; depth >= minDepth; depth--) {
            List<StateForSearch> nodesAtDepth = vsb.getAllStatesAtDepth(depth);
            for (StateForSearch np : nodesAtDepth) {
                List<Double> costs = findCostCandidatesForNode(np);
                System.out.println("costs = "+costs);
                np.value=strategy.findBestInList(costs);
            }
        }

    }

    private List<Double> findCostCandidatesForNode(StateForSearch np) {

        List<Double> costList = new ArrayList<>();
        List<StateExperience> expList = vsb.getExperienceList(np.id);

        if (expList.size() == 0) {
            logger.warning("No experience (edges) for state id:" + np.id);
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
        double dfpd = calcDiscountFactorPowerDepth(np.depth);
        return edge.reward + dfpd * vsb.getStateVisitsDAO().get(edge.idNewState).value;
    }


    public List<StateForSearch> findNodesOnOptimalPath(StateForSearch startNode) {
        nodesOnOptPath = new ArrayList<>();
        addBestNodeAndFindNewBestNodeRecursive(startNode);
        return nodesOnOptPath;
    }

    public void addBestNodeAndFindNewBestNodeRecursive(State bestNode) {
        nodesOnOptPath.add((StateForSearch) bestNode);
        State newBestNode = findNewBestNode((StateForSearch) bestNode);
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

    private State findNewBestNode(StateForSearch bestNode) {
        State newBestNode = new NullState();
        double costBest = strategy.badNumber();
        List<StateExperience> expList = vsb.getExperienceList(bestNode.id);
        for (StateExperience edge : expList) {
            if (!vsb.getStateVisitsDAO().contains(edge.idNewState)) {
                logger.warning("For node " + bestNode.id + ", is the destination node not defined: " + edge.idNewState);
            } else {
                double cost = calcLongCost(bestNode, edge);
                if (strategy.isFirstBetterThanSecond(cost,costBest)) {
                    costBest = cost;
                    newBestNode = vsb.getStateVisitsDAO().get(edge.idNewState);
                }
            }
        }
        return newBestNode;
    }


}


