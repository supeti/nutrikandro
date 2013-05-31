package org.nutrika;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditPlanItem extends Activity {
	public static String QUANTITY = "quantity";
	public static String NAME = "name";
	Context context = this;
	DatabaseIf db = DatabaseIf.INSTANCE;
	TextView title, item;
	EditText quantity;
	Button OK, Cancel;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editplanitem);
		Bundle extras = getIntent().getExtras();

    	title = (TextView) findViewById(R.id.editPlanItemTitle);
    	quantity = (EditText) findViewById(R.id.editPlanItemQuatity);
    	quantity.setText(extras.getString(QUANTITY));
    	item = (TextView) findViewById(R.id.editPlanItemName);
    	item.setText(extras.getString(NAME));
    	OK = (Button) findViewById(R.id.editPlanItemOK);
    	OK.setOnClickListener(new OKCallBack());
    	Cancel = (Button) findViewById(R.id.editPlanItemCancel);
    	Cancel.setOnClickListener(new CancelCallBack());
	}

	class OKCallBack implements OnClickListener {
		public void onClick(View v) {
			db.updateplan(quantity.getText().toString());
			Intent intent = new Intent(context, Plan.class);
			startActivity(intent);
		}
	}

	class CancelCallBack implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent(context, Plan.class);
			startActivity(intent);
		}
	}

}
