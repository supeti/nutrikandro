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
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.EditText;

public class EditProduct extends Activity {
	Context context = this;
	DatabaseIf db = DatabaseIf.INSTANCE;
	ListView ingredients;
	TextView title;
	EditText price;
	Button addItem, showContents;
	double priceval;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
    	setContentView(R.layout.editproduct);
    	ingredients = (ListView) findViewById(R.id.ingredients);
    	registerForContextMenu(ingredients);
    	title = (TextView) findViewById(R.id.editProductTitle);
    	price = (EditText) findViewById(R.id.editProductPrice);
    	price.setOnFocusChangeListener(new SavePriceCallBack());
    	addItem = (Button) findViewById(R.id.addProductItem);
    	addItem.setOnClickListener(new AddProductItemCallBack());
    	showContents = (Button) findViewById(R.id.showEditProductContents);
    	showContents.setOnClickListener(new ShowContentsCallBack());
	}

	@Override
	public void onResume() {
		super.onResume();
		title.setText(db.productName);
		priceval = db.productPrice();
		price.setText(Double.toString(priceval));
    	Cursor cur = db.ingredients();
        int[] viewIDs = new int[]{R.id.contentsitem};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.contentsitem, cur, db.ingredientsDataColumns, viewIDs, 0);
        ingredients.setAdapter(adapter);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	  super.onCreateContextMenu(menu, v, menuInfo);
	  MenuInflater inflater = getMenuInflater();
	  inflater.inflate(R.menu.editproduct, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	  switch (item.getItemId()) {
	  case R.id.editIngredient:
		  db.ingredientId = info.id;
		  Log.d("ingredientId=", String.valueOf(info.id));
		  Intent intent = new Intent(context, EditIngredient.class);
		  startActivity(intent);
	    return true;
	  case R.id.removeIngredient:
		db.rmIngredient(info.id);
		Cursor cur = db.ingredients();
		((SimpleCursorAdapter) ingredients.getAdapter()).changeCursor(cur);
	    return true;
	  default:
	    return super.onContextItemSelected(item);
	  }
	}
	
	class AddProductItemCallBack implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent(context, Foods.class);
			startActivity(intent);
		}
	}

	class ShowContentsCallBack implements OnClickListener {
		public void onClick(View v) {
			db.contentsOf = ContentsOf.PRODUCT;
			Intent intent = new Intent(context, Contents.class);
			startActivity(intent);
		}
	}

	class SavePriceCallBack implements OnFocusChangeListener {
		public void onFocusChange(View editor, boolean hasFocus) {
			double p = Double.valueOf(price.getText().toString()).doubleValue();
			if (!hasFocus && priceval!=p) db.updateproduct(p);
		}
	}
	
}
