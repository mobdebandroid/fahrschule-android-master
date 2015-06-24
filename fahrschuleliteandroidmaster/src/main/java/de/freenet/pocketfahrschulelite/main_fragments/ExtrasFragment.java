package de.freenet.pocketfahrschulelite.main_fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.freenet.pocketfahrschulelite.Formulas;
import de.freenet.pocketfahrschulelite.Instruction;
import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.StVO;
import de.freenet.pocketfahrschulelite.classes.ApplicationStoreHelper;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.tracking.TrackingManager;

/**
 * Created by lion88 on 16.06.2015.
 */
public class ExtrasFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View views = inflater.inflate(R.layout.extras, container, false);

        return views;
    }

    @Override
    public void onResume() {
        super.onResume();
        TrackingManager.getInstance().sendStatistics(getActivity(), FahrschulePreferences.getInstance().getTrackingUrl("E2"));
    }

    public void clickInstructions(View v) {
        TrackingManager.getInstance().sendStatistics(getActivity(), FahrschulePreferences.getInstance().getTrackingUrl("E3.1"));
        Intent i = new Intent(getActivity(), Instruction.class);
        startActivity(i);
    }

    public void clickFormulary(View v) {
        TrackingManager.getInstance().sendStatistics(getActivity(), FahrschulePreferences.getInstance().getTrackingUrl("E3.2"));
        Intent i = new Intent(getActivity(), Formulas.class);
        startActivity(i);
    }

    public void clickBuyFullVersion(View v) {
        ApplicationStoreHelper.openFullVersionStorePage(getActivity());
    }

    public void clickStVO(View v) {
        TrackingManager.getInstance().sendStatistics(getActivity(), FahrschulePreferences.getInstance().getTrackingUrl("E3.4"));
        Intent i = new Intent(getActivity(), StVO.class);
        startActivity(i);
    }

    public void clickRateApp(View v) {
        ApplicationStoreHelper.openLiteVersionStorePage(getActivity());
    }
}
