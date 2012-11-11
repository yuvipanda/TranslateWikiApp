package net.translatewiki;

import java.io.IOException;
import java.util.ArrayList;

import org.mediawiki.api.ApiResult;
import org.mediawiki.api.MWApi;
import org.mediawiki.auth.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MessageGroupGetterActivity extends Activity implements OnSharedPreferenceChangeListener{

	/** Called when the activity is first created. */
	final Context context = this;
	MessageGroup msgGrp ;
	MessageGroupListAdapter groups;

	TranslateWikiApp app;
	ArrayList<MessageGroup> messageGroupsList;

	//String prefGroupString;


	private class FetchGroupsTask extends AsyncTask<Void, Void, ArrayList<MessageGroup>> {

		private Activity context;

		private ProgressDialog dialog;
		public FetchGroupsTask(Activity context) {
			this.context = context;

		}

		@Override
		protected void onPostExecute(ArrayList<MessageGroup> result) {
			groups.clear(); 
			for(MessageGroup grp : result) {
				groups.add(grp);
			}

			dialog.dismiss();

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			dialog = new ProgressDialog(context);
			dialog.setTitle("Loading Groups...");
			dialog.show();
		}

		@Override
		protected ArrayList<MessageGroup> doInBackground(Void... params) {
			MWApi api = app.getApi();
			messageGroupsList = new ArrayList<MessageGroup>();
			ApiResult result;
			try {
				String userId = api.getUserID();
				result = api.action("query")
						.param("meta", "messagegroups")
						.get();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			ArrayList<ApiResult> messageGroups = result.getNodes("/api/query/messagegroups/group");
			Log.d("TWG", "Actual result is" + Utils.getStringFromDOM(result.getDocument()));
			int messageGroupCounter =0;
			for(ApiResult group: messageGroups) {

				MessageGroup g = new MessageGroup(group.getString("@id"), group.getString("@label"), group.getString("@description"), group.getString("@class"), group.getString("@exists"));
				messageGroupsList.add(g);
				Log.d("TWG", Integer.valueOf(++messageGroupCounter).toString() + g.toString());
				if(messageGroupCounter > 10) // adding only the first few items in the list instead of all the thousand groups
				{
					break;
				}
			}

			return messageGroupsList;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = (TranslateWikiApp)this.getApplicationContext();
		setContentView(R.layout.message_groups_list);
		groups = new MessageGroupListAdapter(this, 0);
		ListView listView = (ListView)findViewById(R.id.listMessageGroupPref);
		listView.setAdapter(groups);
		listView.setOnItemClickListener(onListItemClick);
		getTheGroupsNow();



	}

	private AdapterView.OnItemClickListener onListItemClick = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {


			String prefGroup = messageGroupsList.get(arg2).getGroupId();
			MessageGroup myGroup = messageGroupsList.get(arg2);
			Log.d("TWG", "Group : " +myGroup);
			Log.d("TWG", "Preffered Group id = " + myGroup.getGroupId());
			Toast.makeText(context,"" + prefGroup , Toast.LENGTH_LONG).show();
			if(!prefGroup.equals(null))
			{

				PreferenceManager.getDefaultSharedPreferences(context).edit().putString("group", prefGroup).commit();
				Intent goToPref = new Intent(context, ProofReadActivity.class);
				startActivity(goToPref);
			}




		}

	};
	private void getTheGroupsNow() {
		// TODO Auto-generated method stub
		FetchGroupsTask fetchGroups = new FetchGroupsTask(this);
		Utils.executeAsyncTask(fetchGroups);
		//fetchGroups.execute();

	}

	private class MessageGroupListAdapter extends ArrayAdapter<MessageGroup> {

		public MessageGroupListAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if(v == null) {
				v = getLayoutInflater().inflate(R.layout.listitem_preference_groups, null);
			}

			final MessageGroup mGrp = this.getItem(position);

			TextView lblGroupName = (TextView) v.findViewById(R.id.lblPrefGroupName);
			TextView lblGroupDesc = (TextView) v.findViewById(R.id.lblPrefGroupDesc);
			lblGroupName.setText(mGrp.getGroupName());
			lblGroupDesc.setText(mGrp.getGroupDesc());

			return v;
		}


	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub

	}
}


