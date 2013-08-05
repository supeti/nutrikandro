package org.nutrika;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class EditPlanItem extends Activity {
	public static String QUANTITY = "quantity";
	public static String NAME = "name";
	DatabaseIf db = DatabaseIf.INSTANCE;
	EditText quantity;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editplanitem);
		Bundle extras = getIntent().getExtras();
		quantity = (EditText) findViewById(R.id.editPlanItemQuatity);
		quantity.setText(extras.getString(QUANTITY));
    	((TextView) findViewById(R.id.editPlanItemName)).setText(extras.getString(NAME));
    	findViewById(R.id.editPlanItemOK).setOnClickListener(new OKCallBack());
    	findViewById(R.id.editPlanItemCancel).setOnClickListener(new CancelCallBack());
	}

	class OKCallBack implements OnClickListener {
		public void onClick(View v) {
			db.updateplan(quantity.getText().toString());
			finish();
		}
	}

	class CancelCallBack implements OnClickListener {
		public void onClick(View v) {
			finish();
		}
	}
}
