package com.udacity.stockhawk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.udacity.stockhawk.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AddStockDialog extends DialogFragment {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.dialog_add_stock_symbol)
    EditText stock;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams") final View addStockDialogBody =
                inflater.inflate(R.layout.dialog_add_stock, null);

        ButterKnife.bind(this, addStockDialogBody);

        stock.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                addStock();
                return true;
            }
        });

        final AlertDialog dialog = new AlertDialog.Builder( getActivity() )
                .setView(addStockDialogBody)
                .setMessage(getString(R.string.dialog_title))
                .setPositiveButton(getString(R.string.dialog_add), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addStock();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .create();

        final Window dialogWindow = dialog.getWindow();
        if (null != dialogWindow) {
            dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        final Button addButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        addButton.setEnabled(false);

        stock.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // We do not need to do anything here. It is required by the interface.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // We do not need to do anything here. It is required by the interface.
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    addButton.setEnabled(false);
                } else {
                    addButton.setEnabled(true);
                }
            }
        });
    }

    private void addStock() {
        final Activity parent = getActivity();
        if (parent instanceof MainActivity) {
            ((MainActivity) parent).addStock(stock.getText().toString());
        }
        dismissAllowingStateLoss();
    }


}
