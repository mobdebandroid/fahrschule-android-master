package de.freenet.pocketfahrschulelite.question_fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import de.freenet.pocketfahrschulelite.PocketFahrschuleListActivity;
import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.SearchQuestion;
import de.freenet.pocketfahrschulelite.SubGroupList;
import de.freenet.pocketfahrschulelite.adapters.MainGroupAdapter;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.tracking.TrackingManager;

/**
 * Created by lion88 on 17.06.2015.
 */
public class QuestionCatalogFragment extends ListFragment {

    private boolean mFirstStart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(getActivity());
        setListAdapter(new MainGroupAdapter(getActivity(), db.getMainGroups()));
        db.close();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mFirstStart) {
            FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(getActivity());
            ((MainGroupAdapter) getListAdapter()).clearAndSetObject(db.getMainGroups());
            db.close();
            ((MainGroupAdapter) getListAdapter()).notifyDataSetChanged();
        }
        mFirstStart = false;

        TrackingManager.getInstance().sendStatistics(getActivity(), FahrschulePreferences.getInstance().getTrackingUrl("B2"));

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.learning, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_polling:
                PocketFahrschuleListActivity.showPollingDialog(getActivity(), null);
                return true;
            case R.id.menu_search:
                Intent i = new Intent(getActivity(), SearchQuestion.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick (ListView l, View v, int position, long id) {
        if (position == 0 || position == 1 || position == 5) {
            Intent i = new Intent(getActivity(), SubGroupList.class);
            i.putExtra(SubGroupList.EXTRA_MAINGROUP_ID, ((MainGroupAdapter) getListAdapter()).getItem(position).id);
            i.putExtra(SubGroupList.EXTRA_MAINGROUP_TITLE, ((MainGroupAdapter) getListAdapter()).getItem(position).name);
            startActivity(i);
        }
    }

}
