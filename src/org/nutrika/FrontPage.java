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
import java.security.KeyStore;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FrontPage extends Activity {
	DatabaseIf db = DatabaseIf.INSTANCE;
	ProgressBar pb;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db.loadPreferences(getPreferences(MODE_PRIVATE));
		if (db.checkRefDB())
			onRefReady();
		else {
			setContentView(R.layout.progress);
			pb = (ProgressBar) findViewById(R.id.progress);
			pb.setMax(db.REFGZ_SIZE);
			new DownloadRefTask().execute();
		}
	}

	private void onRefReady() {
		if (db.checkUserDB())
			onDBReady();
		else {
			setContentView(R.layout.progress);
			pb = (ProgressBar) findViewById(R.id.progress);
			pb.setMax(db.USER_SIZE);
			new CopyUserDBTask().execute();
		}
	}

	public void onDBReady() {
		db.open();
		setContentView(R.layout.frontpage);
		findViewById(R.id.settings).setOnClickListener(new Activator(Settings.class));
		findViewById(R.id.foods).setOnClickListener(new Activator(Foods.class));
		findViewById(R.id.products).setOnClickListener(new Activator(Products.class));
		findViewById(R.id.plan).setOnClickListener(new Activator(Plan.class));
	}

	class DownloadRefTask extends AsyncTask<Void, Integer, Void> {
		String error = null;

		protected Void doInBackground(Void... params) {
			InputStream input = null;
			FileOutputStream output = null;
			try {
				KeyStore keyStore = null;
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
				tmf.init(keyStore);
				SSLContext context = SSLContext.getInstance("TLS");
				context.init(null, tmf.getTrustManagers(), null);
				URL url = new URL("https://docs.google.com/uc?id=0B8R75YhXzNxecFlTdXVlTW1XMmc&export=download");
				HttpsURLConnection ucon = (HttpsURLConnection) url.openConnection();
				ucon.setSSLSocketFactory(context.getSocketFactory());
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
			} catch (Exception e) {
				error = e.getClass().getCanonicalName() + ": " + e.getMessage();
			}
			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
			pb.setProgress(progress[0]);
		}

		protected void onPostExecute(Void result) {
			if (null == error) {
				((TextView) findViewById(R.id.progress_text)).setText(R.string.extractingRef);
				pb.setMax(db.REF_SIZE);
				new ExtractRefTask().execute();
			} else
				throw new Error(error);
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
				throw new Error(error);
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
				throw new Error(error);
		}
	}

	class Activator implements OnClickListener {
		Class<?> c;
		
		Activator (Class<?> c) {
			this.c = c;
		}
		
		public void onClick(View v) {
			startActivity(new Intent(FrontPage.this, c));
		}
	}

}