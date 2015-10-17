package info.korzeniowski.rcontroller;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

public class Backup {
//
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        GyroscopeValues values = new GyroscopeValues(event.values);
//
//        if (initialRotationVectors == null) {
//            initialRotationVectors = values;
//        }
//
//        float deltaAxisZ = values.axisZ - initialRotationVectors.axisZ;
//        int pwmFillness = convertToPwm(deltaAxisZ);
//    }
//
//    private int convertToPwm(float delta) {
//        double rage = 0.9;
//        double a = 900;
//
//        if (delta >= -1 && delta < -rage) {
//            return (int) (delta * a + 990);
//        } else if (delta > -rage && delta < 0) {
//            return 180;
//        } else if (delta > 0 && delta < rage) {
//            return 0;
//        } else if (delta >= rage && delta < 1) {
//            return (int) (delta * a - 810);
//        }
//        return 90;
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//    }
//
//    class GyroscopeValues {
//        private float axisX;
//        private float axisY;
//        private float axisZ;
//
//        public GyroscopeValues(float[] values) {
//            axisX = values[0];
//            axisY = values[1];
//            axisZ = values[2];
//        }
//
//        public float getAxisX() {
//            return axisX;
//        }
//
//        public float getAxisY() {
//            return axisY;
//        }
//
//        public float getAxisZ() {
//            return axisZ;
//        }
//    }
}
