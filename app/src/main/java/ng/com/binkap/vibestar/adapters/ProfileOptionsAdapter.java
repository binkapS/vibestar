package ng.com.binkap.vibestar.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.helpers.Strings;
import ng.com.binkap.vibestar.models.ProfileOptionsModel;

public class ProfileOptionsAdapter extends RecyclerView.Adapter<ProfileOptionsAdapter.ViewHolder> {

    List<ProfileOptionsModel> options;

    public ProfileOptionsAdapter(List<ProfileOptionsModel> options) {
        this.options = options;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_screen_options_linear_recycler, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProfileOptionsModel option = options.get(position);
        holder.icon.setImageResource(option.getIcon());
        holder.name.setText(option.getName());
        holder.itemView.setOnClickListener(view -> {
            if (option.isActivity()){
                view.getContext().startActivity(new Intent(view.getContext(), option.getActivity()));
            }else {
                switch (option.getName()){
                    case Strings.RATE:
                        Toast.makeText(view.getContext(), "Rate Us Clicked", Toast.LENGTH_SHORT).show();
                        break;
                    case Strings.SHARE:
                        Toast.makeText(view.getContext(), "Share Clicked", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView icon;

        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.profile_options_icon);
            name = itemView.findViewById(R.id.profile_options_name);
        }
    }
}
