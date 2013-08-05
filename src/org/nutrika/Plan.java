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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class Plan extends Activity {
	DatabaseIf db = DatabaseIf.INSTANCE;
	TextView title;
	ListView plan;
	SimpleCursorAdapter planCA;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plan);

		title = (TextView) findViewById(R.id.planTitle);
		findViewById(R.id.settings).setOnClickListener(new Activator(Settings.class));
		findViewById(R.id.foods).setOnClickListener(new Activator(Foods.class));
		findViewById(R.id.products).setOnClickListener(new Activator(Products.class));
		findViewById(R.id.showPlanContents).setOnClickListener(new Activator(Contents.class));

		plan = (ListView) findViewById(R.id.plandProducts);
		registerForContextMenu(plan);
	}

	public void onResume() {
		super.onResume();
		title.setText("The " + db.getDays() + "-day plan:");
		int[] viewIDs = new int[] { R.id.planItemQuantity, R.id.planItemName };
		planCA = new SimpleCursorAdapter(this, R.layout.planitem, db.loadPlan(), db.planDataColumns, viewIDs, 0);
		plan.setAdapter(planCA);
		db.contentsType = ContentsType.PLAN;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.plan, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.editPlanItem:
			db.setPlanId(info.id);
			Intent intent = new Intent(this, EditPlanItem.class);
			intent.putExtra(EditPlanItem.QUANTITY, ((TextView) info.targetView.findViewById(R.id.planItemQuantity))
					.getText().toString().replace("g", ""));
			intent.putExtra(EditPlanItem.NAME, ((TextView) info.targetView.findViewById(R.id.planItemName)).getText()
					.toString());
			startActivity(intent);
			return true;
		case R.id.removePlanItem:
			db.rmPlanItem(info.id);
			planCA.changeCursor(db.loadPlan());
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	class Activator implements OnClickListener {
		Class<?> c;
		
		Activator (Class<?> c) {
			this.c = c;
		}
		
		public void onClick(View v) {
			startActivity(new Intent(Plan.this, c));
		}
	}
}
