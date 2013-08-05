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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;

public enum DatabaseIf {
	INSTANCE;
	private SharedPreferences settings;
	private final String PREF_LSG = "life stage group";
	private final String PREF_AGE = "age";
	private final String PREF_WEIGHT = "weight";
	private final String PREF_DAYS = "days";

	public enum ContentsType {
		FOOD, PRODUCT, PLAN
	}

	ContentsType contentsType;

	private String DB_PATH;
	public String REF_NAME, REFGZ_NAME, ASSET_USER_NAME, USER_NAME;
	public int REF_SIZE = 38792192;
	public int REFGZ_SIZE = 6629153;
	public int USER_SIZE = 8192;
	private SQLiteDatabase db;

	private String attachUserDB;
	private String productPrice = "0.0";

	// Cursor foodsCur, foodsLikeCur;

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

	private long lifeStageGroupId = 3;
	private long age = 33;
	private long weight = 70;
	private String days = "7";

	public long getLifeStageGroupId() {
		return lifeStageGroupId - 1;
	}

	public long getAge() {
		return age;
	}

	public long getWeight() {
		return weight;
	}

	public String getDays() {
		return days;
	}

	public void savePreferences(long lifeStageGroupId, long age, long weight, String days) {
		this.lifeStageGroupId = lifeStageGroupId + 1;
		this.age = age;
		this.weight = weight;
		this.days = days;
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(PREF_LSG, this.lifeStageGroupId);
		editor.putLong(PREF_AGE, this.age);
		editor.putLong(PREF_WEIGHT, this.weight);
		editor.putString(PREF_DAYS, this.days);
		editor.commit();
	}

	public void loadPreferences(SharedPreferences prefs) {
		settings = prefs;
		lifeStageGroupId = settings.getLong(PREF_LSG, 3);
		age = settings.getLong(PREF_AGE, 33);
		weight = settings.getLong(PREF_WEIGHT, 70);
		days = settings.getString(PREF_DAYS, "7");
	}

	private String foodGroupId = "100";
	private int foodGroupPos = 0;

	private String foodItemId = "0";
	private CharSequence foodDesc;

	public void setFoodItem(long foodItemId, CharSequence foodDesc) {
		this.foodItemId = String.valueOf(foodItemId);
		this.foodDesc = foodDesc;
	}

	public int getFoodGroupPos() {
		return foodGroupPos;
	}

	public void setFoodGroupId(long id, int position) {
		foodGroupId = String.valueOf(id);
		foodGroupPos = position;
	}

	public final String[] foodGroupsDataColumns = { "description" };
	private final String foodGroupsQS = "SELECT _id,description FROM food_groups ORDER BY description;";

	public Cursor loadFoodGroups() {
		return db.rawQuery(foodGroupsQS, null);
	}

	private String foodsLike = "";
	public final String[] foodDescDataColumns = { "long_desc" };
	private final String foodsLikeQS = "SELECT _id,long_desc FROM food_desc WHERE fdgrp=? AND long_desc like ? ORDER BY long_desc;";

	public Cursor loadFoods() {
		return db.rawQuery(foodsLikeQS, new String[] { foodGroupId, "%" + foodsLike + "%" });
	}

	public Cursor loadFoodsLike(String like) {
		foodsLike = like;
		return db.rawQuery(foodsLikeQS, new String[] { foodGroupId, "%" + like + "%" });
	}
	
	public String getFoodsLike() {
		return foodsLike;
	}

	public CharSequence getContentsTitle() {
		switch (contentsType) {
		case FOOD:
			return new String("100g of " + foodDesc + " contains:");
		case PRODUCT:
			return new String(productName + " contains:");
		case PLAN:
			return new String("The " + days + "-day plan contains:");
		}
		return null;
	}

