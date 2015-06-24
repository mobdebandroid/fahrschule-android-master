package de.freenet.pocketfahrschulelite.main_fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.freenet.pocketfahrschulelite.R;

/**
 * Created by lion88 on 16.06.2015.
 */
public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View views = inflater.inflate(R.layout.settings, container, false);

        return views;
    }

}
