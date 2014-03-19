package com.bourke.glimmr.fragments.dialogs;

import android.support.v4.app.FragmentActivity;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

import com.bourke.glimmr.R;

import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

public class LoginErrorDialog extends SimpleDialogFragment {

    private static final String TAG = "Glimmr/LoginErrorDialog";

    public static void show(FragmentActivity activity) {
        new LoginErrorDialog().show(activity.getSupportFragmentManager(), TAG);
    }

    @Override
    public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
        TextView message = new TextView(getActivity());
        message.setText(getString(R.string.login_error));
        Linkify.addLinks(message, Linkify.ALL);
        int padding = (int) getActivity().getResources()
                .getDimension(R.dimen.dialog_message_padding);
        builder.setView(message, padding, padding, padding, padding);
        builder.setTitle(getString(R.string.hmm));
        builder.setNegativeButton(getString(android.R.string.ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return builder;
    }
}