	public final String[] contentsDataColumns = { "data" };
	private final String foodContentsQS = "SELECT nutr_data.nutr_no AS _id,nutrdesc||':'||round(nutr_val,dec)||units "
			+ "||ifnull(', EAR:'||round(nutr_val/ear*100,1)||'%','')||ifnull(', RDA:'||round(nutr_val/rda*100,1)||'%','') "
			+ "||ifnull(', AI:'||round(nutr_val/ai*100,1)||'%','')||ifnull(', UL:'||round(nutr_val/ul*100,1)||'%','') AS data "
			+ "FROM nutr_data JOIN nutr_def ON nutr_data.nutr_no=nutr_def._id LEFT OUTER JOIN "
			+ "(SELECT nutr_no,lsg,min_age,max_age,ear,rda,ai,ul FROM dri UNION "
			+ "SELECT nutr_no,lsg,min_age,max_age,ear*weight,rda*weight,ai*weight,ul*weight FROM driperkg "
			+ "JOIN (SELECT ? as weight)) AS dri USING (nutr_no) JOIN (SELECT ? AS age, ? AS cur_lsg) "
			+ "WHERE ndb_no=? AND (dri.nutr_no IS NULL OR lsg=cur_lsg AND min_age<=age AND age<max_age) ORDER BY sr_order;";
	private final String productContentsQS = "SELECT nutr_data.nutr_no AS _id,nutrdesc||':'||Round(Sum(nutr_val*amount*0.01),dec)"
			+ "||units||ifnull(', EAR:'||Round(Sum(nutr_val*amount/ear),1)||'%','')||ifnull(', RDA:'||Round(Sum(nutr_val*amount/rda),1)||'%','') "
			+ "||ifnull(', AI:'||Round(Sum(nutr_val*amount/ai),1)||'%','')||ifnull(', UL:'||Round(Sum(nutr_val*amount/ul),1)||'%','')"
			+ "||ifnull(', P/V:'||coalesce(Round(price*100/Sum(nutr_val*amount/rda),2),Round(price*100/Sum(nutr_val*amount/ai),2)),'') AS data "
			+ "FROM nutr_data JOIN nutr_def ON nutr_data.nutr_no=nutr_def._id JOIN ingredients USING(ndb_no) "
			+ "JOIN products ON ingredients.product=products._id LEFT OUTER JOIN "
			+ "(SELECT nutr_no,lsg,min_age,max_age,ear,rda,ai,ul FROM dri UNION "
			+ "SELECT nutr_no,lsg,min_age,max_age,ear*weight,rda*weight,ai*weight,ul*weight FROM driperkg "
			+ "JOIN (SELECT ? as weight)) AS dri USING (nutr_no) JOIN (SELECT ? AS age, ? AS cur_lsg) "
			+ "WHERE products._id=? AND (dri.nutr_no IS NULL OR lsg=cur_lsg AND min_age<=age AND age<max_age) "
			+ "GROUP BY nutr_data.nutr_no,units,nutrdesc,sr_order ORDER BY sr_order;";
	private final String planContentsQS = "SELECT nutr_data.nutr_no AS _id,nutrdesc||':'||"
			+ "Round(Sum(nutr_val*amount*0.01*quantity),dec)||units||ifnull(', EAR:'||"
			+ "Round(Sum(nutr_val*amount*quantity/(ear*days)),1)||'%','')||ifnull(', RDA:'||"
			+ "Round(Sum(nutr_val*amount*quantity/(rda*days)),1)||'%','') "
			+ "||ifnull(', AI:'||Round(Sum(nutr_val*amount*quantity/(ai*days)),1)||'%','')||"
			+ "ifnull(', UL:'||Round(Sum(nutr_val*amount*quantity/(ul*days)),1)||'%','')"
			+ "||ifnull(', P/V:'||coalesce(Round(Sum(price*quantity*100*(rda*days))/Sum(nutr_val*amount*quantity),2),"
			+ "Round(Sum(price*quantity*100*(ai*days))/Sum(nutr_val*amount*quantity),2)),'') AS data "
			+ "FROM plan JOIN products ON products._id=plan.product JOIN ingredients ON ingredients.product=products._id "
			+ "JOIN nutr_data USING(ndb_no) JOIN nutr_def ON nutr_data.nutr_no=nutr_def._id LEFT OUTER JOIN "
			+ "(SELECT nutr_no,lsg,min_age,max_age,ear,rda,ai,ul FROM dri UNION "
			+ "SELECT nutr_no,lsg,min_age,max_age,ear*weight,rda*weight,ai*weight,ul*weight FROM driperkg "
			+ "JOIN (SELECT ? as weight)) AS dri USING (nutr_no) JOIN (SELECT ? AS age, ? AS cur_lsg, "
			+ "? AS days) WHERE dri.nutr_no IS NULL OR lsg=cur_lsg AND min_age<=age AND age<max_age "
			+ "GROUP BY nutr_data.nutr_no,units,nutrdesc,sr_order ORDER BY sr_order;";

