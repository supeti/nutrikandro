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

import org.nutrika.DatabaseIf.ContentsOf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class Foods extends Activity {
	Context context = this;
	DatabaseIf db = DatabaseIf.INSTANCE;
	Spinner foodGroupSpin;
	ListView foods;
	SimpleCursorAdapter foodsCA;
	EditText foodLike;
	MenuItem addToproduct;
	Cursor foodGroupsCur, foodsCur;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.foods);
		Cursor foodGroupsCur = db.loadFoodGroups();
		int[] viewIDs = new int[] { android.R.id.text1 };
		final SimpleCursorAdapter ca = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, foodGroupsCur,
				db.foodGroupsDataColumns, viewIDs, 0);
		ca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner foodGroupSpin = (Spinner) findViewById(R.id.foodGroup);
		foodGroupSpin.setAdapter(ca);
		foodGroupSpin.setOnItemSelectedListener(new FoodGroupCallBack());
		foodLike = (EditText) findViewById(R.id.foodLike);
		ImageButton ib = (ImageButton) findViewById(R.id.selectFood);
		ib.setOnClickListener(new SelectFoodCallBack());
		foodsCur = db.loadFoods();
		viewIDs = new int[] { android.R.id.text1 };
		foodsCA = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, foodsCur, db.foodDescDataColumns, viewIDs, 0);
		foods = (ListView) findViewById(R.id.foods);
		foods.setAdapter(foodsCA);
    	registerForContextMenu(foods);
		Button button = (Button) findViewById(R.id.settings);
		button.setOnClickListener(new SettingsCallBack());
		button = (Button) findViewById(R.id.products);
		button.setOnClickListener(new ProductsCallBack());
		button = (Button) findViewById(R.id.plan);
		button.setOnClickListener(new PlanCallBack());
	}

	public void onResume() {
		super.onResume();
	}
	
	public void onDestroy() {
		foodsCur.close();
		super.onDestroy();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	  super.onCreateContextMenu(menu, v, menuInfo);
	  MenuInflater inflater = getMenuInflater();
	  inflater.inflate(R.menu.foods, menu);
	  if (db.productId != 0) menu.findItem(R.id.addToProduct).setEnabled(true);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Intent intent;
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		db.foodItemId = info.id;
		db.foodDesc = ((TextView) info.targetView).getText();
		switch (item.getItemId()) {
		case R.id.showFoodContents:
			db.contentsOf = ContentsOf.FOOD;
			intent = new Intent(context, Contents.class);
			startActivity(intent);
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
			db.foodGroupId = id;
			foodsCur = db.loadFoods();
			foodsCA.changeCursor(foodsCur);
			foodLike.setText("");
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	class SelectFoodCallBack implements OnClickListener {
		public void onClick(View v) {
			foodsCur = db.loadFoodsLike(foodLike.getText().toString());
			foodsCA.changeCursor(foodsCur);
		}
	}

	class ShowFoodContentsCallBack implements OnClickListener {
		public void onClick(View v) {
			db.contentsOf = ContentsOf.FOOD;
			Intent intent = new Intent(context, Contents.class);
			startActivity(intent);
		}
	}

	class AddToProductCallBack implements OnClickListener {
		public void onClick(View v) {
			db.addIngredient(100.0);
			setResult(RESULT_OK);
			finish();
		}
	}

	class FoodItemCallBack implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			db.foodItemId = id;
			db.foodDesc = ((TextView) view).getText();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

    class SettingsCallBack implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent(context, Settings.class);
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
