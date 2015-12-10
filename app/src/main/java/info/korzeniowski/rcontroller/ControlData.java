package info.korzeniowski.rcontroller;

public class ControlData {
    private int servoFix;
    private SteeringWheel steeringWheel;
    private Engine engine;

    public ControlData(Engine engine, SteeringWheel steeringWheel, int servoFix) {
        this.engine = engine;
        this.steeringWheel = steeringWheel;
        this.servoFix = servoFix;
    }

    public Direction getDirection() {
        return engine.direction;
    }

    public int getSpeed() {
        return engine.speed;
    }

    public Side getSide() {
        return steeringWheel.side;
    }

    public int getAngle() {
        return steeringWheel.angle;
    }

    public int getServoFix() {
        return servoFix;
    }

    @Override
    public String toString() {
        return getDirection() + " " + getSpeed() + " | " + getSide() + " " + getAngle() + " " + servoFix;
    }

    public enum Side {
        RIGHT(1), LEFT(2);

        private int value;

        Side(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Direction {
        FORWARD(1), BACKWARD(2);

        private int value;

        Direction(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static class Engine {
        public final Direction direction;
        public final int speed;

        public Engine(Direction direction, int speed) {
            this.direction = direction;
            this.speed = speed;
        }
    }

    public static class SteeringWheel {
        public final Side side;
        public final int angle;

        public SteeringWheel(Side side, int angle) {
            this.side = side;
            this.angle = angle;
        }
    }
}
