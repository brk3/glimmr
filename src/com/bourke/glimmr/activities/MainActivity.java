package com.bourke.glimmrpro.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.preference.PreferenceManager;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;

import com.androidquery.AQuery;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.GlimmrPagerAdapter;
import com.bourke.glimmrpro.common.MenuListView;
import com.bourke.glimmrpro.fragments.explore.RecentPublicPhotosFragment;
import com.bourke.glimmrpro.fragments.home.ContactsGridFragment;
import com.bourke.glimmrpro.fragments.home.FavoritesGridFragment;
import com.bourke.glimmrpro.fragments.home.GroupListFragment;
import com.bourke.glimmrpro.fragments.home.PhotosetsFragment;
import com.bourke.glimmrpro.fragments.home.PhotoStreamGridFragment;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.services.ActivityNotificationHandler;
import com.bourke.glimmrpro.services.AppListener;
import com.bourke.glimmrpro.services.AppService;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import com.googlecode.flickrjandroid.activity.Event;
import com.googlecode.flickrjandroid.activity.Item;
import com.googlecode.flickrjandroid.activity.ItemList;
import com.googlecode.flickrjandroid.people.User;

import com.sbstrm.appirater.Appirater;

import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.List;

import net.simonvt.widget.MenuDrawer;
import net.simonvt.widget.MenuDrawerManager;

public class MainActivity extends BaseActivity {

    private static final String TAG = "Glimmr/MainActivity";

    private List<PageItem> mContent;

