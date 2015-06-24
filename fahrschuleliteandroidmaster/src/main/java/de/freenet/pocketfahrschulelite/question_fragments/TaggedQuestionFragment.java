package de.freenet.pocketfahrschulelite.question_fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import de.freenet.pocketfahrschulelite.QuestionSheet;
import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.adapters.QuestionAdapter;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.pocketfahrschulelite.objects.QuestionModel;
import de.freenet.tracking.TrackingManager;

/**
 * Created by lion88 on 17.06.2015.
 */
public class TaggedQuestionFragment extends ListFragment implements AdapterViewCompat.OnItemClickListener {

    private TextView mTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tagged_question, container, false);

        mTextView = (TextView) view.findViewById(android.R.id.text1);

        return view;
    }

        @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();

        FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(getActivity());
        setListAdapter(new QuestionAdapter(getActivity(), db.getTaggedQuestions()));
        db.close();

        if (getListAdapter().getCount() == 0) {
            mTextView.setVisibility(View.VISIBLE);
        }
        else {
            mTextView.setVisibility(View.GONE);
        }

        TrackingManager.getInstance().sendStatistics(getActivity(), FahrschulePreferences.getInstance().getTrackingUrl("B7"));
    }

    @Override
    public void onItemClick(AdapterViewCompat<?> arg0, View arg1, int arg2, long arg3) {
        QuestionModel.createModelsForQuestions(((QuestionAdapter) getListAdapter()).getItems());

        Intent i = new Intent(getActivity(), QuestionSheet.class);
        i.putExtra(QuestionSheet.EXTRA_INDEX, arg2);
        startActivity(i);
    }

}