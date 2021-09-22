package java_ai_gym.models_common;

public class Experience {
    public State s;
    public int action;
    public StepReturn stepReturn;  //includes reward and sNew
    public PrioritizedExperienceReplay pExpRep=new PrioritizedExperienceReplay();
    private final int BE_ERROR_INIT=10;

    public class PrioritizedExperienceReplay {
        public double beError;
        public double priority;
        public double Psampling;
        public double sortCriteria;
        public double w;

        public PrioritizedExperienceReplay() {
            priority=0; Psampling=0; sortCriteria=0;   w=1;
        }

        @Override
        public String toString() {
            return "PrioritizedExperienceReplay{" +
                    "beError=" + beError +
                    ", priority=" + priority +
                    ", Psampling=" + Psampling +
                    ", sortCriteria=" + sortCriteria +
                    ", w=" + w +
                    '}'+ System.lineSeparator();
        }
    }

    public Experience(State s, int action, StepReturn stepReturn, double beErrorInit) {
        this.s = s;
        this.action = action;
        this.stepReturn = stepReturn;
        this.pExpRep.beError=beErrorInit;
    }

    public double getSortCriteria() {
        return pExpRep.sortCriteria;
    }

    @Override
    public String toString() {
        return "Experience{" +
                "s=" + s +
                ", action=" + action +
                ", reward=" + stepReturn.reward +
                ", termState=" + stepReturn.termState +
                ", sNew=" + stepReturn.state +
                '}' + System.lineSeparator();
    }
}
