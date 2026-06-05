package ensa.ma.sensors.ui.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ensa.ma.sensors.R;

public class CompassFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private TextView headingText;
    private CompassView compassView;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final float[] gravityValues = new float[3];
    private final float[] magneticValues = new float[3];

    private boolean hasGravity = false;
    private boolean hasMagnetic = false;

    private boolean headingReceived = false;
    private boolean headingChanged = false;
    private boolean simulationRunning = false;

    private float lastHeading = -1f;
    private float simulationHeading = 0f;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        root.setBackgroundResource(R.drawable.bg_screen_lux);

        headingText = new TextView(requireContext());
        headingText.setTextSize(20);
        headingText.setTextColor(Color.rgb(194, 24, 91));
        headingText.setText("Initialisation de la boussole...");
        headingText.setBackgroundResource(R.drawable.bg_chip_lux);
        headingText.setPadding(24, 18, 24, 18);

        compassView = new CompassView(requireContext());

        LinearLayout.LayoutParams compassParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        600
                );

        compassParams.setMargins(0, 40, 0, 0);
        compassView.setLayoutParams(compassParams);

        root.addView(headingText);
        root.addView(compassView);

        sensorManager = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);

        accelerometer =
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        magnetometer =
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        headingReceived = false;
        headingChanged = false;
        simulationRunning = false;
        lastHeading = -1f;

        if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_UI
            );

            sensorManager.registerListener(
                    this,
                    magnetometer,
                    SensorManager.SENSOR_DELAY_UI
            );

            headingText.setText("Boussole réelle activée. Tourne le téléphone.");

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isAdded() && (!headingReceived || !headingChanged)) {
                        headingText.setText("Boussole stable. Simulation activée.");
                        startSimulation();
                    }
                }
            }, 3000);

        } else {
            headingText.setText("Capteur boussole manquant. Simulation activée.");
            startSimulation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        handler.removeCallbacksAndMessages(null);
        simulationRunning = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (simulationRunning) {
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityValues, 0, 3);
            hasGravity = true;
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magneticValues, 0, 3);
            hasMagnetic = true;
        }

        if (hasGravity && hasMagnetic) {
            float[] rotationMatrix = new float[9];
            float[] orientation = new float[3];

            boolean success = SensorManager.getRotationMatrix(
                    rotationMatrix,
                    null,
                    gravityValues,
                    magneticValues
            );

            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientation);

                float heading = (float) Math.toDegrees(orientation[0]);

                if (heading < 0) {
                    heading += 360;
                }

                headingReceived = true;

                if (lastHeading >= 0 && Math.abs(heading - lastHeading) > 2f) {
                    headingChanged = true;
                }

                lastHeading = heading;

                updateCompass(heading, false);
            }
        }
    }

    private void startSimulation() {
        if (simulationRunning) {
            return;
        }

        simulationRunning = true;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!simulationRunning || !isAdded()) {
                    return;
                }

                simulationHeading += 12f;

                if (simulationHeading >= 360f) {
                    simulationHeading = 0f;
                }

                updateCompass(simulationHeading, true);

                handler.postDelayed(this, 700);
            }
        }, 500);
    }

    private void updateCompass(float heading, boolean simulated) {
        String mode;

        if (simulated) {
            mode = "Boussole simulée";
        } else {
            mode = "Boussole réelle";
        }

        headingText.setText(
                mode + "\nAngle : "
                        + String.format("%.1f", heading)
                        + "°\nDirection : "
                        + getDirectionName(heading)
        );

        compassView.setHeading(heading);
    }

    private String getDirectionName(float degree) {
        if (degree >= 337.5 || degree < 22.5) {
            return "Nord";
        } else if (degree < 67.5) {
            return "Nord-Est";
        } else if (degree < 112.5) {
            return "Est";
        } else if (degree < 157.5) {
            return "Sud-Est";
        } else if (degree < 202.5) {
            return "Sud";
        } else if (degree < 247.5) {
            return "Sud-Ouest";
        } else if (degree < 292.5) {
            return "Ouest";
        } else {
            return "Nord-Ouest";
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public static class CompassView extends View {

        private final Paint circlePaint = new Paint();
        private final Paint textPaint = new Paint();
        private final Paint needlePaint = new Paint();
        private final Paint centerPaint = new Paint();

        private float heading = 0f;

        public CompassView(Context context) {
            super(context);

            circlePaint.setColor(Color.rgb(244, 143, 177));
            circlePaint.setStrokeWidth(6f);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setAntiAlias(true);

            textPaint.setColor(Color.rgb(60, 60, 60));
            textPaint.setTextSize(42f);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setAntiAlias(true);

            needlePaint.setColor(Color.rgb(194, 24, 91));
            needlePaint.setStyle(Paint.Style.FILL);
            needlePaint.setAntiAlias(true);

            centerPaint.setColor(Color.rgb(156, 39, 176));
            centerPaint.setStyle(Paint.Style.FILL);
            centerPaint.setAntiAlias(true);
        }

        public void setHeading(float heading) {
            this.heading = heading;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int height = getHeight();

            float cx = width / 2f;
            float cy = height / 2f;
            float radius = Math.min(width, height) / 3f;

            canvas.drawCircle(cx, cy, radius, circlePaint);
            canvas.drawCircle(cx, cy, radius * 0.75f, circlePaint);
            canvas.drawCircle(cx, cy, radius * 0.50f, circlePaint);

            canvas.drawText("N", cx, cy - radius - 25, textPaint);
            canvas.drawText("S", cx, cy + radius + 55, textPaint);
            canvas.drawText("E", cx + radius + 35, cy + 15, textPaint);
            canvas.drawText("W", cx - radius - 35, cy + 15, textPaint);

            canvas.save();
            canvas.rotate(heading, cx, cy);

            Path needle = new Path();
            needle.moveTo(cx, cy - radius + 20);
            needle.lineTo(cx - 25, cy + 20);
            needle.lineTo(cx + 25, cy + 20);
            needle.close();

            canvas.drawPath(needle, needlePaint);

            canvas.restore();

            canvas.drawCircle(cx, cy, 18, centerPaint);
        }
    }
}