    private MenuDrawerManager mMenuDrawer;
    private MenuAdapter mMenuAdapter;
    private MenuListView mList;
    private PagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private PageIndicator mIndicator;
    private int mActivePosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.DEBUG) Log.d(getLogTag(), "onCreate");
        if (mOAuth == null) {
            startActivity(new Intent(this, ExploreActivity.class));
        } else {
            if (savedInstanceState != null) {
                mActivePosition =
                    savedInstanceState.getInt(Constants.STATE_ACTIVE_POSITION);
            }
            mAq = new AQuery(this);
            initPageItems();
            mMenuDrawer =
                new MenuDrawerManager(this, MenuDrawer.MENU_DRAG_CONTENT);
            mMenuDrawer.setContentView(R.layout.main_activity);
            initViewPager();
            initMenuDrawer();
            initNotificationAlarms();
            Appirater.appLaunched(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_PRIVATE);
        mOAuth = loadAccessToken(prefs);
        if (mOAuth != null) {
            mUser = mOAuth.getUser();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            mMenuDrawer.toggleMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        final int drawerState = mMenuDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN ||
                drawerState == MenuDrawer.STATE_OPENING) {
            mMenuDrawer.closeMenu();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.STATE_MENUDRAWER,
                mMenuDrawer.onSaveDrawerState());
        outState.putInt(Constants.STATE_ACTIVE_POSITION, mActivePosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        mMenuDrawer.onRestoreDrawerState(inState
                .getParcelable(Constants.STATE_MENUDRAWER));
    }

    @Override
    public User getUser() {
        return mUser;
    }

    private void initPageItems() {
        mContent = new ArrayList<PageItem>();

        mContent.add(new PageItem(getString(R.string.contacts),
                R.drawable.ic_action_social_person_dark,
                ContactsGridFragment.class));

        mContent.add(new PageItem(getString(R.string.photos),
                R.drawable.ic_content_picture_dark,
                PhotoStreamGridFragment.class));

        mContent.add(new PageItem(getString(R.string.favorites),
                R.drawable.ic_action_rating_important_dark,
                FavoritesGridFragment.class));

        mContent.add(new PageItem(getString(R.string.sets),
                R.drawable.collections_collection_dark,
                PhotosetsFragment.class));

        mContent.add(new PageItem(getString(R.string.groups),
                R.drawable.ic_action_social_group_dark,
                GroupListFragment.class));

        mContent.add(new PageItem(getString(R.string.explore),
                R.drawable.ic_action_av_shuffle_dark,
                RecentPublicPhotosFragment.class));
    }

    public void updateMenuListItems(ItemList flickrActivityItems) {
        if (Constants.DEBUG) Log.d(TAG, "updateMenuListItems");

        /* Add the standard page related items */
        List<Object> items = new ArrayList<Object>();
        for (PageItem page : mContent) {
            items.add(new MenuDrawerItem(page.mTitle, page.mIconDrawable));
        }

        /* Add the activity notification items.
         * An item can be a photo or photoset.
         * An event can be a comment, note, or fav on that item */
        items.add(new MenuDrawerCategory(
                getString(R.string.pref_category_notifications)));
        if (flickrActivityItems != null) {
            for (Item i : flickrActivityItems) {
                for (Event e : i.getEvents()) {
                    // TODO: create new row category here
                    // items.add(new MenuDrawerCategory(e.getValue()));
                }
            }
        }
        mMenuAdapter.setItems(items);
        mMenuAdapter.notifyDataSetChanged();
    }

    private void initMenuDrawer() {
        /* A custom ListView is needed so the drawer can be notified when it's
         * scrolled. This is to update the position
         * of the arrow indicator. */
        mList = new MenuListView(this);
        mMenuAdapter = new MenuAdapter();
        mList.setAdapter(mMenuAdapter);

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                mViewPager.setCurrentItem(position);
                mActivePosition = position;
                mMenuDrawer.setActiveView(view, position);
                mMenuDrawer.closeMenu();
            }
        });

        mList.setOnScrollChangedListener(
                new MenuListView.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                mMenuDrawer.getMenuDrawer().invalidate();
            }
        });

        mMenuDrawer.setMenuView(mList);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mMenuDrawer.getMenuDrawer().setTouchMode(
                MenuDrawer.TOUCH_MODE_FULLSCREEN);

        mIndicator.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(final int position) {
                mIndicator.setCurrentItem(position);
                if (position == 0) {
                    mMenuDrawer.getMenuDrawer().setTouchMode(
                        MenuDrawer.TOUCH_MODE_FULLSCREEN);
                } else {
                    mMenuDrawer.getMenuDrawer().setTouchMode(
                        MenuDrawer.TOUCH_MODE_NONE);
                }
            }
        });

        updateMenuListItems(ActivityNotificationHandler.loadItemList(this));
    }

    private void initNotificationAlarms() {
        SharedPreferences defaultSharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(this);
        boolean enableNotifications = defaultSharedPrefs.getBoolean(
                Constants.KEY_ENABLE_NOTIFICATIONS, false);
        if (enableNotifications) {
            if (Constants.DEBUG) Log.d(TAG, "Scheduling alarms");
            WakefulIntentService.scheduleAlarms(
                    new AppListener(), this, false);
        } else {
            if (Constants.DEBUG) Log.d(TAG, "Cancelling alarms");
            AppService.cancelAlarms(this);
        }
    }

    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        List<String> pageTitles = new ArrayList<String>();
        for (PageItem page : mContent) {
            pageTitles.add(page.mTitle);
        }
        GlimmrPagerAdapter adapter = new GlimmrPagerAdapter(
                getSupportFragmentManager(), mViewPager, mActionBar,
                pageTitles.toArray(new String[pageTitles.size()])) {
            @Override
            public SherlockFragment getItemImpl(int position) {
                try {
                    return (SherlockFragment)
                        mContent.get(position).mFragmentClass.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        mViewPager.setAdapter(adapter);

        mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        if (mIndicator != null) {
            mIndicator.setViewPager(mViewPager);
        } else {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            mViewPager.setOnPageChangeListener(adapter);
            for (PageItem page : mContent) {
                ActionBar.Tab newTab =
                    mActionBar.newTab().setText(page.mTitle);
                newTab.setTabListener(adapter);
                mActionBar.addTab(newTab);
            }
        }
    }

    private static final class PageItem {
        public String mTitle;
        public Integer mIconDrawable;
        public Class mFragmentClass;

        PageItem(String title, int iconDrawable, Class fragmentClass) {
            mTitle = title;
            mIconDrawable = iconDrawable;
            mFragmentClass = fragmentClass;
        }
    }

    private static final class MenuDrawerItem {
        public String mTitle;
        public int mIconRes;

        MenuDrawerItem(String title, int iconRes) {
            mTitle = title;
            mIconRes = iconRes;
        }
    }

    private static final class MenuDrawerCategory {
        public String mTitle;

        MenuDrawerCategory(String title) {
            mTitle = title;
        }
    }

    private class MenuAdapter extends BaseAdapter {
        private List<Object> mItems;

        MenuAdapter(List<Object> items) {
            mItems = items;
        }

        MenuAdapter() {
            mItems = new ArrayList<Object>();
        }

        public void setItems(List<Object> items) {
            mItems = items;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position) instanceof MenuDrawerItem ? 0 : 1;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position) instanceof MenuDrawerItem;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            Object item = getItem(position);

            if (item instanceof MenuDrawerCategory) {
                if (v == null) {
                    v = getLayoutInflater().inflate(R.layout.menu_row_category,
                            parent, false);
                }

                ((TextView) v).setText(((MenuDrawerCategory) item).mTitle);

            } else {
                if (v == null) {
                    v = getLayoutInflater().inflate(R.layout.menu_row_item,
                            parent, false);
                }

                TextView tv = (TextView) v;
                tv.setText(((MenuDrawerItem) item).mTitle);
                tv.setCompoundDrawablesWithIntrinsicBounds(
                        ((MenuDrawerItem) item).mIconRes, 0, 0, 0);
            }

            v.setTag(R.id.mdActiveViewPosition, position);

            if (position == mActivePosition) {
                mMenuDrawer.setActiveView(v, position);
            }

            return v;
        }
    }
}
