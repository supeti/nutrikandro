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

import java.io.File;
import java.io.IOException;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;

public enum DatabaseIf {
	INSTANCE;
	
	public enum ContentsOf {
		FOOD, PRODUCT, PLAN
	} ContentsOf contentsOf;

	public String DB_PATH, REF_NAME, REFGZ_NAME, ASSET_USER_NAME, USER_NAME;
	public int REF_SIZE = 38792192;
	public int REFGZ_SIZE = 6629153;
	public int USER_SIZE = 8192;
	private SQLiteDatabase db;

	public long lifeStageGroupId = 3;
	public long age = 33;
	public long weight = 70;
	public long days = 7;
	public long foodGroupId = 100;
	private String attachUserDB;
	public final String[] foodGroupsDataColumns = { "description" };
	private final String foodGroupsQS = "SELECT _id,description FROM food_groups ORDER BY description;";
	public final String[] foodDescDataColumns = { "long_desc" };
	private final String foodsQS = "SELECT _id,long_desc FROM food_desc WHERE fdgrp=? ORDER BY long_desc;";
	private final String foodsLikeQS = "SELECT _id,long_desc FROM food_desc WHERE fdgrp=? AND long_desc like ? ORDER BY long_desc;";
	public long foodItemId = 0;
	public CharSequence foodDesc;
	public final String[] contentsDataColumns = { "data" };
	private final String foodContentsQS = "SELECT nutr_data.nutr_no AS _id,nutrdesc||':'||round(nutr_val,dec)||units " +
		"||ifnull(', EAR:'||round(nutr_val/ear*100,1)||'%','')||ifnull(', RDA:'||round(nutr_val/rda*100,1)||'%','') " +
		"||ifnull(', AI:'||round(nutr_val/ai*100,1)||'%','')||ifnull(', UL:'||round(nutr_val/ul*100,1)||'%','') AS data " +
		"FROM nutr_data JOIN nutr_def ON nutr_data.nutr_no=nutr_def._id LEFT OUTER JOIN " +
		"(SELECT nutr_no,lsg,min_age,max_age,ear,rda,ai,ul FROM dri UNION " +
		"SELECT nutr_no,lsg,min_age,max_age,ear*weight,rda*weight,ai*weight,ul*weight FROM driperkg " +
		"JOIN (SELECT ? as weight)) AS dri USING (nutr_no) JOIN (SELECT ? AS age, ? AS cur_lsg) " +
		"WHERE ndb_no=? AND (dri.nutr_no IS NULL OR lsg=cur_lsg AND min_age<=age AND age<max_age) ORDER BY sr_order;";
	public long productId = 0;
	public CharSequence productName;
	public double productPrice = 0.0;
	public final String[] productsDataColumns = { "name" };
	private final String productsQS = "SELECT _id,name FROM user.products ORDER BY name;";
	private final String productsLikeQS = "SELECT _id,name,price FROM user.products WHERE name like ? ORDER BY name;";
	private final String productContentsQS = "SELECT nutr_data.nutr_no AS _id,nutrdesc||':'||Round(Sum(nutr_val*amount*0.01),dec)" + 
	"||units||ifnull(', EAR:'||Round(Sum(nutr_val*amount/ear),1)||'%','')||ifnull(', RDA:'||Round(Sum(nutr_val*amount/rda),1)||'%','') " +
	"||ifnull(', AI:'||Round(Sum(nutr_val*amount/ai),1)||'%','')||ifnull(', UL:'||Round(Sum(nutr_val*amount/ul),1)||'%','')" +
	"||ifnull(', PD:'||coalesce(Round(price*100/Sum(nutr_val*amount/rda),2),Round(price*100/Sum(nutr_val*amount/ai),2)),'') AS data " +
	"FROM nutr_data JOIN nutr_def ON nutr_data.nutr_no=nutr_def._id JOIN ingredients USING(ndb_no) " + 
	"JOIN products ON ingredients.product=products._id LEFT OUTER JOIN " +
	"(SELECT nutr_no,lsg,min_age,max_age,ear,rda,ai,ul FROM dri UNION " +
	"SELECT nutr_no,lsg,min_age,max_age,ear*weight,rda*weight,ai*weight,ul*weight FROM driperkg " +
	"JOIN (SELECT ? as weight)) AS dri USING (nutr_no) JOIN (SELECT ? AS age, ? AS cur_lsg) " +
	"WHERE products._id=? AND (dri.nutr_no IS NULL OR lsg=cur_lsg AND min_age<=age AND age<max_age) " + 
	"GROUP BY nutr_data.nutr_no,units,nutrdesc,sr_order ORDER BY sr_order;";
	public final String[] ingredientDataColumns = { "food", "amount" };
	private final String ingredientQS = "SELECT long_desc AS food,amount FROM user.ingredients AS i " +
		"JOIN food_desc AS f ON i.ndb_no=f._id WHERE i._id=?;";
	public final String[] ingredientsDataColumns = { "ingredient" };
	private final String ingredientsQS = "SELECT i._id,amount||'g '||long_desc AS ingredient " + 
		"FROM user.ingredients AS i JOIN food_desc ON i.ndb_no=food_desc._id WHERE product=? ORDER BY long_desc;";
	public long ingredientId = 0, planId = 0;
	public final String[] planDataColumns = { "quantity", "name" };
	private final String planQS = "SELECT plan._id,plan.quantity,products.name " + 
		"FROM user.plan JOIN user.products ON plan.product=products._id " + 
		"ORDER BY products.name;";
	private final String planContentsQS = "SELECT nutr_data.nutr_no AS _id,nutrdesc||':'||" +
		"Round(Sum(nutr_val*amount*0.01*quantity),dec)||units||ifnull(', EAR:'||" + 
		"Round(Sum(nutr_val*amount*quantity/(ear*days)),1)||'%','')||ifnull(', RDA:'||" + 
		"Round(Sum(nutr_val*amount*quantity/(rda*days)),1)||'%','') " +
		"||ifnull(', AI:'||Round(Sum(nutr_val*amount*quantity/(ai*days)),1)||'%','')||" + 
		"ifnull(', UL:'||Round(Sum(nutr_val*amount*quantity/(ul*days)),1)||'%','')" +
		"||ifnull(', PD:'||coalesce(Round(Sum(price*quantity*100*(rda*days))/Sum(nutr_val*amount*quantity),2)," +
		"Round(Sum(price*quantity*100*(ai*days))/Sum(nutr_val*amount*quantity),2)),'') AS data " +
 		"FROM plan JOIN products ON products._id=plan.product JOIN ingredients ON ingredients.product=products._id " + 
		"JOIN nutr_data USING(ndb_no) JOIN nutr_def ON nutr_data.nutr_no=nutr_def._id LEFT OUTER JOIN " +
		"(SELECT nutr_no,lsg,min_age,max_age,ear,rda,ai,ul FROM dri UNION " +
		"SELECT nutr_no,lsg,min_age,max_age,ear*weight,rda*weight,ai*weight,ul*weight FROM driperkg " +
		"JOIN (SELECT ? as weight)) AS dri USING (nutr_no) JOIN (SELECT ? AS age, ? AS cur_lsg, " + 
		"? AS days) WHERE dri.nutr_no IS NULL OR lsg=cur_lsg AND min_age<=age AND age<max_age " + 
		"GROUP BY nutr_data.nutr_no,units,nutrdesc,sr_order ORDER BY sr_order;";
	Cursor foodsCur, foodsLikeCur;

