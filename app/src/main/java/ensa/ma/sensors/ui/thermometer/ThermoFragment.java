package ensa.ma.sensors.ui.thermometer;

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

import java.util.Locale;

import ensa.ma.sensors.R;

/**
 * Fragment affichant les données du capteur de température ou une simulation si absent.
 */
public class ThermoFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor temperatureSensor;

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
        valueText.setText("Initialisation du capteur température...");
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

        temperatureSensor =
                sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

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
        description.setText("Évolution température");
        description.setTextColor(Color.DKGRAY);
        chart.setDescription(description);

        chart.getLegend().setTextColor(Color.DKGRAY);
    }

    @Override
    public void onResume() {
        super.onResume();

        realValueReceived = false;
        simulationRunning = false;

        if (temperatureSensor != null) {
            sensorManager.registerListener(
                    this,
                    temperatureSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            );

            valueText.setText("Capteur détecté. Attente des mesures...");

            // Délai pour activer la simulation si aucune donnée réelle n'arrive (ex: émulateur)
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!realValueReceived && isAdded()) {
                        valueText.setText("Capteur inactif. Simulation activée.");
                        startSimulation();
                    }
                }
            }, 3000);

        } else {
            valueText.setText("Capteur absent. Simulation activée.");
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

        float temperature = event.values[0];

        valueText.setText(String.format(Locale.getDefault(), "Température réelle : %.2f °C", temperature));

        addPointToChart(temperature);
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

                // Simulation sinusoïdale réaliste
                float fakeTemperature =
                        24f + (float) Math.sin(simulationTime / 4f) * 4f;

                valueText.setText(String.format(Locale.getDefault(), "Température simulée : %.2f °C", fakeTemperature));

                addPointToChart(fakeTemperature);

                handler.postDelayed(this, 1000);
            }
        }, 500);
    }

    private void addPointToChart(float value) {
        LineData data = chart.getData();

        if (data == null) {
            data = new LineData();
            chart.setData(data);
        }

        LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);

        if (set == null) {
            set = new LineDataSet(null, "Température °C");
            set.setColor(Color.rgb(233, 30, 99));
            set.setCircleColor(Color.rgb(233, 30, 99));
            set.setLineWidth(3f);
            set.setCircleRadius(4f);
            set.setDrawValues(false);
            set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

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
