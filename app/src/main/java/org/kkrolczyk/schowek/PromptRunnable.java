package org.kkrolczyk.schowek;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by kkrolczyk on 20.11.14. => idea by Thomas Penwell
 */

class PromptRunnable implements Runnable {
    Context context;
    String message;
    public PromptRunnable(Context ctx, String message)
    {
        this.context = ctx;
        this.message = message;
    }

    private String v;
    void setValue(String input) {
        this.v = input;
    }
    String getValue() {
        return this.v;
    }
    public void run() {
        this.run();
    }



    public void promptForResult(final PromptRunnable postrun, Boolean numeric_only) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this.context);
        alert.setTitle(this.message);
        //alert.setMessage("Message.");

        final EditText input = new EditText(this.context);
        if (numeric_only)
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        alert.setView(input);
        // procedure for when the ok button is clicked.
        alert.setPositiveButton(this.context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                dialog.dismiss();
                postrun.setValue(value);

                postrun.run();
                return;
            }
        });

        alert.setNegativeButton(this.context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                return;
            }
        });
        alert.show();
    }

}

////////////////////////////////////////////////// USE THIS IN OUTER CALL
//    private Boolean set_password(){
//        new PromptRunnable(this, "").promptForResult(
//                new PromptRunnable(this, getString(R.string.enter_password)){
//                    public void run() {
//                        getPreferences(0).edit().putInt("stored_key", Integer.parseInt(this.getValue()));
//                    } }, true);
//        return true;
//    }

//    promptForResult(new PromptRunnable(){
//        // put whatever code you want to run after user enters a result
//        public void run() {
//            // get the value we stored from the dialog
//            String value = this.getValue();
//            // do something with this value...
//            // In our example we are taking our value and passing it to
//            // an activity intent, then starting the activity.
//            Intent i = new Intent(getApplication(), MyActivity.class);
//            i.putExtra("extraValue", value);
//            startActivity(i);
//        }
//    });