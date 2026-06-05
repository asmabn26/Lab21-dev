package ensa.ma.sensors.ui.sensors;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ensa.ma.sensors.R;
import ensa.ma.sensors.beans.SensorItem;
import ensa.ma.sensors.ui.sensors.ListSonsorsFragment.OnListFragmentInteractionListener;

import java.util.List;

public class ListSonsorsFragmentRecyclerViewAdapter
        extends RecyclerView.Adapter<ListSonsorsFragmentRecyclerViewAdapter.ViewHolder> {

    private final List<SensorItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public ListSonsorsFragmentRecyclerViewAdapter(
            List<SensorItem> items,
            OnListFragmentInteractionListener listener
    ) {
        mValues = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_listsonsors, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull final ViewHolder holder,
            int position
    ) {
        SensorItem currentItem = mValues.get(position);

        holder.mItem = currentItem;

        holder.mIdView.setText("Capteur N° : " + currentItem.id);
        holder.mNameView.setText("Nom : " + currentItem.name);
        holder.mVendorView.setText("Fabricant : " + currentItem.vendor);
        holder.mTypeView.setText("Type : " + currentItem.type);
        holder.mVersionView.setText("Version : " + currentItem.version);

        holder.mResolutionView.setText("Résolution : " + currentItem.resolution);
        holder.mPowerView.setText("Énergie : " + currentItem.power);
        holder.mMaximumRangeView.setText("Plage maximale : " + currentItem.range);
        holder.mMaximumSpeedView.setText("Vitesse max : " + currentItem.max_speed);

        holder.mView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onListFragmentInteraction(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;

        public final TextView mIdView;
        public final TextView mNameView;
        public final TextView mTypeView;
        public final TextView mVendorView;
        public final TextView mVersionView;
        public final TextView mResolutionView;
        public final TextView mMaximumRangeView;
        public final TextView mPowerView;
        public final TextView mMaximumSpeedView;

        public SensorItem mItem;

        public ViewHolder(@NonNull View view) {
            super(view);

            mView = view;

            mIdView = view.findViewById(R.id.item_number);
            mNameView = view.findViewById(R.id.name);
            mTypeView = view.findViewById(R.id.type);
            mVendorView = view.findViewById(R.id.vendor);
            mVersionView = view.findViewById(R.id.version);
            mResolutionView = view.findViewById(R.id.resolution);
            mMaximumRangeView = view.findViewById(R.id.maximum_range);
            mPowerView = view.findViewById(R.id.power);
            mMaximumSpeedView = view.findViewById(R.id.maximum_speed);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}