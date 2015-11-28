package org.kkrolczyk.schowek.Notes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.kkrolczyk.schowek.R;
import org.kkrolczyk.schowek._external_AesCbcWithIntegrity;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public class NoteAdd extends Activity {

    EditText textField;
    final private float DEFAULT_FONT_SIZE = 12.0f;
    private static String content_encrypted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_add);

        Intent intent = getIntent();
        String message = intent.getStringExtra("content");
        boolean note_encrypted = intent.getBooleanExtra(content_encrypted, false);
        content_encrypted = getString(R.string.content_encrypted);

        textField = ((EditText) findViewById(R.id.Update_or_new_view));
        textField.setTextSize(intent.getFloatExtra("FontSize", DEFAULT_FONT_SIZE));
        prepare_note_view(message, note_encrypted);

    }
    private void prepare_note_view(final String message, boolean note_encrypted){

        if(note_encrypted && message.length()>0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText input = new EditText(this);
            //input.setTransformationMethod();
            input.setHint(getString(R.string.set_password));
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            final TextView info = new TextView(this);
            LinearLayout layout = new LinearLayout(getApplicationContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(input);
            layout.addView(info);
            alert.setView(layout);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    _external_AesCbcWithIntegrity.SecretKeys keys;
                    try {
                        String real_message;
                        if (message.startsWith(content_encrypted)) {
                                real_message = message.substring(content_encrypted.length());
                            } else {
                                final int before = 10;  // ENCRYPTED(
                                int title_len = message.indexOf(')') - before;
                                final int after = 2; // )\n
                                real_message = message.substring(before + title_len + after); // "ENCRYPTED(   "TITLE"   )\n
                            }
                        keys = _external_AesCbcWithIntegrity.generateKeyFromPassword(input.getText().toString(), "AesDontNeedSalt?");
                        _external_AesCbcWithIntegrity.CipherTextIvMac encoded = new _external_AesCbcWithIntegrity.CipherTextIvMac(real_message);
                        real_message = _external_AesCbcWithIntegrity.decryptString(encoded, keys);
                        textField.setText(real_message);
                        textField.setSelection(real_message.length());
                        real_message = "";

                    } catch (GeneralSecurityException e) {
                        Toast.makeText(getBaseContext(), "Unable to setup security, wrong password?", Toast.LENGTH_SHORT).show();
                        success(false); // wrong password or unable to setup security in other way
                    } catch (UnsupportedEncodingException e){
                        // Multi-catches not supported at this language level
                        Toast.makeText(getBaseContext(), "Unable to setup security, encoding fail...", Toast.LENGTH_SHORT).show();
                    } catch (IllegalArgumentException e) {
                        // Thrown when password passphrase cannot be decrypted, for example if passed text is not ciphered text but raw text.
                        Toast.makeText(getBaseContext(), "Unable to setup security, not a valid secret...", Toast.LENGTH_SHORT).show();
                        //success(false);
                        // try to recover...
                        Toast.makeText(getBaseContext(), "...converting to plain-text", Toast.LENGTH_SHORT).show();
                        textField.setText(message);
                        Intent intent = getIntent();
                        intent.removeExtra(content_encrypted);
                    }

                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Text will not be decrypted.
                    // Canceled / missing password / wrong passoword
                    Toast.makeText(getBaseContext(), getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
                    success(false);
                }
            });
            alert.show();
        } else {
                textField.setText(message);
                textField.setSelection(message.length());
        }

    }

    public void save_clicked(View v) {

        Intent intent = this.getIntent();
        if (intent.getBooleanExtra(content_encrypted, false)){
            save_encrypted_note(v);
        } else {
            intent.putExtra("content", textField.getText().toString());
            success(true);
        }
    }

    public void cancel_clicked(View v) {
        success(false);
    }

    public void save_encrypted_note(View v) {

        final Intent intent = this.getIntent();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        final EditText info = new EditText(this);
        info.setHint("Optional: set title");
        input.setHint(getString(R.string.set_password));
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        //input.setTransformationMethod();
        //alert.setView(input);
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input);
        layout.addView(info);
        alert.setView(layout);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                _external_AesCbcWithIntegrity.SecretKeys keys;
                try {
                    keys = _external_AesCbcWithIntegrity.generateKeyFromPassword(input.getText().toString(), "AesDontNeedSalt?");
                    String secure = _external_AesCbcWithIntegrity.encrypt(textField.getText().toString(), keys).toString();
                    String title;
                    if (info.getText().toString().length() > 0)
                        title = "ENCRYPTED(" + info.getText().toString() + ")\n";
                    else
                        title = content_encrypted;
                    intent.putExtra("content", title + secure);
                    success(true);
                } catch (GeneralSecurityException e) {
                    Toast.makeText(getBaseContext(), "Unable to setup security, unable to encrypt...", Toast.LENGTH_SHORT).show();
                } catch (UnsupportedEncodingException e) {
                    // Multi-catches not supported at this language level
                    Toast.makeText(getBaseContext(), "Unable to setup security, unable to encrypt...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Text will not be decrypted.
                // Canceled / missing password / wrong passoword
                Toast.makeText(getBaseContext(), getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
            }
        });
        alert.show();
    }

    public void success(boolean isIt){
        Intent intent = this.getIntent();
        if (isIt)
            this.setResult(RESULT_OK, intent);
        else
            this.setResult(RESULT_CANCELED, intent);
        finish();
    }
}