	private DatabaseIf() {
		try {
			DB_PATH = Environment.getExternalStorageDirectory().getCanonicalPath() + "/nutrika/";
			REF_NAME = DB_PATH + "nutrika.db";
			REFGZ_NAME = REF_NAME + ".gz";
			ASSET_USER_NAME = "user.db";
			USER_NAME = DB_PATH + ASSET_USER_NAME;
			attachUserDB = "ATTACH \"" + USER_NAME + "\" AS user;";
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean checkRefDB() {
		try {
			File dir = new File(DB_PATH);
			if (!dir.isDirectory()) {
				if (!dir.mkdir())
					throw new Error("Creation of nutrika directory failed.");
				return false;
			}
			File dbfile = new File(REF_NAME);
			if (dbfile.length() != REF_SIZE)
				return false;
			SQLiteDatabase db = SQLiteDatabase.openDatabase(REF_NAME, null, SQLiteDatabase.OPEN_READONLY);
			db.close();
			return true;
		} catch (SQLiteException e) {
			return false;
		}
	}

	public boolean checkUserDB() {
		try {
			SQLiteDatabase db = SQLiteDatabase.openDatabase(USER_NAME, null, SQLiteDatabase.OPEN_READONLY);
			db.close();
			return true;
		} catch (SQLiteException e) {
			return false;
		}
	}

	public DatabaseIf open() throws SQLException {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			try {
				db = SQLiteDatabase.openDatabase(REF_NAME, null, SQLiteDatabase.OPEN_READWRITE);
				db.execSQL(attachUserDB);
			} catch (SQLiteException e) {
				throw new Error("DB error:" + e.getMessage());
			}
		} else {
			throw new Error("SD card is not mounted.");
		}
		return this;
	}


	public void close() {
		db.close();
	}

	public Cursor foodGroups() {
		return db.rawQuery(foodGroupsQS, null);
	}

	public Cursor foods() {
		return db.rawQuery(foodsQS, new String[] { String.valueOf(foodGroupId) });
	}

	public Cursor foodsLike(String like) {
		return db.rawQuery(foodsLikeQS, new String[] { String.valueOf(foodGroupId), "%" + like + "%" });
	}

	public CharSequence contentsTitle() {
		switch (contentsOf) {
		case FOOD: 
			return new String("100g of " + foodDesc + " contains:");
		case PRODUCT:
			return new String("one " + productName + " contains:");
		case PLAN:
			return new String("The " + days + "-day plan contains:");
		}
		return null;
	}
	
	public Cursor contents() {
		switch (contentsOf) {
		case FOOD: 
			return db.rawQuery(foodContentsQS, new String[] { String.valueOf(weight), String.valueOf(age),
					String.valueOf(lifeStageGroupId), String.valueOf(foodItemId) });
		case PRODUCT:
			return db.rawQuery(productContentsQS, new String[] { String.valueOf(weight), String.valueOf(age),
					String.valueOf(lifeStageGroupId), String.valueOf(productId) });
		case PLAN:
			return db.rawQuery(planContentsQS, new String[] { String.valueOf(weight), String.valueOf(age),
					String.valueOf(lifeStageGroupId), String.valueOf(days)});
		}
		return null;
	}

	public void addProduct(String product) {
		ContentValues cv = new ContentValues();
		cv.put("name", product);
		productId = db.insertWithOnConflict("user.products", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
	}

	public void deleteProduct() {
		productId = db.delete("user.products", "_id=?", new String[] { String.valueOf(productId) });
	}

	public Cursor products() {
		return db.rawQuery(productsQS, null);
	}

	public Cursor productsLike(String like) {
		return db.rawQuery(productsLikeQS, new String[] { "%" + like + "%" });
	}

	public double productPrice() {
		double price;
		Cursor cur = db.rawQuery("select price from user.products where _id=?", new String[] { String.valueOf(productId) });
		cur.moveToFirst();
		price = cur.getDouble(0);
		cur.close();
		return price;
	}
	
	public void updateproduct(Double price) {
		ContentValues cv = new ContentValues();
		cv.put("price", price);
		db.update("user.products", cv, "_id=?", new String[] { String.valueOf(productId) });
		productPrice = price;
	}

	public Cursor ingredient() {
		return db.rawQuery(ingredientQS, new String[] { String.valueOf(ingredientId) });
	}

	public Cursor ingredients() {
		return db.rawQuery(ingredientsQS, new String[] { String.valueOf(productId) });
	}
	
	public void addIngredient(double amount) {
		ContentValues cv = new ContentValues();
		cv.put("product", productId);
		cv.put("ndb_no", foodItemId);
		cv.put("amount", String.valueOf(amount));
		db.insertWithOnConflict("user.ingredients", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
	}

	public void rmIngredient(long id) {
		db.delete("user.ingredients", "_id=?", new String[] { String.valueOf(id) });
	}

	public void updateIngredient(float amount) {
		ContentValues cv = new ContentValues();
		cv.put("amount", amount);
		db.update("user.ingredients", cv, "_id=?", new String[] { String.valueOf(ingredientId) });
	}

	public void planProduct() {
		ContentValues cv = new ContentValues();
		cv.put("product", productId);
		cv.put("quantity", 1.0f);
		db.insertWithOnConflict("user.plan", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
	}

	public void updateplan(String quantity) {
		ContentValues cv = new ContentValues();
		cv.put("quantity", quantity);
		db.update("user.plan", cv, "_id=?", new String[] { String.valueOf(planId) });
	}

	public Cursor plan() {
		return db.rawQuery(planQS, new String[] { });
	}

	public void rmPlanItem(long id) {
		productId = db.delete("user.plan", "_id=?", new String[] { String.valueOf(id) });
	}
}
