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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class EditProduct extends Activity {
	DatabaseIf db = DatabaseIf.INSTANCE;
	TextView title;
	EditText price;
	ListView ingredients;
	SimpleCursorAdapter ingredientsCA;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editproduct);
		ingredients = (ListView) findViewById(R.id.ingredients);
		registerForContextMenu(ingredients);
		title = (TextView) findViewById(R.id.editProductTitle);
		price = (EditText) findViewById(R.id.editProductPrice);
		findViewById(R.id.savePrice).setOnClickListener(new SavePriceCallBack());
		findViewById(R.id.addProductItem).setOnClickListener(new Activator(Foods.class));
		findViewById(R.id.showEditProductContents).setOnClickListener(new Activator(Contents.class));
		findViewById(R.id.finishEditProduct).setOnClickListener(new Finisher());
	}

	@Override
	public void onResume() {
		super.onResume();
		title.setText(db.getProductName());
		price.setText(db.loadProductPrice());
		int[] viewIDs = new int[] { R.id.ingredientAmount, R.id.ingredientDescription };
		ingredientsCA = new SimpleCursorAdapter(this, R.layout.ingredientitem, db.loadIngredients(),
				db.ingredientsDataColumns, viewIDs, 0);
		ingredients.setAdapter(ingredientsCA);
		db.contentsType = ContentsType.PRODUCT;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.editproduct, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.editIngredient:
			db.setIngredientId(info.id);
			Intent intent = new Intent(this, EditIngredient.class);
			intent.putExtra(EditIngredient.AMOUNT, ((TextView) info.targetView.findViewById(R.id.ingredientAmount)).getText()
					.toString());
			intent.putExtra(EditIngredient.DESCRIPTION, ((TextView) info.targetView.findViewById(R.id.ingredientDescription)).getText()
					.toString());
			startActivity(intent);
			return true;
		case R.id.removeIngredient:
			db.rmIngredient(info.id);
			ingredientsCA.changeCursor(db.loadIngredients());
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	class SavePriceCallBack implements OnClickListener {
		public void onClick(View v) {
			db.updateproduct(price.getText().toString());
		}
	}

	class Finisher implements OnClickListener {
		public void onClick(View v) {
			finish();
		}
	}

	class Activator implements OnClickListener {
		Class<?> c;

		Activator(Class<?> c) {
			this.c = c;
		}

		public void onClick(View v) {
			startActivity(new Intent(EditProduct.this, c));
		}
	}
}
