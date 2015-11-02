package org.kkrolczyk.schowek;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class NoteAdd extends Activity {

    EditText textField;
    final private float DEFAULT_FONT_SIZE = 12.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_add);

        Intent intent = getIntent();
        String message = intent.getStringExtra("content");

        textField = ((EditText) findViewById(R.id.Update_or_new_view));
        textField.setTextSize(intent.getFloatExtra("FontSize", DEFAULT_FONT_SIZE));
        textField.setText(message);
        textField.setSelection(message.length());
    }

    public void save_clicked(View v) {
        Intent intent = this.getIntent();
        intent.putExtra("content", textField.getText().toString());
        this.setResult(RESULT_OK, intent);
        finish();
    }

    public void cancel_clicked(View v) {
        Intent intent = this.getIntent();
        this.setResult(RESULT_CANCELED, intent);
        finish();
    }
}
