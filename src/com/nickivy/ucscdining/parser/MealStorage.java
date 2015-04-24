package com.nickivy.ucscdining.parser;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MealStorage extends SQLiteOpenHelper{
	
  public static final String TABLE_MEALS = "menu";
	  
  public static final String COLUMN_ID = "_id",
		  COLUMN_MENUITEM = "menuitem",
		  COLUMN_MEAL = "meal",
		  COLUMN_COLLEGE = "college",
		  COLUMN_MONTH = "month",
		  COLUMN_DAY = "day",
		  COLUMN_YEAR = "year",
		  COLUMN_NULLABLE = "nullcolumn";

  private static final String DATABASE_NAME = "menu.db";
  private static final int DATABASE_VERSION = 1;

  // Database creation sql statement
  private static final String DATABASE_CREATE = "create table "
		  + TABLE_MEALS + "(" + COLUMN_ID
		  + " integer primary key," + COLUMN_COLLEGE + " integer," + 
		  COLUMN_MEAL + " integer," + COLUMN_MENUITEM + " text," + 
		  COLUMN_MONTH + " integer," + COLUMN_DAY + " integer," + COLUMN_YEAR 
		  + " integer" + ")";

	public MealStorage(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEALS);
		onCreate(db);
	}

}