	public Cursor loadContents() {
		switch (contentsType) {
		case FOOD:
			return db.rawQuery(foodContentsQS, new String[] { String.valueOf(weight), String.valueOf(age),
					String.valueOf(lifeStageGroupId), foodItemId });
		case PRODUCT:
			return db.rawQuery(productContentsQS, new String[] { String.valueOf(weight), String.valueOf(age),
					String.valueOf(lifeStageGroupId), productId });
		case PLAN:
			return db.rawQuery(planContentsQS, new String[] { String.valueOf(weight), String.valueOf(age),
					String.valueOf(lifeStageGroupId), String.valueOf(days) });
		}
		return null;
	}

	private String productId = "0";
	private CharSequence productName;

	public CharSequence getProductName() {
		return productName;
	}

	public void setProduct(long productId, CharSequence productName) {
		this.productId = String.valueOf(productId);
		this.productName = productName;
	}

	public boolean isProductSelected() {
		return "0" != productId;
	}

	public void addProduct(String product) {
		ContentValues cv = new ContentValues();
		cv.put("name", product);
		productId = String.valueOf(db.insertWithOnConflict("user.products", null, cv, SQLiteDatabase.CONFLICT_IGNORE));
		productName = product;
	}

	public void deleteProduct() {
		productId = String.valueOf(db.delete("user.products", "_id=?", new String[] { productId }));
	}

	public final String[] productsDataColumns = { "name" };
	private final String productsQS = "SELECT _id,name FROM user.products ORDER BY name;";
	private final String productsLikeQS = "SELECT _id,name,price FROM user.products WHERE name like ? ORDER BY name;";

	public Cursor loadProducts() {
		return db.rawQuery(productsQS, null);
	}

	public Cursor loadProductsLike(String like) {
		return db.rawQuery(productsLikeQS, new String[] { "%" + like + "%" });
	}

	private final String productPriceQS = "select price from user.products where _id=?";

	public String loadProductPrice() {
		Cursor cur = db.rawQuery(productPriceQS, new String[] { productId });
		cur.moveToFirst();
		productPrice = cur.getString(0);
		cur.close();
		return productPrice;
	}

	public void updateproduct(String price) {
		productPrice = price;
		ContentValues cv = new ContentValues();
		cv.put("price", productPrice);
		db.update("user.products", cv, "_id=?", new String[] { productId });
	}

	private String ingredientId = "0";

	public void setIngredientId(long ingredientId) {
		this.ingredientId = String.valueOf(ingredientId);
	}

	/*
	 * public final String[] ingredientDataColumns = { "food", "amount" };
	 * private final String ingredientQS =
	 * "SELECT long_desc AS food,amount FROM user.ingredients AS i " +
	 * "JOIN food_desc AS f ON i.ndb_no=f._id WHERE i._id=?;";
	 * 
	 * public Cursor loadIngredient() { return db.rawQuery(ingredientQS, new
	 * String[] { ingredientId }); }
	 */
	public final String[] ingredientsDataColumns = { "amount", "long_desc" };
	private final String ingredientsQS = "SELECT i._id,amount,long_desc "
			+ "FROM user.ingredients AS i JOIN food_desc ON i.ndb_no=food_desc._id "
			+ "WHERE product=? ORDER BY long_desc;";

	public Cursor loadIngredients() {
		return db.rawQuery(ingredientsQS, new String[] { productId });
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
		db.update("user.ingredients", cv, "_id=?", new String[] { ingredientId });
	}

	private String planId = "0";

	public void setPlanId(long planId) {
		this.planId = String.valueOf(planId);
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
		db.update("user.plan", cv, "_id=?", new String[] { planId });
	}

	public final String[] planDataColumns = { "quantity", "name" };
	private final String planQS = "SELECT plan._id,plan.quantity,products.name "
			+ "FROM user.plan JOIN user.products ON plan.product=products._id " + "ORDER BY products.name;";

	public Cursor loadPlan() {
		return db.rawQuery(planQS, new String[] {});
	}

	public void rmPlanItem(long id) {
		productId = String.valueOf(db.delete("user.plan", "_id=?", new String[] { String.valueOf(id) }));
	}

}
