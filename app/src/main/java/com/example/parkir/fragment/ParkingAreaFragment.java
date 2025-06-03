package com.example.parkir.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkir.R;
import com.example.parkir.adapter.ParkingSlotAdapter;
import com.example.parkir.model.ParkingArea;
import com.example.parkir.model.ParkingSlot;

import java.io.Serializable;
import java.util.List;

public class ParkingAreaFragment extends Fragment {

    private static final String ARG_PARKING_AREA = "parkingArea";
    private RecyclerView recyclerView;
    private ParkingSlotAdapter adapter;
    private ParkingArea parkingArea;

    public static ParkingAreaFragment newInstance(ParkingArea parkingArea) {
        ParkingAreaFragment fragment = new ParkingAreaFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARKING_AREA, parkingArea);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            parkingArea = (ParkingArea) getArguments().getSerializable(ARG_PARKING_AREA);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parking_area, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewSlotFragment);
        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {
        if (getContext() != null && parkingArea != null) {
            adapter = new ParkingSlotAdapter(getContext(), parkingArea.getVehicle_type(), (slot, position) -> {
                adapter.setSelectedPosition(position);
            });
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4)); // 4 kolom
            recyclerView.setAdapter(adapter);
            adapter.updateSlots(parkingArea.getSlots());
        }
    }

    public ParkingSlot getSelectedSlot() {
        if (adapter != null) {
            return adapter.getSelectedSlot();
        }
        return null;
    }
    public ParkingArea getParkingArea() {
        return parkingArea;
    }
}