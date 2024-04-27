package com.techtop.onehyreapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class CustomAlertbox extends AppCompatDialogFragment {
    private EditText editTextNumber, editTextMail, editTextName;
    private CustomAlertBoxListener listener;
    String defaultValue = "Not Set";

    @SuppressLint("MissingInflatedId")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_alertbox, null);

        Drawable icon = getResources().getDrawable(R.drawable.ic_share32);
        builder.setIcon(icon);

        builder.setView(view)
                .setIcon(icon)
                .setTitle("Do you want to share Trip Data with Customer?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String clientPNo = editTextNumber.getText().toString();
                        String clientMail = editTextMail.getText().toString();
                        String clientName = editTextName.getText().toString();

                        if (clientMail.isEmpty())
                        {
                            clientMail = defaultValue;
                        }
                        if (clientName.isEmpty())
                        {
                            clientName = defaultValue;
                        }
                        if (clientPNo.isEmpty())
                        {
                            clientPNo = defaultValue;
                        }
                        listener.applyTexts(clientPNo, clientMail, clientName);
                        BuildVariant.isSavedPremium = true;
                    }
                });

        editTextNumber = view.findViewById(R.id.edittxtInsertPNumber);
        editTextMail = view.findViewById(R.id.edittxtInsertMail);
        editTextName = view.findViewById(R.id.edittxtInsertName);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (CustomAlertBoxListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement CustomAlertBoxListener");
        }
    }

    public interface CustomAlertBoxListener{
        void applyTexts(String clientPNo, String clientMail, String clientName);
    }
}
