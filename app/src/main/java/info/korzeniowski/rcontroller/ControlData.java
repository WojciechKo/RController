package info.korzeniowski.rcontroller;

public class ControlData {
    private Direction direction;
    private int speed;
    private Side side;
    private int angle;

    public ControlData(Direction direction, int speed, Side side, int angle) {
        this.direction = direction;
        this.speed = speed;
        this.side = side;
        this.angle = angle;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getSpeed() {
        return speed;
    }

    public Side getSide() {
        return side;
    }

    public int getAngle() {
        return angle;
    }

    @Override
    public String toString() {
        return direction + " " + speed + " | " + side + " " + angle;
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
}
