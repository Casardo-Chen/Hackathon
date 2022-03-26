package com.example.hackathon.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.hackathon.R;
import com.example.hackathon.database.Collector;
import com.example.hackathon.database.DatabaseManager;
import com.google.gson.Gson;

import java.util.Base64;

public class CreateCollectorFromURLDialogBuilder {

    private Context c;
    private AlertDialog.Builder dialogBuilder;
    private Runnable refreshCollectorListRunnable;

    public CreateCollectorFromURLDialogBuilder(Context c) {
        this.c = c;
        this.dialogBuilder = new AlertDialog.Builder(c);

    }

    public Dialog build(){
        final View popupView = LayoutInflater.from(c).inflate(R.layout.dialog_add_collector_from_url, null);
        dialogBuilder.setView(popupView);
        Dialog dialog = dialogBuilder.create();
        Button popupCancelBtn = (Button) popupView.findViewById(R.id.addFromUrlCancelButton);
        Button popupNextBtn = (Button) popupView.findViewById(R.id.addFromUrlAddButton);
        EditText urlText = (EditText) popupView.findViewById(R.id.urlEditText);

        popupCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        popupNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // decode URL
                Gson gson = new Gson();
                if (urlText.getText() != null) {
                    byte[] result = Base64.getDecoder().decode(urlText.getText().toString());
                    String collectorJson = result.toString();
                    Collector newCollector = gson.fromJson(collectorJson, Collector.class);

                    // add to the collector database
                    DatabaseManager dbManager = new DatabaseManager(c);
                    dbManager.addOneCollector(newCollector);

                    // recursively call itself with new currentScreen String value
                    refreshCollectorListRunnable.run();

                    dialog.dismiss();
                    CreateCollectorFromURLDialogSuccessMessage nextPopup = new CreateCollectorFromURLDialogSuccessMessage(c);
                    nextPopup.build();
                } else {
                    //Toast.makeText(context,"Please enter a valid URL", Toast.LENGTH_LONG).show();
                }

            }
        });
        return dialog;
    }

}
