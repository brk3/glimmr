package com.bourke.glimmr.fragments.dialogs;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.bourke.glimmr.R;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.TextUtils;

import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

public class BuyProDialog extends SimpleDialogFragment {

    private static final String TAG = "Glimmr/BuyProDialog";

    private TextUtils mTextUtils;

    public static void show(FragmentActivity activity) {
        new BuyProDialog().show(activity.getSupportFragmentManager(), TAG);
    }

    @Override
    public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
        mTextUtils = new TextUtils(getActivity().getAssets());
        View layout = LayoutInflater.from(getActivity()).inflate(
                R.layout.buy_pro_dialog_fragment, null);
        TextView tvPart1 = (TextView) layout.findViewById(R.id.buyProPart1);
        mTextUtils.setFont(tvPart1, TextUtils.FONT_ROBOTOLIGHT);
        builder.setView(layout);
        builder.setPositiveButton("Buy Pro", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(Constants.PRO_MARKET_LINK);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                dismiss();
            }
        });
        builder.setNegativeButton("No Thanks", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return builder;
    }
}
