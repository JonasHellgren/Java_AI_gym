package java_ai_gym.swing;

public class Position2D {
    public double x;
    public double y;

    public Position2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Position2D{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

