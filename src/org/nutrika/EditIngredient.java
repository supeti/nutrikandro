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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class EditIngredient extends Activity {
	static final float OZ2G = 28.35f;
	public static final String AMOUNT = "amount";
	public static final String DESCRIPTION = "description";
	DatabaseIf db = DatabaseIf.INSTANCE;
	Spinner units;
	EditText amount;
	long unit = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editingredient);
		Bundle extras = getIntent().getExtras();
		amount = (EditText) findViewById(R.id.editIngredientWeight);
		amount.setText(extras.getString(AMOUNT));
		((TextView) findViewById(R.id.editIngredientTitle)).setText(extras.getString(DESCRIPTION));
		units = (Spinner) findViewById(R.id.editIngredientUnit);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.ingredientUnits,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		units.setAdapter(adapter);
		units.setOnItemSelectedListener(new UnitsCallBack());

		findViewById(R.id.editIngredientOK).setOnClickListener(new OKCallBack());
		findViewById(R.id.editIngredientCancel).setOnClickListener(new CancelCallBack());
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
			db.updateIngredient(Float.valueOf(amount.getText().toString()) * (0 == unit ? 1 : OZ2G));
			finish();
		}
	}

	class CancelCallBack implements OnClickListener {
		public void onClick(View v) {
			finish();
		}
	}

}
