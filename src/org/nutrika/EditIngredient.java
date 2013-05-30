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
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class EditIngredient extends Activity {
	Context context = this;
	DatabaseIf db = DatabaseIf.INSTANCE;
	TextView title;
	Spinner units;
	ListView ingredients;
	EditText amount;
	Button ok, cancel;
	long unit = 0;
	float oz2g = 28.35f;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
    	setContentView(R.layout.editingredient);
    	title = (TextView) findViewById(R.id.editIngredientTitle);
    	units = (Spinner) findViewById(R.id.editIngredientUnit);
    	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.ingredientUnits, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    units.setAdapter(adapter);
	    units.setOnItemSelectedListener(new UnitsCallBack());

	    amount = (EditText) findViewById(R.id.editIngredientWeight);
    	ok = (Button) findViewById(R.id.editIngredientOK);
    	ok.setOnClickListener(new OKCallBack());
    	cancel = (Button) findViewById(R.id.editIngredientCancel);
    	cancel.setOnClickListener(new CancelCallBack());
    	
	}

	@Override
	protected void onResume() {
		super.onResume();
		Cursor cur = db.ingredient();
		if (cur.moveToFirst()) {
			title.setText(cur.getString(0));
			amount.setText(cur.getString(1));
		}
		cur.close();
	}

	class UnitsCallBack implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			unit = id;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
	
	class OKCallBack implements OnClickListener {
		public void onClick(View v) {
			float q;
			if (0 == unit) q = Float.valueOf(amount.getText().toString());
			else q = Float.valueOf(amount.getText().toString()) * oz2g;
			db.updateIngredient(q);
			finish();
		}
	}

	class CancelCallBack implements OnClickListener {
		public void onClick(View v) {
			finish();
		}
	}

}
