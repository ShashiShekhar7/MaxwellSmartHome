package com.shashi.maxwellsmarthome.roomdb;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shashi.maxwellsmarthome.R;

import java.util.List;

public class AdapterConfigureSuccess extends RecyclerView.Adapter<AdapterConfigureSuccess.MyViewHolder> {

    private final List<ModelConfiguredDevice> deviceList;

    public AdapterConfigureSuccess(List<ModelConfiguredDevice> deviceList) {
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_config_succes_device, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ModelConfiguredDevice device = deviceList.get(position);
        holder.textView.setText(device.name);
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_card_success_device_name);
        }
    }
}
