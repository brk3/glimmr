package com.bourke.glimmr.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.bourke.glimmr.R;
import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.OAuthUtils;

public class StackViewWidgetConfigure extends BaseActivity {

    private static final String TAG = "Glimmr/StackViewWidgetConfigure";

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private static final String KEY_WIDGET_TYPE =
        "com.bourke.glimmr.appwidget.StackViewWidgetConfigure.KEY_WIDGET_TYPE";

    public static final int WIDGET_TYPE_EXPORE = 0;
    public static final int WIDGET_TYPE_FAVORITES = 1;
    public static final int WIDGET_TYPE_PHOTOS= 2;
    public static final int WIDGET_TYPE_CONTACTS = 3;
    private static final int[] LOGIN_REQUIRED = { WIDGET_TYPE_FAVORITES,
        WIDGET_TYPE_PHOTOS, WIDGET_TYPE_CONTACTS };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        /* Set the result to CANCELED.  This will cause the widget host to
         * cancel out of the widget placement if they press the back button. */
        setResult(RESULT_CANCELED);

        setContentView(R.layout.stackview_widget_configure);

        /* Find the widget id from the intent. */
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        /* If they gave us an intent without the widget id, just bail. */
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        /* NOTE: Remember to update the constants at the top if modifying
         * these */
        String[] widgetTypes = new String[] { getString(R.string.explore),
            getString(R.string.favorites), getString(R.string.photos),
            getString(R.string.contacts) };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                widgetTypes);
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                final Context context = StackViewWidgetConfigure.this;

                /* check if widget type requires login */
                for (int widgetType : LOGIN_REQUIRED) {
                    if (widgetType == position) {
                        if (!OAuthUtils.isLoggedIn(context)) {
                            Toast.makeText(context,
                                getString(R.string.login_required),
                                Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }

                saveWidgetType(position);

                /* Make sure we pass back the original appWidgetId */
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
    }

    private void saveWidgetType(int type) {
        SharedPreferences prefs = getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_WIDGET_TYPE, type);
        editor.commit();
    }

    public static int loadWidgetType(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);
        int widgetType = prefs.getInt(KEY_WIDGET_TYPE, -1);
        if (widgetType == -1) {
            Log.e(TAG, "No widgetType found in SharedPreferences (-1)");
            widgetType = WIDGET_TYPE_EXPORE;
        }
        return widgetType;
    }

    /* Disable menu for this activity */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}
