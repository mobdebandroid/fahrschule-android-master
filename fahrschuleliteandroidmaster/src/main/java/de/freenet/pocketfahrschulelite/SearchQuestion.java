package de.freenet.pocketfahrschulelite;

import java.util.Arrays;
import java.util.List;

import de.freenet.pocketfahrschulelite.adapters.QuestionAdapter;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.objects.Question;
import de.freenet.pocketfahrschulelite.objects.QuestionModel;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class SearchQuestion extends PocketFahrschuleListActivity implements TextWatcher, OnItemClickListener {
	
	private SearchTask mSearchTask;
	private EditText mSearchEditText;
	private TextView mResultTextView;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_question);
        
        setTitle(R.string.search);
        
        mSearchEditText = (EditText) findViewById(R.id.searchEditText);
        mSearchEditText.addTextChangedListener(this);
        
        mResultTextView = (TextView) this.findViewById(android.R.id.text1);
        
        setListAdapter(new QuestionAdapter(this, Arrays.asList(new Question[0])));
        getListView().setOnItemClickListener(this);
    }
	
	@Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK) {
    		if (mSearchTask != null) mSearchTask.cancel(true);
			
			mSearchTask = new SearchTask();
			mSearchTask.execute(mSearchEditText.getText().toString());
    	}
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.learning, menu);
        menu.removeItem(R.id.menu_search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
        // Handle item selection
        switch (item.getItemId()) {
	        case R.id.menu_polling:
	        	PocketFahrschuleListActivity.showPollingDialog(this, mSearchEditText.getText().toString(), R.array.polling_choices_2);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void afterTextChanged(Editable s) { }

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		
		QuestionAdapter adapter = (QuestionAdapter) getListAdapter();
		int len = s.length();
		
		if (len > 2) {
			
			if (mSearchTask != null) mSearchTask.cancel(true);
			
			mSearchTask = new SearchTask();
			mSearchTask.execute(s.toString());
		}
		else {
			adapter.clear();
			adapter.notifyDataSetChanged();
			
			mResultTextView.setVisibility(View.GONE);
		}
	}
	
	public void clearEditText(View v) {
		mSearchEditText.setText("");
		
		QuestionAdapter adapter = (QuestionAdapter) getListAdapter();
		adapter.clear();
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		QuestionModel.createModelsForQuestions(((QuestionAdapter) getListAdapter()).getItems());
		Intent i = new Intent(this, QuestionSheet.class);
		i.putExtra(QuestionSheet.EXTRA_INDEX, arg2);
		startActivityForResult(i, 0);
	}

	private class SearchTask extends AsyncTask<String, Void, List<Question>> {
		
		private FahrschuleDatabaseHelper mDb;
		
		@Override
		protected void onPreExecute() {
			mDb = new FahrschuleDatabaseHelper(SearchQuestion.this);
			
			if (getListAdapter().getCount() == 0) {
				mResultTextView.setVisibility(View.VISIBLE);
				mResultTextView.setText(R.string.searching);
			}
			else {
				mResultTextView.setVisibility(View.GONE);
			}
		}

		@Override
		protected List<Question> doInBackground(String... params) {
			return mDb.searchQuestions(params[0]);
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			mDb.close();
		}
    	
		@Override
		protected void onPostExecute(List<Question> result) {
			mDb.close();
			
			QuestionAdapter adapter = (QuestionAdapter) getListAdapter();
			adapter.clearAndSetObject(result);
			adapter.notifyDataSetChanged();
			
			if (result.size() == 0) {
				mResultTextView.setVisibility(View.VISIBLE);
				mResultTextView.setText(R.string.no_results);
			}
			else {
				mResultTextView.setVisibility(View.GONE);
			}
		}
    }
}
