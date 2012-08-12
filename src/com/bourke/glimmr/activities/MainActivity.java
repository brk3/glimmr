package com.bourke.glimmr.activities;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;

import com.bourke.glimmr.fragments.home.ContactsGridFragment;
import com.bourke.glimmr.fragments.home.GroupListFragment;
import com.bourke.glimmr.fragments.home.PhotosetsFragment;
import com.bourke.glimmr.fragments.home.PhotoStreamGridFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.services.AppListener;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import com.viewpagerindicator.TitlePageIndicator;

public class MainActivity extends BaseActivity {

    private static final String TAG = "Glimmr/MainActivity";

    public static final int CONTACTS_PAGE = 0;
    public static final int PHOTOSTREAM_PAGE = 1;
    public static final int SETS_PAGE = 2;
    public static final int GROUPS_PAGE = 3;

    //TODO: add to R.strings
    public static final String[] CONTENT =
        new String[] { "Contacts", "You", "Sets", "Groups" };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getLogTag(), "onCreate");

        if (mOAuth == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            WakefulIntentService.scheduleAlarms(
                    new AppListener(), this, false);
            setContentView(R.layout.main);
            mAq = new AQuery(this);
            initViewPager();
        }
    }

    private void initViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        GlimmrPagerAdapter adapter = new GlimmrPagerAdapter(
                getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        TitlePageIndicator indicator = (TitlePageIndicator) findViewById(
                R.id.indicator);
        indicator.setOnPageChangeListener(this);
        indicator.setViewPager(viewPager);
    }

    class GlimmrPagerAdapter extends FragmentPagerAdapter {
        public GlimmrPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public SherlockFragment getItem(int position) {
            switch (position) {
                case PHOTOSTREAM_PAGE:
                    return PhotoStreamGridFragment.newInstance(
                            mOAuth.getUser());

                case CONTACTS_PAGE:
                    return ContactsGridFragment.newInstance(mOAuth.getUser());

                case GROUPS_PAGE:
                    return GroupListFragment.newInstance(mOAuth.getUser());

                case SETS_PAGE:
                    return PhotosetsFragment.newInstance(mOAuth.getUser());
            }
            return null;
        }

        @Override
        public int getCount() {
            return MainActivity.CONTENT.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return MainActivity.CONTENT[position % MainActivity.CONTENT.length]
                .toUpperCase();
        }
    }
}
