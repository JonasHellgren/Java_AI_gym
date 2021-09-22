package java_ai_gym.models_common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/***
 * Experience replay is described in https://arxiv.org/pdf/1511.05952.pdf
 */

public class ReplayBuffer {
    public final List<Experience> buffer = new ArrayList<>();

    public static final int BELLMAN_ERROR_MAX=10;
    public  double RB_EPS =1.0;
    public double RB_ALP =0.1;  //0 <=> uniform distribution from bellman error for mini batch selection
    public double BETA0 =0.5;
    public int REPLAY_BUFFER_SIZE = 100;

    public ReplayBuffer(double RB_EPS, double RB_ALP, double BETA0,int REPLAY_BUFFER_SIZE) {
        this.RB_EPS = RB_EPS;
        this.RB_ALP = RB_ALP;
        this.BETA0 = BETA0;
        this.REPLAY_BUFFER_SIZE = REPLAY_BUFFER_SIZE;
    }

    public void addExperience(Experience experience) {
        if (buffer.size() >= REPLAY_BUFFER_SIZE)   //remove first/oldest item in set if set is "full"
            buffer.remove(0);
        buffer.add(experience);
    }

    public int size() {
        return buffer.size();
    }

    public void clear() {
        buffer.clear();
    }

    public List<Experience> getBuffer() {
        return buffer;
    }

    public List<Experience> getMiniBatch(int batchLength) {
        List<Experience> miniBatch = new ArrayList<>();

        List<Integer> indexes = IntStream.rangeClosed(0, buffer.size() - 1)
                .boxed().collect(Collectors.toList());
        Collections.shuffle(indexes);

        for (int i = 0; i < Math.min(batchLength,indexes.size()); i++)
            miniBatch.add(buffer.get(indexes.get(i)));

        return miniBatch;
    }

    public List<Experience> getMiniBatchPrioritizedExperienceReplay(int batchLength, double fEpisodes) {

        calcPrioInReplayBufferFromBellmanError();
        calcSortCriteriaInReplayBuffer();
           List<Experience> miniBatch=extractMiniBatchFromBufferAccordingToSortCriteria( batchLength);
        double beta= BETA0 *(1-fEpisodes)+1*fEpisodes;  //linearly anneal β from its initial value β0 to 1
        calcImportanceSamplingWeights(miniBatch, beta);
        return extractMiniBatchFromBufferAccordingToSortCriteria( batchLength);
    }

    public boolean isFull(AgentNeuralNetwork agent) {
        return size()==agent.REPLAY_BUFFER_SIZE;

    }

    private void calcPrioInReplayBufferFromBellmanError() {
        for (Experience exper : buffer)
            exper.pExpRep.priority = Math.min(Math.abs(exper.pExpRep.beError), BELLMAN_ERROR_MAX) + RB_EPS;
    }

    private void calcSortCriteriaInReplayBuffer() {
        double sumOfPrios = 0;
        for (Experience exper : buffer)
            sumOfPrios = sumOfPrios + Math.pow(exper.pExpRep.priority, RB_ALP);

        for (Experience exper : buffer) {
            exper.pExpRep.Psampling = Math.pow(exper.pExpRep.priority, RB_ALP) / sumOfPrios;
            exper.pExpRep.sortCriteria = exper.pExpRep.Psampling * Math.random();
        }
    }

    private List<Experience> extractMiniBatchFromBufferAccordingToSortCriteria(int batchLength) {
        List<Experience> sortedRepBuff = buffer.stream()
                .sorted(Comparator.comparing(Experience::getSortCriteria).reversed())
                .collect(Collectors.toList());

        return sortedRepBuff.stream()
                .limit(batchLength)
                .collect(Collectors.toList());
    }

    private void calcImportanceSamplingWeights(List<Experience> miniBatch, double beta)  {
        List<Double> weights=new ArrayList<>();
        for (Experience exper : miniBatch) {
            exper.pExpRep.w = Math.pow(1 / (buffer.size() * exper.pExpRep.Psampling), beta);
            weights.add(exper.pExpRep.w );
            //System.out.println("Psampling:"+exper.pExpRep.Psampling+", w:"+ exper.pExpRep.w);
        }

        normalizeWeights(miniBatch, weights);
    }

    private void normalizeWeights(List<Experience> miniBatch, List<Double> weights) {
        double wMax= weights.stream().mapToDouble(Double::doubleValue).max().getAsDouble();
        for (Experience exper : miniBatch) {
            exper.pExpRep.w = exper.pExpRep.w / wMax;
        }
    }


    @Override
    public String toString() {
        return bufferAsString(buffer);
    }

    public String bufferAsString(List<Experience> buffer) {
        return "ReplayBuffer{" + System.lineSeparator() +
                buffer +
                '}';
    }

    public String pExpRepInfoAsString(List<Experience> buffer) {

        List<String> text=new ArrayList<>();
        for (Experience exper : buffer) {
            text.add(exper.pExpRep.toString());
        }

        return "PrioritizedExperienceReplay{" + System.lineSeparator() +
                text +
                '}';
    }

}