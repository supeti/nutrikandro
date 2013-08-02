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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class Products extends Activity {
	Context context = this;
	DatabaseIf db = DatabaseIf.INSTANCE;
	ImageButton addProductBn, selectProductBn;
	ListView products;
	SimpleCursorAdapter productsCA;
	EditText productsET;
	Button addProduct, editProduct, deleteProduct;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.products);
		productsET = (EditText) findViewById(R.id.productET);
		selectProductBn = (ImageButton) findViewById(R.id.selectProduct);
		selectProductBn.setOnClickListener(new SelectProductCallBack());
		addProductBn = (ImageButton) findViewById(R.id.addProduct);
		addProductBn.setOnClickListener(new AddProductCallBack());

		Cursor cur = db.loadProducts();
		int[] viewIDs = new int[] { android.R.id.text1 };
		productsCA = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cur, db.productsDataColumns, viewIDs, 0);
		products = (ListView) findViewById(R.id.products);
		products.setAdapter(productsCA);
		registerForContextMenu(products);
		Button button = (Button) findViewById(R.id.settings);
		button.setOnClickListener(new SettingsCallBack());
		button = (Button) findViewById(R.id.foods);
		button.setOnClickListener(new FoodsCallBack());
		button = (Button) findViewById(R.id.plan);
		button.setOnClickListener(new PlanCallBack());
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.product, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Intent intent;
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		db.setProduct(info.id, ((TextView) info.targetView).getText());
		switch (item.getItemId()) {
		case R.id.showProductContents:
			db.contentsOf = ContentsOf.PRODUCT;
			intent = new Intent(context, Contents.class);
			startActivity(intent);
			return true;
		case R.id.planProduct:
			db.planProduct();
			intent = new Intent(context, Plan.class);
			startActivity(intent);
			return true;
		case R.id.editProduct:
			intent = new Intent(context, EditProduct.class);
			startActivity(intent);
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
			String product = productsET.getText().toString();
			db.addProduct(product);
			selectProductBn.performClick();
		}
	}

	class SelectProductCallBack implements OnClickListener {
		public void onClick(View v) {
			Cursor cur = db.loadProductsLike(productsET.getText().toString());
			productsCA.changeCursor(cur);
		}
	}

	/*
	 * class ProductsCallBack implements OnItemSelectedListener { public void
	 * onItemSelected(AdapterView<?> parent, View view, int position, long id) {
	 * db.productId = id; if (null == view) db.productName = "null view"; else
	 * db.productName = ((TextView) view).getText(); }
	 * 
	 * public void onNothingSelected(AdapterView<?> parent) { db.productId = -1;
	 * db.productName = null; db.productPrice = 0.0; } }
	 */
	class SettingsCallBack implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent(context, Settings.class);
			startActivity(intent);
		}
	}

	class FoodsCallBack implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent(context, Foods.class);
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
