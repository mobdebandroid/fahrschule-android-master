package de.freenet.pocketfahrschulelite.main_fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;

import de.freenet.pocketfahrschulelite.QuestionCatalogNavigation;
import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.pocketfahrschulelite.objects.LearnStatistic;
import de.freenet.pocketfahrschulelite.widget.KanisterView;

/**
 * A simple {@link Fragment} subclass.
 */
public class LearningFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int REQUEST_CODE = 100;

//    private KanisterView mKanisterView;
    private Button learningButton;
    private PieGraph pg;
    TextView mRightige, mFalsch, mVerbleibend, mBeantwortet;

    public LearningFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.learning, container, false);

        mRightige = (TextView) view.findViewById(R.id.mRightige);
        mFalsch = (TextView) view.findViewById(R.id.mFalsch);
        mVerbleibend = (TextView) view.findViewById(R.id.mVerbleibend);
        mBeantwortet = (TextView) view.findViewById(R.id.mBeanwortet);

        pg = (PieGraph)view.findViewById(R.id.learningKanisterImageView);

//        mKanisterView = (KanisterView) view.findViewById(R.id.learningKanisterImageView);
        learningButton = (Button) view.findViewById(R.id.learningButton);
        learningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), QuestionCatalogNavigation.class);
                startActivityForResult(i, REQUEST_CODE);
            }
        });

        loadValuesToKanisterView();

        FahrschulePreferences.getInstance().registerOnSharedPreferenceChangeListener(this);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FahrschulePreferences.getInstance().unregisterOnSharedPreferenceChangeListener(this);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            Utils.showQuitApplicationDialog(getActivity());
//
//            return true;
//        }
//
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("licenseClass")) {
//            mKanisterView.clearValues();
            loadValuesToKanisterView();
//            mKanisterView.invalidate();
        }
    }

//    public void clickStartLearning(View v) {
//        Intent i = new Intent(getActivity(), QuestionCatalogNavigation.class);
//        startActivityForResult(i, REQUEST_CODE);
//    }

    private void loadValuesToKanisterView() {
        FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(getActivity());

        int rightige = db.countQuestions(LearnStatistic.StatisticState.CORRECT_ANSWERED);
        int falsch = db.countQuestions(LearnStatistic.StatisticState.FAULTY_ANSWERED);
        int verbleibend = db.countQuestions(null) - rightige - falsch;

        mRightige.setText(String.valueOf(rightige));
        mFalsch.setText(String.valueOf(falsch));
        mVerbleibend.setText(String.valueOf(verbleibend));

        PieSlice slice = new PieSlice();
        slice.setColor(getResources().getColor(R.color.richtige));
        slice.setValue(rightige);
        pg.addSlice(slice);
        slice = new PieSlice();
        slice.setColor(getResources().getColor(R.color.falsch));
        slice.setValue(falsch);
        pg.addSlice(slice);
        slice = new PieSlice();
        slice.setColor(getResources().getColor(R.color.verbleibend));
        slice.setValue(verbleibend);
        pg.setInnerCircleRatio(180);
//        pg.setPadding(20);
        pg.addSlice(slice);


//        mKanisterView.addValues(LearnStatistic.StatisticState.CORRECT_ANSWERED, rightige);
//        mKanisterView.addValues(LearnStatistic.StatisticState.FAULTY_ANSWERED, falsch);
//        mKanisterView.addValues(LearnStatistic.StatisticState.NOT_ANSWERED, db.countQuestions(null) - rightige - falsch);

        db.close();
    }

}

