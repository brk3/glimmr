package com.bourke.glimmr.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.os.Handler;

import android.preference.PreferenceManager;

import android.support.v4.view.ViewPager;

import android.text.Html;

import android.util.Log;

import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;

import com.androidquery.AQuery;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GlimmrPagerAdapter;
import com.bourke.glimmr.common.MenuListView;
import com.bourke.glimmr.event.Events.IActivityItemsReadyListener;
import com.bourke.glimmr.event.Events.IPhotoInfoReadyListener;
import com.bourke.glimmr.fragments.explore.RecentPublicPhotosFragment;
import com.bourke.glimmr.fragments.home.ContactsGridFragment;
import com.bourke.glimmr.fragments.home.FavoritesGridFragment;
import com.bourke.glimmr.fragments.home.GroupListFragment;
import com.bourke.glimmr.fragments.home.PhotosetsFragment;
import com.bourke.glimmr.fragments.home.PhotoStreamGridFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.services.ActivityNotificationHandler;
import com.bourke.glimmr.services.AppListener;
import com.bourke.glimmr.services.AppService;
import com.bourke.glimmr.tasks.LoadFlickrActivityTask;
import com.bourke.glimmr.tasks.LoadPhotoInfoTask;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import com.googlecode.flickrjandroid.activity.Event;
import com.googlecode.flickrjandroid.activity.Item;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;

import com.sbstrm.appirater.Appirater;

import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import java.io.File;

import java.lang.Runnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.simonvt.widget.MenuDrawer;
import net.simonvt.widget.MenuDrawerManager;

import org.ocpsoft.pretty.time.PrettyTime;
import android.widget.ImageButton;

public class MainActivity extends BaseActivity {

    private static final String TAG = "Glimmr/MainActivity";

    public static final String KEY_TIME_MENUDRAWER_ITEMS_LAST_UPDATED =
        "glimmr_time_menudrawer_items_last_updated";
    public static final String KEY_STATE_MENUDRAWER =
        "glimmr_menudrawer_state";
    public static final String KEY_STATE_ACTIVE_POSITION =
        "glimmr_menudrawer_active_position";

