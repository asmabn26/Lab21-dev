package ensa.ma.sensors.ui.proximity;

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

public class ProximityFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor proximitySensor;

    private TextView valueText;
    private LineChart chart;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private float simulationTime = 0f;
    private int pointIndex = 0;

    private boolean realValueReceived = false;
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
        valueText.setText("Initialisation du capteur proximité...");
        valueText.setBackgroundResource(R.drawable.bg_chip_lux);
        valueText.setPadding(24, 18, 24, 18);

        chart = new LineChart(requireContext());

        LinearLayout.LayoutParams chartParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        chartParams.setMargins(0, 20, 0, 0);
        chart.setLayoutParams(chartParams);
        chart.setBackgroundResource(R.drawable.bg_chart_panel);
        chart.setExtraOffsets(10, 10, 10, 10);

        root.addView(valueText);
        root.addView(chart);

        configureChart();

        sensorManager = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);

        proximitySensor =
                sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        return root;
    }

    private void configureChart() {
        LineData lineData = new LineData();
        chart.setData(lineData);

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
        description.setText("Évolution proximité");
        description.setTextColor(Color.DKGRAY);
        chart.setDescription(description);

        chart.getLegend().setTextColor(Color.DKGRAY);
    }

    @Override
    public void onResume() {
        super.onResume();

        realValueReceived = false;
        simulationRunning = false;

        if (proximitySensor != null) {
            sensorManager.registerListener(
                    this,
                    proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            );

            valueText.setText("Capteur détecté. Attente des mesures...");

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!realValueReceived && isAdded()) {
                        valueText.setText("Aucune mesure reçue. Simulation activée.");
                        startSimulation();
                    }
                }
            }, 2000);

        } else {
            valueText.setText("Capteur proximité absent. Simulation activée.");
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
        realValueReceived = true;

        float proximity = event.values[0];

        valueText.setText(
                "Proximité réelle : "
                        + String.format("%.2f", proximity)
                        + "\nÉtat : " + getState(proximity)
        );

        addPointToChart(proximity);
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

                float fakeProximity;

                if (((int) simulationTime) % 8 < 4) {
                    fakeProximity = 0f;
                } else {
                    fakeProximity = 5f;
                }

                valueText.setText(
                        "Proximité simulée : "
                                + String.format("%.2f", fakeProximity)
                                + "\nÉtat : " + getState(fakeProximity)
                );

                addPointToChart(fakeProximity);

                handler.postDelayed(this, 1000);
            }
        }, 500);
    }

    private String getState(float value) {
        if (value <= 1f) {
            return "Objet proche";
        } else {
            return "Aucun objet proche";
        }
    }

    private void addPointToChart(float value) {
        LineData data = chart.getData();

        if (data == null) {
            data = new LineData();
            chart.setData(data);
        }

        LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);

        if (set == null) {
            set = new LineDataSet(new ArrayList<Entry>(), "Proximité");
            set.setColor(Color.rgb(233, 30, 99));
            set.setCircleColor(Color.rgb(233, 30, 99));
            set.setLineWidth(3f);
            set.setCircleRadius(4f);
            set.setDrawValues(false);
            set.setMode(LineDataSet.Mode.STEPPED);

            data.addDataSet(set);
        }

        data.addEntry(new Entry(pointIndex, value), 0);
        pointIndex++;

        data.notifyDataChanged();

        chart.notifyDataSetChanged();
        chart.setVisibleXRangeMaximum(30);
        chart.moveViewToX(data.getEntryCount());
        chart.invalidate();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}