/*
 * Copyright (C) 2013  Peter Sulyok
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl.html>.
 */

package org.nutrika;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class Contents extends Activity {
	Context context = this;
	DatabaseIf db = DatabaseIf.INSTANCE;
	ListView contents;
	TextView title;
	ProgressDialog dialog;
	
	static final int DIALOG_COMPUTING_ID = 0;

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
    	setContentView(R.layout.contents);
    	contents = (ListView) findViewById(R.id.contents);
    	title = (TextView) findViewById(R.id.contentstitle);
	}

	public void onResume() {
		super.onResume();
		title.setText(db.getContentsTitle());
		new QueryDBTask().execute();
		dialog = ProgressDialog.show(this, "", "Computing. Please wait...", true);
	}

    class QueryDBTask extends AsyncTask<Void, Integer, Void> {
    	Cursor cur;

    	protected Void doInBackground(Void... params) {
        	cur = db.loadContents();
        	return null;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

		protected void onPostExecute(Void result) {
            int[] viewIDs = new int[]{R.id.contentsitem};
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(context, R.layout.contentsitem, cur, db.contentsDataColumns, viewIDs, 0);
        	contents.setAdapter(adapter);
			dialog.dismiss();
        }
    }

}
