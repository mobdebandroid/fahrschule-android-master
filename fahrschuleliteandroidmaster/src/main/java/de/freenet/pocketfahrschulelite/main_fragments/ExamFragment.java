package de.freenet.pocketfahrschulelite.main_fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import de.freenet.pocketfahrschulelite.Graph;
import de.freenet.pocketfahrschulelite.QuestionSheet;
import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.pocketfahrschulelite.objects.ExamStatistic;
import de.freenet.pocketfahrschulelite.objects.QuestionModel;
import de.freenet.pocketfahrschulelite.widget.BarChartView;
import de.freenet.tracking.TrackingManager;
import de.freenet.view.OrientationListener;
import de.freenet.view.OrientationManager;

/**
 * Created by lion88 on 16.06.2015.
 */
public class ExamFragment extends Fragment implements /*SharedPreferences.OnSharedPreferenceChangeListener, */OrientationListener {

    private BarChartView mBarChartView;
    private TextView mExamIndicationBadgeTextView;
    private OrientationManager mOrientationManager;

    public static final int REQUEST_CODE = 200;
    public static final int RESULT_EXAM_ABORTED = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View views = inflater.inflate(R.layout.exam, container, false);
        mBarChartView = (BarChartView) views.findViewById(R.id.examBarChartView);
        loadBarChartView(views);

//        FahrschulePreferences.getInstance().registerOnSharedPreferenceChangeListener(getActivity());

        mOrientationManager = new OrientationManager(getActivity());

        return views;
    }

    @Override
    public void onResume() {
        super.onResume();
        mOrientationManager.startListening(this);

        TrackingManager.getInstance().sendStatistics(getActivity(), FahrschulePreferences.getInstance().getTrackingUrl("C2"));
    }

    @Override
    public void onPause() {
        super.onPause();
        mOrientationManager.stopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        FahrschulePreferences.getInstance().unregisterOnSharedPreferenceChangeListener(this);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            Utils.showQuitApplicationDialog(this);
//
//            return true;
//        }
//
//        return super.onKeyDown(keyCode, event);
//    }
    /**
     * OnClick method for starting an exam. If a cancel exam is available the user will be asked if this exam should be used, otherwise
     * a new exam will be used
     * @param v the Button view
     */
    public void clickStartExam(View v) {
        FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(getActivity());

        // An old exam is saved. Ask user if it should be resumed
        if (db.countExams(ExamStatistic.ExamState.CANCELED_EXAM) > 0) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(getActivity());
                    Intent i = null;

                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // Start saved exam
                            List<ExamStatistic> stats = db.getExamStatistics(1, ExamStatistic.ExamState.CANCELED_EXAM);

                            if (stats.size() > 0) {
                                stats.get(0).createModelsFromExam();

                                i = new Intent(getActivity(), QuestionSheet.class);
                                i.putExtra(QuestionSheet.EXTRA_IS_EXAM, true);
                                i.putExtra(QuestionSheet.EXTRA_TIME_LEFT, stats.get(0).secondsLeft);
                                i.putExtra(QuestionSheet.EXTRA_INDEX, stats.get(0).selectedQuestionIndex);
                                startActivityForResult(i, REQUEST_CODE);
                            }
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            // Start new exam
                            QuestionModel.createModelsForQuestions(db.getExamQuestions());

                            i = new Intent(getActivity(), QuestionSheet.class);
                            i.putExtra(QuestionSheet.EXTRA_IS_EXAM, true);
                            startActivityForResult(i, REQUEST_CODE);

                            break;
                    }
                    db.removeCancelledExam();
                    db.close();
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.resume_exam_dialogbox).setPositiveButton(R.string.yes, dialogClickListener)
                    .setNegativeButton(R.string.no, dialogClickListener).setTitle(R.string.exam).show();
        }
        else {
            QuestionModel.createModelsForQuestions(db.getExamQuestions());

            Intent i = new Intent(getActivity(), QuestionSheet.class);
            i.putExtra(QuestionSheet.EXTRA_IS_EXAM, true);
            startActivityForResult(i, REQUEST_CODE);
        }
        db.close();
    }

    /**
     * Prepares the bar chart in the exam view. If no previous exams exists a placeholder image is shown.
     */
    private void loadBarChartView(View viewing) {
        FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(getActivity());
        List<ExamStatistic> exams = db.getExamStatistics(6);
        db.close();

        RelativeLayout rl = (RelativeLayout) viewing.findViewById(R.id.examBarChartRelativeLayout);
        ImageView iv = (ImageView) viewing.findViewById(R.id.examPlaceholderImageView);
        if (exams.size() == 0) {
            iv.setVisibility(View.VISIBLE);
            rl.setVisibility(View.GONE);
        }
        else {
            iv.setVisibility(View.GONE);
            rl.setVisibility(View.VISIBLE);
        }

        mBarChartView.clearValues();
        for (ExamStatistic exam : exams) {
            mBarChartView.addValues(exam.points, exam.passed, exam.date);
        }
    }

//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        if (key.equals("licenseClass") || key.startsWith("teachingType")) {
//            loadBarChartView(view);
//        }
//    }

    @Override
    public void onOrientationChange(OrientationManager.Orientation orientation) {
        if (orientation.isLandscape() && mBarChartView.size() > 0) {
            TrackingManager.getInstance().sendStatistics(getActivity(), FahrschulePreferences.getInstance().getTrackingUrl("C7"));
            Intent i = new Intent(getActivity(), Graph.class);
            startActivity(i);
            getActivity().overridePendingTransition(0, 0);
        }
    }

}
