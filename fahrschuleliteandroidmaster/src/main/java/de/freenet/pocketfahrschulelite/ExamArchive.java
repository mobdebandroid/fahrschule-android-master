package de.freenet.pocketfahrschulelite;

import de.freenet.pocketfahrschulelite.adapters.ExamArchiveAdapter;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ExamArchive extends PocketFahrschuleListActivity implements OnItemClickListener {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ListView listView = new ListView(this);
        listView.setId(android.R.id.list);
        listView.setBackgroundResource(R.drawable.bg_mit_gitter);
        setContentView(listView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
        setListAdapter(new ExamArchiveAdapter(this, db.getExamStatistics(-1)));
        db.close();
        
        getListView().setOnItemClickListener(this);
    }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		((ExamArchiveAdapter) getListAdapter()).getItem(arg2).createModelsFromExam();
		
		Intent i = new Intent(this, QuestionSheet.class);
		i.putExtra(QuestionSheet.EXTRA_HIDE_SOLUTION_BUTTON, true);
		i.putExtra(QuestionSheet.EXTRA_IS_OLD_OFFICIAL_EXAM, FahrschulePreferences.getInstance().isOfficialExamLayout());
		startActivity(i);
	}
}
