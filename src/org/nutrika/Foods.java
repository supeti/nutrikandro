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

import org.nutrika.DatabaseIf.ContentsType;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class Foods extends Activity {
	DatabaseIf db = DatabaseIf.INSTANCE;
	SimpleCursorAdapter foodsCA;
	EditText foodLike;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.foods);
		int[] viewIDs = new int[] { android.R.id.text1 };
		final SimpleCursorAdapter ca = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, db.loadFoodGroups(),
				db.foodGroupsDataColumns, viewIDs, 0);
		ca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner foodGroupSpin = (Spinner) findViewById(R.id.foodGroup);
		foodGroupSpin.setAdapter(ca);
		foodGroupSpin.setOnItemSelectedListener(new FoodGroupCallBack());
		foodLike = (EditText) findViewById(R.id.foodLike);
		((ImageButton) findViewById(R.id.selectFood)).setOnClickListener(new SelectFoodCallBack());
		viewIDs = new int[] { android.R.id.text1 };
		foodsCA = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, db.loadFoods(), db.foodDescDataColumns, viewIDs, 0);
		ListView foods = (ListView) findViewById(R.id.foods);
		foods.setAdapter(foodsCA);
    	registerForContextMenu(foods);
		findViewById(R.id.settings).setOnClickListener(new Activator(Settings.class));
		findViewById(R.id.products).setOnClickListener(new Activator(Products.class));
		findViewById(R.id.plan).setOnClickListener(new Activator(Plan.class));
	}

	@Override
	public void onResume() {
		super.onResume();
		db.contentsType = ContentsType.FOOD;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	  super.onCreateContextMenu(menu, v, menuInfo);
	  getMenuInflater().inflate(R.menu.foods, menu);
	  if (db.isProductSelected()) menu.findItem(R.id.addToProduct).setEnabled(true);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		db.setFoodItem(info.id, ((TextView) info.targetView).getText());
		switch (item.getItemId()) {
		case R.id.showFoodContents:
			startActivity(new Intent(this, Contents.class));
			return true;
		case R.id.addToProduct:
			db.addIngredient(100.0);
			setResult(RESULT_OK);
			finish();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

    class FoodGroupCallBack implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			db.setFoodGroupId(id);
			foodsCA.changeCursor(db.loadFoods());
			foodLike.setText("");
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	class SelectFoodCallBack implements OnClickListener {
		public void onClick(View v) {
			foodsCA.changeCursor(db.loadFoodsLike(foodLike.getText().toString()));
		}
	}

	class Activator implements OnClickListener {
		Class<?> c;
		
		Activator (Class<?> c) {
			this.c = c;
		}
		
		public void onClick(View v) {
			startActivity(new Intent(Foods.this, c));
		}
	}
}
