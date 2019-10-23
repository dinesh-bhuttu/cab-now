package com.example.ridercabnow.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.ridercabnow.MapActivities.ChooseRideActivity;
import com.example.ridercabnow.MapActivities.WelcomeActivity;
import com.example.ridercabnow.R;

public class PaymentDialog extends DialogFragment {

    private static String paymentSelected = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String[] payments = getActivity().getResources().getStringArray(R.array.payments);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Payment method");
        builder.setSingleChoiceItems(R.array.payments, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                paymentSelected = payments[i];
            }
        });

        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            // save payment method in ChooseRideActivity
            ChooseRideActivity.payment = paymentSelected;

            // get p1 and p2 from WelcomeActivity and pass to ChooseRideActivity
            Intent i1 = new Intent(getActivity(), ChooseRideActivity.class);
            i1.putExtra("place1", WelcomeActivity.p1);
            i1.putExtra("place2", WelcomeActivity.p2);
            startActivity(i1);
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            Toast.makeText(getActivity(), "Choose a payment method", Toast.LENGTH_LONG).show();
        });

        return builder.create();
    }
}
