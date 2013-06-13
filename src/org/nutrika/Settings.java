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
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class Settings extends Activity {
	Context context = this;
	DatabaseIf db = DatabaseIf.INSTANCE;
	SharedPreferences settings;
	Spinner lifeStageGroup;
	TextView ageTV;
	SeekBar agesb;
	String ageunit = "-year old";
	TextView weightTV;
	SeekBar weightsb;
	EditText daysET;
	long lsg, age, weight;
	int minages[] = {0, 1, 9, 9, 14, 14};
	int maxagesb[] = {12, 8, 92, 92, 36, 36};
	boolean first;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		ArrayAdapter<CharSequence> adapter;

		lifeStageGroup = (Spinner) findViewById(R.id.lifeStageGroupSpin);
	    adapter = ArrayAdapter.createFromResource(this, R.array.lifestagegroups, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    lifeStageGroup.setAdapter(adapter);
	    lifeStageGroup.setOnItemSelectedListener(new LifeStageGroupCallBack());
	    
	    ageTV = (TextView) findViewById(R.id.agetv);
	    agesb = (SeekBar) findViewById(R.id.agesb);
	    agesb.setOnSeekBarChangeListener(new AgeSBCallBack());
	    weightTV = (TextView) findViewById(R.id.eaterweight);
	    weightsb = (SeekBar) findViewById(R.id.weightSB);
	    weightsb.setOnSeekBarChangeListener(new WeightSBCallBack());
	    daysET = (EditText) findViewById(R.id.days);
	    
		lsg = db.lifeStageGroupId - 1;
		age = db.age;
		weight = db.weight;
	    lifeStageGroup.setSelection((int) lsg);
	    ageTV.setText(agetext((int) age));
	    agesb.setProgress(age2progress(age,lsg));
	    agesb.setMax(maxagesb[(int) lsg]);
	    weightTV.setText("Weight: " + String.valueOf(weight) + "kg = " + String.valueOf(weight*2) + "lb");
	    weightsb.setProgress((int) weight - 1);
	    weightsb.setMax(120);
	    daysET.setText(String.valueOf(db.days));

		Button button = (Button) findViewById(R.id.settingsOK);
		button.setOnClickListener(new OKCallBack());
		button = (Button) findViewById(R.id.settingsCancel);
		button.setOnClickListener(new CancelCallBack());

	}

	class LifeStageGroupCallBack implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			Log.d("id=", String.valueOf(id));
			Log.d("pos=", String.valueOf(position));
			Log.d("db.lifeStageGroupId=", String.valueOf(db.lifeStageGroupId));
			if (lsg != id) {
				lsg = id;
				if (0 == lsg) ageunit = "-month old";
				else ageunit = "-year old";
				int max = maxagesb[(int) lsg];
			    agesb.setMax(max);
			    int progress = age2progress(age, lsg);
			    if (0>progress || progress>=max) progress = max / 3;
			    agesb.setProgress(progress);
				ageTV.setText(agetext(progress2age(progress, lsg)));
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
	

	class AgeSBCallBack implements OnSeekBarChangeListener {
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			age = progress2age(progress, lsg);
			ageTV.setText(agetext(age));
		}
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	}

	class WeightSBCallBack implements OnSeekBarChangeListener {
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			weight = progress + 1;
		    weightTV.setText("Weight: " + String.valueOf(weight) + "kg = " + String.valueOf(weight*2) + "lb");
		}
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	}
	
	private int age2progress(long age, long lsg) {
		return (int) age - minages[(int) lsg];
	}

	private int progress2age(int progress, long lsg) {
		return progress + minages[(int) lsg];
	}
	
	private String agetext(long age) {
		return "Age: " + String.valueOf(age) + ageunit;
	}

    class OKCallBack implements OnClickListener {
		public void onClick(View v) {
			db.lifeStageGroupId = lsg + 1;
			db.age = age;
			db.weight = weight;
			db.days = Long.valueOf(daysET.getText().toString());
			db.savePreferences();
			setResult(RESULT_OK);
			finish();
		}
	}

    class CancelCallBack implements OnClickListener {
		public void onClick(View v) {
			setResult(RESULT_CANCELED);
			finish();
		}
	}

}
