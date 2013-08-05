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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class Products extends Activity {
	DatabaseIf db = DatabaseIf.INSTANCE;
	ImageButton selectProductBn;
	SimpleCursorAdapter productsCA;
	EditText productsET;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.products);
		productsET = (EditText) findViewById(R.id.productET);
		selectProductBn = (ImageButton) findViewById(R.id.selectProduct);
		selectProductBn.setOnClickListener(new SelectProductCallBack());
		findViewById(R.id.addProduct).setOnClickListener(new AddProductCallBack());

		int[] viewIDs = new int[] { android.R.id.text1 };
		productsCA = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, db.loadProducts(),
				db.productsDataColumns, viewIDs, 0);
		ListView products = (ListView) findViewById(R.id.products);
		products.setAdapter(productsCA);
		registerForContextMenu(products);
		findViewById(R.id.settings).setOnClickListener(new Activator(Settings.class));
		findViewById(R.id.foods).setOnClickListener(new Activator(Foods.class));
		findViewById(R.id.plan).setOnClickListener(new Activator(Plan.class));
	}

	@Override
	public void onResume() {
		super.onResume();
		db.contentsType = ContentsType.PRODUCT;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.product, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		db.setProduct(info.id, ((TextView) info.targetView).getText());
		switch (item.getItemId()) {
		case R.id.showProductContents:
			startActivity(new Intent(Products.this, Contents.class));
			return true;
		case R.id.planProduct:
			db.planProduct();
			startActivity(new Intent(Products.this, Plan.class));
			return true;
		case R.id.editProduct:
			startActivity(new Intent(Products.this, EditProduct.class));
			return true;
		case R.id.deleteProduct:
			db.deleteProduct();
			selectProductBn.performClick();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	class AddProductCallBack implements OnClickListener {
		public void onClick(View v) {
			db.addProduct(productsET.getText().toString());
			productsET.setText("");
			selectProductBn.performClick();
			startActivity(new Intent(Products.this, EditProduct.class));
		}
	}

	class SelectProductCallBack implements OnClickListener {
		public void onClick(View v) {
			productsCA.changeCursor(db.loadProductsLike(productsET.getText().toString()));
		}
	}

	class Activator implements OnClickListener {
		Class<?> c;
		
		Activator (Class<?> c) {
			this.c = c;
		}
		
		public void onClick(View v) {
			startActivity(new Intent(Products.this, c));
		}
	}
}
