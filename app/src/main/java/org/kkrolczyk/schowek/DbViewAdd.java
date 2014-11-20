package org.kkrolczyk.schowek;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class DbViewAdd extends Activity {

    EditText textField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_view_add);

        Intent intent = getIntent();
        String message = intent.getStringExtra("content");
        textField = ((EditText) findViewById(R.id.Update_or_new_view));
        textField.setText(message);
        textField.setSelection(message.length());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_db_view_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void save_clicked(View v){

        Intent intent = this.getIntent();
        intent.putExtra("content", textField.getText().toString());
        this.setResult(RESULT_OK, intent);
        finish();
    }
    public void cancel_clicked(View v){

        Intent intent = this.getIntent();
        this.setResult(RESULT_CANCELED, intent);
        finish();
    }


}
