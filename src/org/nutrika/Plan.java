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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class Plan extends Activity {
	Context context = this;
	DatabaseIf db = DatabaseIf.INSTANCE;
	TextView title;
	Button products, showContents;
	ListView plan;
	SimpleCursorAdapter planCA;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plan);

		title = (TextView) findViewById(R.id.planTitle);
		Button button = (Button) findViewById(R.id.settings);
		button.setOnClickListener(new SettingsCallBack());
		button = (Button) findViewById(R.id.foods);
		button.setOnClickListener(new FoodsCallBack());
		button = (Button) findViewById(R.id.products);
		button.setOnClickListener(new ProductsCallBack());
		showContents = (Button) findViewById(R.id.showPlanContents);
		showContents.setOnClickListener(new ShowContentsCallBack());

		plan = (ListView) findViewById(R.id.plandProducts);
		registerForContextMenu(plan);
	}

	public void onResume() {
		super.onResume();
		title.setText("The " + db.getDays() + "-day plan:");
		Cursor cur = db.loadPlan();
		int[] viewIDs = new int[] { R.id.planItemQuantity, R.id.planItemName };
		planCA = new SimpleCursorAdapter(this, R.layout.planitem, cur, db.planDataColumns, viewIDs, 0);
		plan.setAdapter(planCA);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.plan, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.editPlanItem:
			db.setPlanId(info.id);
			Intent intent = new Intent(context, EditPlanItem.class);
			intent.putExtra(EditPlanItem.QUANTITY, ((TextView) info.targetView.findViewById(R.id.planItemQuantity))
					.getText().toString());
			intent.putExtra(EditPlanItem.NAME, ((TextView) info.targetView.findViewById(R.id.planItemName)).getText()
					.toString());
			startActivity(intent);
			return true;
		case R.id.removePlanItem:
			db.rmPlanItem(info.id);
			Cursor cur = db.loadPlan();
			((SimpleCursorAdapter) plan.getAdapter()).changeCursor(cur);
			return true;
		default:
			return super.onContextItemSelected(item);
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

	class ShowContentsCallBack implements OnClickListener {
		public void onClick(View v) {
			db.contentsOf = ContentsOf.PLAN;
			Intent intent = new Intent(context, Contents.class);
			startActivity(intent);
		}
	}

	class FoodsCallBack implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent(context, Foods.class);
			startActivity(intent);
		}
	}
}
