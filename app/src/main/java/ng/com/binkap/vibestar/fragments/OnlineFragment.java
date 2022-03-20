package ng.com.binkap.vibestar.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ng.com.binkap.vibestar.R;

public class OnlineFragment extends Fragment {

    public OnlineFragment() {
        // Required empty public constructor
    }

    public static OnlineFragment newInstance() {
        OnlineFragment fragment = new OnlineFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_online, container, false);
    }
}