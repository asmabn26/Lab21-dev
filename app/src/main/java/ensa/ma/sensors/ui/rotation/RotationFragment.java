package ensa.ma.sensors.ui.rotation;

import android.content.Context;
import android.graphics.Color;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import ensa.ma.sensors.R;

public class RotationFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;

    private TextView valueText;
    private LineChart chart;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private int pointIndex = 0;
    private float simulationTime = 0f;

    private boolean realValueReceived = false;
    private boolean realMovementDetected = false;
    private boolean simulationRunning = false;

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

        valueText = new TextView(requireContext());
        valueText.setTextSize(20);
        valueText.setTextColor(Color.rgb(194, 24, 91));
        valueText.setText("Initialisation du gyroscope...");
        valueText.setBackgroundResource(R.drawable.bg_chip_lux);
        valueText.setPadding(24, 18, 24, 18);

        chart = new LineChart(requireContext());
        chart.setBackgroundResource(R.drawable.bg_chart_panel);
        chart.setExtraOffsets(10, 10, 10, 10);

        LinearLayout.LayoutParams chartParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        chartParams.setMargins(0, 20, 0, 0);
        chart.setLayoutParams(chartParams);

        root.addView(valueText);
        root.addView(chart);

        configureChart();

        sensorManager = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);

        gyroscopeSensor =
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        return root;
    }

    private void configureChart() {
        LineData data = new LineData();
        chart.setData(data);

        chart.setNoDataText("Préparation du graphe...");
        chart.setBackgroundColor(Color.WHITE);

        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        chart.getXAxis().setTextColor(Color.DKGRAY);
        chart.getAxisLeft().setTextColor(Color.DKGRAY);
        chart.getAxisRight().setEnabled(false);

        Description description = new Description();
        description.setText("Rotation x, y, z");
        description.setTextColor(Color.DKGRAY);
        chart.setDescription(description);

        chart.getLegend().setTextColor(Color.DKGRAY);
    }

    @Override
    public void onResume() {
        super.onResume();

        realValueReceived = false;
        realMovementDetected = false;
        simulationRunning = false;

        if (gyroscopeSensor != null) {
            sensorManager.registerListener(
                    this,
                    gyroscopeSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            );

            valueText.setText("Gyroscope détecté. Attente du mouvement...");

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isAdded() && (!realValueReceived || !realMovementDetected)) {
                        valueText.setText("Téléphone stable. Simulation rotation activée.");
                        startSimulation();
                    }
                }
            }, 3000);

        } else {
            valueText.setText("Gyroscope absent. Simulation activée.");
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

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float magnitude = (float) Math.sqrt(x * x + y * y + z * z);

        realValueReceived = true;

        if (magnitude > 0.05f) {
            realMovementDetected = true;
        }

        valueText.setText(
                "Rotation X : " + String.format("%.2f", x) + " rad/s\n"
                        + "Rotation Y : " + String.format("%.2f", y) + " rad/s\n"
                        + "Rotation Z : " + String.format("%.2f", z) + " rad/s\n"
                        + "Norme : " + String.format("%.2f", magnitude)
        );

        addTripletToChart(x, y, z);
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

                simulationTime++;

                float x = (float) Math.sin(simulationTime / 2f) * 1.5f;
                float y = (float) Math.cos(simulationTime / 3f) * 1.2f;
                float z = (float) Math.sin(simulationTime / 4f) * 0.9f;

                valueText.setText(
                        "Rotation simulée X : " + String.format("%.2f", x) + " rad/s\n"
                                + "Rotation simulée Y : " + String.format("%.2f", y) + " rad/s\n"
                                + "Rotation simulée Z : " + String.format("%.2f", z) + " rad/s"
                );

                addTripletToChart(x, y, z);

                handler.postDelayed(this, 1000);
            }
        }, 500);
    }

    private void addTripletToChart(float x, float y, float z) {
        LineData data = chart.getData();

        if (data == null) {
            data = new LineData();
            chart.setData(data);
        }

        if (data.getDataSetCount() == 0) {
            data.addDataSet(createDataSet("Rotation X", Color.rgb(233, 30, 99)));
            data.addDataSet(createDataSet("Rotation Y", Color.rgb(156, 39, 176)));
            data.addDataSet(createDataSet("Rotation Z", Color.rgb(3, 169, 244)));
        }

        data.addEntry(new Entry(pointIndex, x), 0);
        data.addEntry(new Entry(pointIndex, y), 1);
        data.addEntry(new Entry(pointIndex, z), 2);

        pointIndex++;

        data.notifyDataChanged();

        chart.notifyDataSetChanged();
        chart.setVisibleXRangeMaximum(30);
        chart.moveViewToX(data.getEntryCount());
        chart.invalidate();
    }

    private LineDataSet createDataSet(String label, int color) {
        LineDataSet set = new LineDataSet(new ArrayList<Entry>(), label);
        set.setColor(color);
        set.setCircleColor(color);
        set.setLineWidth(3f);
        set.setCircleRadius(3f);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        return set;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
