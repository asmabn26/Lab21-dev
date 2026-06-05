package ensa.ma.sensors.ui.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ensa.ma.sensors.R;
import ensa.ma.sensors.beans.SensorItem;

public class ListSonsorsFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    public ListSonsorsFragment() {
    }

    public static ListSonsorsFragment newInstance(int columnCount) {
        ListSonsorsFragment fragment = new ListSonsorsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    private List<SensorItem> loadSensor() {
        List<SensorItem> sensors = new ArrayList<>();

        SensorManager sensorManager = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);

        int counter = 0;

        for (Sensor currentSensor : sensorList) {
            counter++;

            String sensorId = String.valueOf(counter);

            String sensorName = currentSensor.getName();

            String sensorType = sensorTypeToString(currentSensor.getType())
                    + "\nInt Type : " + currentSensor.getType();

            String sensorVendor = currentSensor.getVendor();

            String sensorVersion = String.valueOf(currentSensor.getVersion());

            String sensorResolution = String.valueOf(currentSensor.getResolution());

            String sensorRange = String.valueOf(currentSensor.getMaximumRange());

            String sensorPower = currentSensor.getPower() + " mA";

            String sensorSpeed = formatAcquisitionSpeed(currentSensor.getMinDelay());

            sensors.add(
                    new SensorItem(
                            sensorId,
                            sensorName,
                            sensorType,
                            sensorVendor,
                            sensorVersion,
                            sensorResolution,
                            sensorRange,
                            sensorPower,
                            sensorSpeed
                    )
            );
        }

        return sensors;
    }

    private String formatAcquisitionSpeed(int minDelay) {
        if (minDelay <= 0) {
            return "Non définie";
        }

        float speedHz = 1000000f / minDelay;

        return minDelay + " µs ≈ " + String.format("%.2f", speedHz) + " Hz";
    }

    @SuppressWarnings("deprecation")
    public static String sensorTypeToString(int sensorType) {
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                return "Accéléromètre";

            case Sensor.TYPE_AMBIENT_TEMPERATURE:
            case Sensor.TYPE_TEMPERATURE:
                return "Température ambiante";

            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                return "Vecteur de rotation jeu";

            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                return "Vecteur rotation géomagnétique";

            case Sensor.TYPE_GRAVITY:
                return "Gravité";

            case Sensor.TYPE_GYROSCOPE:
                return "Gyroscope";

            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                return "Gyroscope non calibré";

            case Sensor.TYPE_HEART_RATE:
                return "Fréquence cardiaque";

            case Sensor.TYPE_LIGHT:
                return "Lumière";

            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "Accélération linéaire";

            case Sensor.TYPE_MAGNETIC_FIELD:
                return "Champ magnétique";

            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                return "Champ magnétique non calibré";

            case Sensor.TYPE_ORIENTATION:
                return "Orientation";

            case Sensor.TYPE_PRESSURE:
                return "Pression";

            case Sensor.TYPE_PROXIMITY:
                return "Proximité";

            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "Humidité relative";

            case Sensor.TYPE_ROTATION_VECTOR:
                return "Vecteur de rotation";

            case Sensor.TYPE_SIGNIFICANT_MOTION:
                return "Mouvement significatif";

            case Sensor.TYPE_STEP_COUNTER:
                return "Compteur de pas";

            case Sensor.TYPE_STEP_DETECTOR:
                return "Détecteur de pas";

            default:
                return "Type inconnu";
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_listsonsors_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            recyclerView.setAdapter(
                    new ListSonsorsFragmentRecyclerViewAdapter(loadSensor(), mListener)
            );
        }

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(SensorItem item);
    }
}