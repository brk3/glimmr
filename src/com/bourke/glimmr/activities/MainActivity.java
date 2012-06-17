package com.bourke.glimmr;

import com.androidquery.AQuery;
import android.content.Intent;

import android.os.Bundle;
import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends SherlockFragmentActivity
        implements ViewPager.OnPageChangeListener {

    private static final String TAG = "Glimmr/MainActivity";

    public static final int PHOTOSTREAM_PAGE = 0;
    public static final int CONTACTS_PAGE = 1;
    public static final int GROUPS_PAGE = 2;

    private AQuery mAq;

    private int mStackLevel = 0;

    //TODO: add to R.strings
    public static final String[] CONTENT =
        new String[] { "You", "Contacts", "Groups" };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

		mAq = new AQuery(this);
        initViewPager();

        /* Hide the home icon */
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        if (savedInstanceState != null) {
            mStackLevel = savedInstanceState.getInt("level");
        }
    }

    private void initViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        GlimmrPagerAdapter adapter = new GlimmrPagerAdapter(
                getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        TabPageIndicator indicator = (TabPageIndicator) findViewById(
                R.id.indicator);
        indicator.setOnPageChangeListener(this);
        indicator.setViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onPageScrolled(int pos, float posOffset, int posOffsetPx) {}

    @Override
    public void onPageSelected(int pos) {}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("level", mStackLevel);
    }

    class GlimmrPagerAdapter extends FragmentPagerAdapter {
        public GlimmrPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public SherlockFragment getItem(int position) {
            switch (position) {
                case PHOTOSTREAM_PAGE:
                    return PhotoGridFragment.newInstance(PhotoGridFragment
                            .TYPE_PHOTO_STREAM);
                case CONTACTS_PAGE:
                    return PhotoGridFragment.newInstance(PhotoGridFragment
                            .TYPE_CONTACTS_STREAM);
                case GROUPS_PAGE:
                    return PhotoGridFragment.newInstance(PhotoGridFragment
                            .TYPE_GROUPS_STREAM);
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
