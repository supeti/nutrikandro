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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FrontPage extends Activity {
	Context context = this;
	DatabaseIf db = DatabaseIf.INSTANCE;
	SharedPreferences settings;
	String foodGroup;
	ProgressBar pb;

	private final String PREF_LSG = "life stage group";
	private final String PREF_AGE = "age";
	private final String PREF_WEIGHT = "weight";
	private final int REQUEST_SETTING = 1;

	public void onDBReady() {
        db.open();
		setContentView(R.layout.frontpage);
		Button button = (Button) findViewById(R.id.settings);
		button.setOnClickListener(new SettingsCallBack());
		button = (Button) findViewById(R.id.foods);
		button.setOnClickListener(new FoodsCallBack());
		button = (Button) findViewById(R.id.products);
		button.setOnClickListener(new ProductsCallBack());
		button = (Button) findViewById(R.id.plan);
		button.setOnClickListener(new PlanCallBack());
	}
	
	public void onDownloadReady() {
		TextView tv = (TextView) findViewById(R.id.progress_text);
		tv.setText(R.string.extractingRef);
		pb.setMax(db.REF_SIZE);
		new ExtractRefTask().execute();
	}

	public void onDownloadError(String msg) {
		throw new Error(msg);
	}

	public void onExtractError(String msg) {
		throw new Error(msg);
	}

	public void onCopyError(String msg) {
		throw new Error(msg);
	}

	private void onRefReady() {
		if (db.checkUserDB()) {
			onDBReady();
		} else {
			setContentView(R.layout.progress);
			pb = (ProgressBar) findViewById(R.id.progress);
			pb.setMax(db.USER_SIZE);
			new CopyUserDBTask().execute();
		}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    settings = getPreferences(MODE_PRIVATE);
	    db.lifeStageGroupId = settings.getLong(PREF_LSG, 2);
	    db.age = settings.getLong(PREF_AGE, 33);
	    db.weight = settings.getLong(PREF_WEIGHT, 70);
		Log.d("lifeStageGroupId=", String.valueOf(db.lifeStageGroupId));
		Log.d("age=", String.valueOf(db.age));
		Log.d("weight=", String.valueOf(db.weight));
		if (db.checkRefDB()) {
			onRefReady();
		} else {
			setContentView(R.layout.progress);
			pb = (ProgressBar) findViewById(R.id.progress);
			pb.setMax(db.REFGZ_SIZE);
			new DownloadRefTask().execute();
		}
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (REQUEST_SETTING == requestCode && RESULT_OK == resultCode) {
    		Log.d("onActivityResult RESULT_OK lsg=", String.valueOf(db.lifeStageGroupId));
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putLong(PREF_LSG, db.lifeStageGroupId);
    		editor.putLong(PREF_AGE, db.age);
    		editor.putLong(PREF_WEIGHT, db.weight);
    		editor.commit();
    	}
		super.onActivityResult(requestCode, resultCode, data);
	}

	class DownloadRefTask extends AsyncTask<Void, Integer, Void> {
    	String error = null;

    	protected Void doInBackground(Void... params) {
    		InputStream input = null;
    		FileOutputStream output = null;
    		try {
    			URLConnection ucon = new URL("http://sourceforge.net/p/nutrikandro/code/ci/5fb6cf6898ca59f8fe983d11baed923e64ab5338/tree/nutrika.db.gz?format=raw").openConnection();
    			input = ucon.getInputStream();
    			output = new FileOutputStream(db.REFGZ_NAME);
    			byte[] buffer = new byte[65536];
    			int length, progress = 0;
    			while ((length = input.read(buffer)) > 0) {
    				output.write(buffer, 0, length);
    				progress += length;
    				publishProgress(progress);
    			}
    			output.flush();
    			input.close();
    			output.close();
    		} catch (IOException e) {
    			error = e.getClass().getCanonicalName() + ": " + e.getMessage();
    		}
			return null;
        }

        protected void onProgressUpdate(Integer... progress) {
        	pb.setProgress(progress[0]);
        }

		protected void onPostExecute(Void result) {
			if (null == error)
				onDownloadReady();
			else
				onDownloadError(error);
        }
    }

    class ExtractRefTask extends AsyncTask<Void, Integer, Void> {
    	String error = null;

    	protected Void doInBackground(Void... params) {
        	GZIPInputStream gzinput = null;
    		FileOutputStream output = null;
    		try {
        		gzinput = new GZIPInputStream(new FileInputStream(db.REFGZ_NAME));
        		output = new FileOutputStream(db.REF_NAME);
    			byte[] buffer = new byte[65536];
    			int length, progress = 0;
    			progress = 0;
    			while ((length = gzinput.read(buffer)) > 0) {
    				output.write(buffer, 0, length);
    				progress += length;
    				publishProgress(progress);
    			}
    			output.flush();
    			gzinput.close();
    			output.close();
    		} catch (IOException e) {
    			error = e.getClass().getCanonicalName() + ": " + e.getMessage();
    		}
			return null;
        }

        protected void onProgressUpdate(Integer... progress) {
        	pb.setProgress(progress[0]);
        }

        protected void onPostExecute(Void result) {
        	if (null == error)
        		onRefReady();
        	else
    			onExtractError(error);
        }
    }

    class CopyUserDBTask extends AsyncTask<Void, Integer, Void> {
    	String error = null;

    	protected Void doInBackground(Void... params) {
    		InputStream input = null;
    		FileOutputStream output = null;
    		try {
    			input = getAssets().open(db.ASSET_USER_NAME); 
    			output = new FileOutputStream(db.USER_NAME);
    			byte[] buffer = new byte[65536];
    			int length, progress = 0;
    			progress = 0;
    			while ((length = input.read(buffer)) > 0) {
    				output.write(buffer, 0, length);
    				progress += length;
    				publishProgress(progress);
    			}
    			output.flush();
    			input.close();
    			output.close();
    		} catch (IOException e) {
    			Log.e(this.toString(), e.getMessage(), e);
    			error = e.getClass().getCanonicalName() + ": " + e.getMessage();
    		}
			return null;
        }

        protected void onProgressUpdate(Integer... progress) {
        	pb.setProgress(progress[0]);
        }

		protected void onPostExecute(Void result) {
			if (null == error)
				onDBReady();
			else
				onCopyError(error);
        }
    }

    class SettingsCallBack implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent(context, Settings.class);
			startActivityForResult(intent, REQUEST_SETTING);
		}
	}
	class FoodsCallBack implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent(context, Foods.class);
			startActivity(intent);
		}
	}
	class ProductsCallBack implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent(context, Products.class);
			startActivity(intent);
		}
	}
	class PlanCallBack implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent(context, Plan.class);
			startActivity(intent);
		}
	}

}