    private List<PageItem> mContent;
    private MenuDrawerManager mMenuDrawerMgr;
    private MenuAdapter mMenuAdapter;
    private MenuListView mList;
    private Runnable mToggleUpRunnable;
    private boolean mDisplayHomeAsUp = true;
    private GlimmrPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private PageIndicator mIndicator;
    private int mActivePosition = -1;
    private long mActivityListVersion = -1;
    private SharedPreferences mPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mOAuth == null) {
            startActivity(new Intent(this, ExploreActivity.class));
        } else {
            if (savedInstanceState != null) {
                mActivePosition =
                    savedInstanceState.getInt(KEY_STATE_ACTIVE_POSITION);
            }
            mPrefs = getSharedPreferences(Constants.PREFS_NAME,
                    Context.MODE_PRIVATE);
            mAq = new AQuery(this);
            initPageItems();
            mMenuDrawerMgr =
                new MenuDrawerManager(this, MenuDrawer.MENU_DRAG_CONTENT);
            mMenuDrawerMgr.setContentView(R.layout.main_activity);
            initViewPager();
            initMenuDrawer();
            initNotificationAlarms();
            Appirater.appLaunched(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPrefs = getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_PRIVATE);
        mOAuth = loadAccessToken(mPrefs);
        if (mOAuth != null) {
            mUser = mOAuth.getUser();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            if (activityItemsNeedsUpdate()) {
                updateMenuListItems(false);
            }
            mMenuDrawerMgr.toggleMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mMenuDrawerMgr != null) {
            final int drawerState = mMenuDrawerMgr.getDrawerState();
            if (drawerState == MenuDrawer.STATE_OPEN ||
                    drawerState == MenuDrawer.STATE_OPENING) {
                mMenuDrawerMgr.closeMenu();
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMenuDrawerMgr != null) {
            outState.putParcelable(KEY_STATE_MENUDRAWER,
                    mMenuDrawerMgr.onSaveDrawerState());
            outState.putInt(KEY_STATE_ACTIVE_POSITION, mActivePosition);
        }
        outState.putLong(KEY_TIME_MENUDRAWER_ITEMS_LAST_UPDATED,
                mActivityListVersion);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        mActivityListVersion = inState.getLong(
                KEY_TIME_MENUDRAWER_ITEMS_LAST_UPDATED, -1);
        if (mMenuDrawerMgr != null ) {
        mMenuDrawerMgr.onRestoreDrawerState(inState
                .getParcelable(KEY_STATE_MENUDRAWER));
        }
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

    public void updateMenuListItems(boolean forceRefresh) {
        if (Constants.DEBUG) Log.d(TAG, "updateMenuListItems");

        final List<Object> menuItems = new ArrayList<Object>();

        /* Add the standard page related items */
        for (PageItem page : mContent) {
            menuItems.add(new MenuDrawerItem(page.mTitle, page.mIconDrawable));
        }
        menuItems.add(new MenuDrawerCategory(getString(R.string.activity)));

        /* If the activity list file exists, add the contents to the menu
         * drawer area.  Otherwise start a task to fetch one. */
        File f = getFileStreamPath(ActivityNotificationHandler
                .ACTIVITY_ITEMLIST_FILE);
        if (f.exists() && !forceRefresh) {
            /* There is some duplicated code here.  Could move it into another
             * function but the task is fragmented enough as is */
            List<Item> items = ActivityNotificationHandler.loadItemList(this);
            menuItems.addAll(buildActivityStream(items));
            mActivityListVersion = mPrefs.getLong(ActivityNotificationHandler
                    .KEY_TIME_ACTIVITY_ITEMS_LAST_UPDATED, -1);
            mMenuAdapter.setItems(menuItems);
            mMenuAdapter.notifyDataSetChanged();
        } else {
            setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
            new LoadFlickrActivityTask(new IActivityItemsReadyListener() {
                @Override
                public void onItemListReady(List<Item> items) {
                    setSupportProgressBarIndeterminateVisibility(
                        Boolean.FALSE);
                    if (items != null) {
                        ActivityNotificationHandler.storeItemList(
                            MainActivity.this, items);
                        menuItems.addAll(buildActivityStream(items));
                        mActivityListVersion = mPrefs.getLong(
                                ActivityNotificationHandler
                                .KEY_TIME_ACTIVITY_ITEMS_LAST_UPDATED, -1);
                    } else {
                        Log.e(TAG, "onItemListReady: Item list is null");
                    }
                    mMenuAdapter.setItems(menuItems);
                    mMenuAdapter.notifyDataSetChanged();
                }
            })
            .execute(mOAuth);
        }
    }

    /**
     * Determines if the menu drawer's items need to be refreshed from the
     * cache file.
     * True if the cache file doesn't exist or a newer cache file exists than
     * the version we're displaying.
     */
    private boolean activityItemsNeedsUpdate() {
        long lastUpdate = mPrefs.getLong(ActivityNotificationHandler
                .KEY_TIME_ACTIVITY_ITEMS_LAST_UPDATED, -1);
        File f = getFileStreamPath(ActivityNotificationHandler
                .ACTIVITY_ITEMLIST_FILE);
        boolean isStale = (mActivityListVersion < lastUpdate);
        boolean ret = (isStale || !f.exists());
        if (Constants.DEBUG) Log.d(TAG, "activityItemsNeedsUpdate: " + ret);
        return ret;
    }

    /**
     * An item can be a photo or photoset.
     * An event can be a comment, note, or fav on that item.
     */
    private List<Object> buildActivityStream(List<Item> activityItems) {
        List<Object> ret = new ArrayList<Object>();
        if (activityItems == null) {
            return ret;
        }

        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        String html = "<small><i>%s</i></small><br>" +
            "%s <font color=\"#ff0084\"><b>%s</b></font> <i>‘%s’</i>";

        for (Item i : activityItems) {
            if ("photo".equals(i.getType())) {
                StringBuilder itemString = new StringBuilder();
                for (int j=i.getEvents().size()-1; j>=0; j--) {
                    Event e = ((List<Event>)i.getEvents()).get(j);
                    String pTime = prettyTime.format(e.getDateadded());
                    String author = e.getUsername();
                    if (mUser != null && mUser.getUsername().equals(author)) {
                        author = getString(R.string.you);
                    }
                    if ("comment".equals(e.getType())) {
                        itemString.append(String.format(html, pTime, author,
                                    getString(R.string.commented_on),
                                    i.getTitle()));
                    } else if ("fave".equals(e.getType())) {
                        itemString.append(String.format(html, pTime, author,
                                    getString(R.string.favorited),
                                    i.getTitle()));
                    } else {
                        Log.e(TAG, "unsupported Event type: " + e.getType());
                        continue;
                    }
                    if (j > 0) {
                        itemString.append("<br><br>");
                    }
                }
                ret.add(new MenuDrawerActivityItem(itemString.toString(), -1));
            }
        }
        return ret;
    }

    private void initMenuDrawer() {
        /* A custom ListView is needed so the drawer can be notified when it's
         * scrolled. This is to update the position
         * of the arrow indicator. */
        mList = new MenuListView(this);
        mMenuAdapter = new MenuAdapter();
        mList.setDivider(null);
        mList.setDividerHeight(0);
        mList.setCacheColorHint(0);
        mList.setBackgroundResource(R.drawable.navy_blue_tiled);
        mList.setSelector(R.drawable.selectable_background_glimmrdark);
        mList.setAdapter(mMenuAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                switch (mMenuAdapter.getItemViewType(position)) {
                    case MENU_DRAWER_ITEM:
                        mViewPager.setCurrentItem(position);
                        mActivePosition = position;
                        mMenuDrawerMgr.setActiveView(view, position);
                        mMenuDrawerMgr.closeMenu();
                        break;
                    case MENU_DRAWER_ACTIVITY_ITEM:
                        /* offset the position by number of content items + 1
                         * for the category item */
                        startViewerForActivityItem(position-mContent.size()-1);
                        break;
                }
            }
        });
        mList.setOnScrollChangedListener(
                new MenuListView.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                mMenuDrawerMgr.getMenuDrawer().invalidate();
            }
        });

        /* If first run, make the "up" button blink until clicked */
        final Handler handler = new Handler();
        final boolean isFirstRun = mPrefs.getBoolean(
                Constants.KEY_IS_FIRST_RUN, true);
        if (isFirstRun) {
            if (Constants.DEBUG) Log.d(TAG, "First run");
            mToggleUpRunnable = new Runnable() {
                @Override
                public void run() {
                    mDisplayHomeAsUp = !mDisplayHomeAsUp;
                    mActionBar.setDisplayHomeAsUpEnabled(mDisplayHomeAsUp);
                    handler.postDelayed(mToggleUpRunnable, 500);
                }
            };
            handler.postDelayed(mToggleUpRunnable, 500);
        }

        mMenuDrawerMgr.setMenuView(mList);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mMenuDrawerMgr.getMenuDrawer().setTouchMode(
                MenuDrawer.TOUCH_MODE_FULLSCREEN);
        mMenuDrawerMgr.getMenuDrawer().setOnDrawerStateChangeListener(
                new MenuDrawer.OnDrawerStateChangeListener() {
                    @Override
                    public void onDrawerStateChange(int oldState,
                            int newState) {
                        if (newState == MenuDrawer.STATE_OPEN) {
                            handler.removeCallbacks(mToggleUpRunnable);
                            if (isFirstRun) {
                                SharedPreferences.Editor editor =
                                    mPrefs.edit();
                                editor.putBoolean(Constants.KEY_IS_FIRST_RUN,
                                    false);
                                editor.commit();
                            }
                            if (!mDisplayHomeAsUp) {
                                mActionBar.setDisplayHomeAsUpEnabled(true);
                            }
                            if (activityItemsNeedsUpdate()) {
                                updateMenuListItems(false);
                            }
                        }
                    }
                });

        ViewPager.SimpleOnPageChangeListener pageChangeListener =
                new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(final int position) {
                if (mIndicator != null) {
                    mIndicator.setCurrentItem(position);
                } else {
                    mActionBar.setSelectedNavigationItem(position);
                }
                if (position == 0) {
                    mMenuDrawerMgr.getMenuDrawer().setTouchMode(
                        MenuDrawer.TOUCH_MODE_FULLSCREEN);
                } else {
                    mMenuDrawerMgr.getMenuDrawer().setTouchMode(
                        MenuDrawer.TOUCH_MODE_NONE);
                }
            }
        };
        if (mIndicator != null) {
            mIndicator.setOnPageChangeListener(pageChangeListener);
        } else {
            mViewPager.setOnPageChangeListener(pageChangeListener);
        }

        updateMenuListItems(false);
    }

    private void startViewerForActivityItem(int itemPos) {
        setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);

        // TODO: only load these once throughout the activity
        List<Item> items = ActivityNotificationHandler
            .loadItemList(MainActivity.this);
        Item item = items.get(itemPos);
        new LoadPhotoInfoTask(new IPhotoInfoReadyListener() {
            @Override
            public void onPhotoInfoReady(final Photo photo) {
                if (photo == null) {
                    Log.e(TAG, "onPhotoInfoReady: photo is null, " +
                        "can't start viewer");
                    // TODO: alert user
                    return;
                }
                List<Photo> photos = new ArrayList<Photo>();
                photos.add(photo);
                setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
                PhotoViewerActivity.startPhotoViewer(
                    MainActivity.this, photos, 0);
            }
        }, item.getId(), item.getSecret()).execute(mOAuth);
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
        mPagerAdapter = new GlimmrPagerAdapter(
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
        mViewPager.setAdapter(mPagerAdapter);

        mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        if (mIndicator != null) {
            mIndicator.setViewPager(mViewPager);
        } else {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            for (PageItem page : mContent) {
                ActionBar.Tab newTab =
                    mActionBar.newTab().setText(page.mTitle);
                newTab.setTabListener(mPagerAdapter);
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

    private static final class MenuDrawerActivityItem {
        public String mTitle;
        public int mIconRes;

        MenuDrawerActivityItem(String title, int iconRes) {
            mTitle = title;
            mIconRes = iconRes;
        }
    }

    public static final int MENU_DRAWER_ITEM = 0;
    public static final int MENU_DRAWER_CATEGORY_ITEM = 1;
    public static final int MENU_DRAWER_ACTIVITY_ITEM = 2;

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
            Object item = getItem(position);
            if (item instanceof MenuDrawerActivityItem) {
                return MENU_DRAWER_ACTIVITY_ITEM;

            } else if (item instanceof MenuDrawerCategory) {
                return MENU_DRAWER_CATEGORY_ITEM;
            }
            return MENU_DRAWER_ITEM;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public boolean isEnabled(int position) {
            return !(getItem(position) instanceof MenuDrawerCategory);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            Object item = getItem(position);

            if (item instanceof MenuDrawerActivityItem) {
                if (v == null) {
                    v = (LinearLayout) getLayoutInflater().inflate(
                            R.layout.menu_row_activity_item, parent, false);
                }
                TextView tv = (TextView) v.findViewById(R.id.text);
                tv.setText(Html.fromHtml(
                            ((MenuDrawerActivityItem) item).mTitle));

            } else if (item instanceof MenuDrawerItem) {
                if (v == null) {
                    v = getLayoutInflater().inflate(
                            R.layout.menu_row_item, parent, false);
                }
                TextView tv = (TextView) v;
                tv.setText(((MenuDrawerItem) item).mTitle);
                tv.setCompoundDrawablesWithIntrinsicBounds(
                        ((MenuDrawerItem) item).mIconRes, 0, 0, 0);

            } else if (item instanceof MenuDrawerCategory) {
                if (v == null) {
                    v = (LinearLayout) getLayoutInflater().inflate(
                            R.layout.menu_row_category, parent, false);
                }
                ((TextView) v.findViewById(R.id.text)).setText(
                    ((MenuDrawerCategory) item).mTitle);

                ImageButton refreshActivityButton = (ImageButton)
                    v.findViewById(R.id.refreshActivityStreamButton);
                refreshActivityButton.setOnClickListener(
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                updateMenuListItems(true);
                            }
                        });
            } else {
                Log.e(TAG, "MenuAdapter.getView: Unsupported item type");
            }

            v.setTag(R.id.mdActiveViewPosition, position);

            if (position == mActivePosition) {
                mMenuDrawerMgr.setActiveView(v, position);
            }

            return v;
        }
    }
}
