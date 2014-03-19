package com.bourke.glimmr.activities;

import com.bourke.glimmr.BuildConfig;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bourke.glimmr.R;
import com.bourke.glimmr.common.*;
import com.bourke.glimmr.event.Events.IActivityItemsReadyListener;
import com.bourke.glimmr.event.Events.IPhotoInfoReadyListener;
import com.bourke.glimmr.fragments.explore.RecentPublicPhotosFragment;
import com.bourke.glimmr.fragments.home.*;
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
//import com.sbstrm.appirater.Appirater;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;
import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private static final String TAG = "Glimmr/MainActivity";

    private static final String KEY_TIME_MENUDRAWER_ITEMS_LAST_UPDATED =
        "glimmr_time_menudrawer_items_last_updated";
    private static final String KEY_STATE_MENUDRAWER =
        "glimmr_menudrawer_state";
    private static final String KEY_STATE_ACTIVE_POSITION =
        "glimmr_menudrawer_active_position";
    public static final String KEY_PAGER_START_INDEX =
            "com.bourke.glimmr.MainActivity.KEY_PAGER_START_INDEX";

    private List<PageItem> mContent;
    private List<String> mPageTitles;
    private MenuDrawer mMenuDrawer;
    private final MenuAdapter mMenuAdapter = new MenuAdapter();
    private ViewPager mViewPager;
    private PageIndicator mIndicator;
    private int mActivePosition = -1;
    private long mActivityListVersion = -1;
    private SharedPreferences mPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mOAuth == null) {
            startActivityForResult(new Intent(this, ExploreActivity.class), 0);
        } else {
            if (savedInstanceState != null) {
                mActivePosition =
                    savedInstanceState.getInt(KEY_STATE_ACTIVE_POSITION);
            }
            mPrefs = getSharedPreferences(Constants.PREFS_NAME,
                    Context.MODE_PRIVATE);
            initPageItems();
            initMenuDrawer();
            initViewPager();
            initNotificationAlarms();
            handleIntent();
//            Appirater.appLaunched(this);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            final int pagerStartIndex = intent.getIntExtra(KEY_PAGER_START_INDEX, -1);
            if (pagerStartIndex > -1) {
                mViewPager.setCurrentItem(pagerStartIndex);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == ExploreActivity.ACTIVITY_RESULT_EXIT) {
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPrefs == null) {
            mPrefs = getSharedPreferences(Constants.PREFS_NAME,
                    Context.MODE_PRIVATE);
        }
        mOAuth = OAuthUtils.loadAccessToken(this);
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
            mMenuDrawer.toggleMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mMenuDrawer != null) {
            final int drawerState = mMenuDrawer.getDrawerState();
            if (drawerState == MenuDrawer.STATE_OPEN ||
                    drawerState == MenuDrawer.STATE_OPENING) {
                mMenuDrawer.closeMenu();
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMenuDrawer != null) {
            outState.putParcelable(KEY_STATE_MENUDRAWER,
                    mMenuDrawer.saveState());
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
        Parcelable drawerState = inState.getParcelable(KEY_STATE_MENUDRAWER);
        if (mMenuDrawer != null && drawerState != null) {
            mMenuDrawer.restoreState(drawerState);
        }
    }

    private void initPageItems() {
        mContent = new ArrayList<PageItem>();
        mPageTitles = Arrays.asList(
                getResources().getStringArray(R.array.pageTitles));

        mContent.add(new PageItem(getString(R.string.contacts),
                R.drawable.ic_action_social_person_dark));

        mContent.add(new PageItem(getString(R.string.photos),
                R.drawable.ic_content_picture_dark));

        mContent.add(new PageItem(getString(R.string.favorites),
                R.drawable.ic_action_rating_important_dark));

        mContent.add(new PageItem(getString(R.string.sets),
                R.drawable.collections_collection_dark));

        mContent.add(new PageItem(getString(R.string.groups),
                R.drawable.ic_action_social_group_dark));

        mContent.add(new PageItem(getString(R.string.explore),
                R.drawable.ic_action_av_shuffle_dark));
    }

    public void updateMenuListItems(boolean forceRefresh) {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateMenuListItems");

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
            setProgressBarIndeterminateVisibility(Boolean.TRUE);
            new LoadFlickrActivityTask(new IActivityItemsReadyListener() {
                @Override
                public void onItemListReady(List<Item> items, Exception e) {
                    setProgressBarIndeterminateVisibility(
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
        if (BuildConfig.DEBUG) Log.d(TAG, "activityItemsNeedsUpdate: " + ret);
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
                if ( ! itemString.toString().isEmpty()) {
                    ret.add(new MenuDrawerActivityItem(itemString.toString(), -1));
                }
            }
        }
        return ret;
    }

    private void initMenuDrawer() {
        mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.OVERLAY,
                Position.LEFT);
        mMenuDrawer.setContentView(R.layout.main_activity);
        /* The drawable that replaces the up indicator in the action bar */
        mMenuDrawer.setSlideDrawable(R.drawable.ic_drawer);
        mMenuDrawer.setDrawerIndicatorEnabled(true);
        MenuListView menuListView = new MenuListView(this);
        menuListView.setDivider(null);
        menuListView.setDividerHeight(0);
        menuListView.setCacheColorHint(0);
        menuListView.setBackgroundResource(R.drawable.navy_blue_tiled);
        menuListView.setSelector(R.drawable.selectable_background_glimmrdark);
        menuListView.setAdapter(mMenuAdapter);
        menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (mMenuAdapter.getItemViewType(position)) {
                    case MENU_DRAWER_ITEM:
                        mViewPager.setCurrentItem(position);
                        mActivePosition = position;
                        mMenuDrawer.setActiveView(view, position);
                        mMenuDrawer.closeMenu();
                        break;
                    case MENU_DRAWER_ACTIVITY_ITEM:
                        /* offset the position by number of content items + 1
                         * for the category item */
                        startViewerForActivityItem(position - mContent.size() - 1);
                        break;
                }
            }
        });
        menuListView.setOnScrollChangedListener(
                new MenuListView.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        mMenuDrawer.invalidate();
                    }
                });

        /* If first run, make the "up" button blink until clicked */
        final boolean isFirstRun = mPrefs.getBoolean(
                Constants.KEY_IS_FIRST_RUN, true);
        if (isFirstRun) {
            if (BuildConfig.DEBUG) Log.d(TAG, "First run");
            mMenuDrawer.peekDrawer();
        }

        mMenuDrawer.setMenuView(menuListView);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mMenuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_FULLSCREEN);
        mMenuDrawer.setOnDrawerStateChangeListener(
                new MenuDrawer.OnDrawerStateChangeListener() {
                    @Override
                    public void onDrawerStateChange(int oldState,
                            int newState) {
                        if (newState == MenuDrawer.STATE_OPEN) {
                            if (activityItemsNeedsUpdate()) {
                                updateMenuListItems(false);
                            }
                        }
                    }
                    @Override
                    public void onDrawerSlide(float openRatio,
                            int offsetPixels) {
                    }
                });

        updateMenuListItems(false);
    }

    private void startViewerForActivityItem(int itemPos) {
        setProgressBarIndeterminateVisibility(Boolean.TRUE);

        List<Item> items = ActivityNotificationHandler
            .loadItemList(MainActivity.this);
        Item item = items.get(itemPos);
        new LoadPhotoInfoTask(new IPhotoInfoReadyListener() {
            @Override
            public void onPhotoInfoReady(final Photo photo, Exception e) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                if (FlickrHelper.getInstance().handleFlickrUnavailable(MainActivity.this, e)) {
                    return;
                }
                if (photo == null) {
                    Log.e(TAG, "onPhotoInfoReady: photo is null, " +
                        "can't start viewer");
                    return;
                }
                List<Photo> photos = new ArrayList<Photo>();
                photos.add(photo);
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
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
            if (BuildConfig.DEBUG) Log.d(TAG, "Scheduling alarms");
            WakefulIntentService.scheduleAlarms(
                    new AppListener(), this, false);
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "Cancelling alarms");
            AppService.cancelAlarms(this);
        }
    }

    private void showUsageTip(final String pageTitle) {
        if (pageTitle.equalsIgnoreCase(getString(R.string.explore))) {
            UsageTips.getInstance().show(this, getString(R.string.tip_view_profile), false);
        } else if (pageTitle.equalsIgnoreCase(getString(R.string.groups))) {
            UsageTips.getInstance().show(this, getString(R.string.tip_add_to_group), false);
        } else if (pageTitle.equalsIgnoreCase(getString(R.string.sets))) {
            UsageTips.getInstance().show(this, getString(R.string.tip_add_to_set), false);
        }
    }

    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewPager);

        /* create adapter and bind to viewpager */
        GlimmrPagerAdapter glimmrPagerAdapter = new GlimmrPagerAdapter(
                getSupportFragmentManager(), mViewPager, mActionBar,
                mPageTitles.toArray(new String[mPageTitles.size()])) {
            @Override
            public Fragment getItemImpl(int position) {
                try {
                    PageItem page = mContent.get(position);
                    return GlimmrFragmentFactory.getInstance(MainActivity.this,
                            page.mTitle, mUser);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void onPageSelected(final int position) {
                showUsageTip(mPageTitles.get(position));
                if (mIndicator != null) {
                    mIndicator.setCurrentItem(position);
                } else {
                    mActionBar.setSelectedNavigationItem(position);
                }
                if (position == 0) {
                    mMenuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_FULLSCREEN);
                } else {
                    mMenuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_NONE);
                }
            }
        };
        mViewPager.setAdapter(glimmrPagerAdapter);

        /* bind the listeners to either the indicator or the actionbar */
        mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        if (mIndicator != null) {
            mIndicator.setOnPageChangeListener(glimmrPagerAdapter);
            mIndicator.setViewPager(mViewPager);
        } else {
            mViewPager.setOnPageChangeListener(glimmrPagerAdapter);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            for (PageItem page : mContent) {
                ActionBar.Tab newTab =
                    mActionBar.newTab().setText(page.mTitle);
                newTab.setTabListener(glimmrPagerAdapter);
                mActionBar.addTab(newTab);
            }
        }

        /* set current tab to what's in preferences */
        SharedPreferences defaultSharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(this);
        String initialTabPref = defaultSharedPrefs.getString(
                Constants.KEY_INITIAL_TAB_LIST_PREFERENCE,
                getString(R.string.contacts));
        mViewPager.setCurrentItem(mPageTitles.indexOf(initialTabPref));
    }

    private static final class PageItem {
        public final String mTitle;
        public final Integer mIconDrawable;

        PageItem(String title, int iconDrawable) {
            mTitle = title;
            mIconDrawable = iconDrawable;
        }
    }

    private static final class MenuDrawerItem {
        public final String mTitle;
        public final int mIconRes;

        MenuDrawerItem(String title, int iconRes) {
            mTitle = title;
            mIconRes = iconRes;
        }
    }

    private static final class MenuDrawerCategory {
        public final String mTitle;

        MenuDrawerCategory(String title) {
            mTitle = title;
        }
    }

    private static final class MenuDrawerActivityItem {
        public final String mTitle;
        public final int mIconRes;

        MenuDrawerActivityItem(String title, int iconRes) {
            mTitle = title;
            mIconRes = iconRes;
        }
    }

    private static final int MENU_DRAWER_ITEM = 0;
    private static final int MENU_DRAWER_CATEGORY_ITEM = 1;
    private static final int MENU_DRAWER_ACTIVITY_ITEM = 2;

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
                    v = getLayoutInflater().inflate(
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
                tv.setText(((MenuDrawerItem) item).mTitle.toUpperCase(
                            Locale.getDefault()));
                tv.setCompoundDrawablesWithIntrinsicBounds(
                        ((MenuDrawerItem) item).mIconRes, 0, 0, 0);
                mTextUtils.setFont(tv, TextUtils.FONT_ROBOTOTHIN);

            } else if (item instanceof MenuDrawerCategory) {
                if (v == null) {
                    v = getLayoutInflater().inflate(
                            R.layout.menu_row_category, parent, false);
                }
                TextView tv = (TextView) v.findViewById(R.id.text);
                tv.setText(((MenuDrawerCategory) item).mTitle);
                mTextUtils.setFont(tv, TextUtils.FONT_ROBOTOBOLD);

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
                mMenuDrawer.setActiveView(v, position);
            }

            return v;
        }
    }

    public static class GlimmrFragmentFactory {
        public static Fragment getInstance(Context context,
                String name, User user) {
            if (context.getString(R.string.contacts).equals(name))  {
                return ContactsGridFragment.newInstance();
            } else if (context.getString(R.string.photos).equals(name))  {
                return PhotoStreamGridFragment.newInstance(user);
            } else if (context.getString(R.string.favorites).equals(name))  {
                return FavoritesGridFragment.newInstance(user);
            } else if (context.getString(R.string.sets).equals(name))  {
                return PhotosetsFragment.newInstance(user);
            } else if (context.getString(R.string.groups).equals(name))  {
                return GroupListFragment.newInstance();
            } else if (context.getString(R.string.explore).equals(name))  {
                return RecentPublicPhotosFragment.newInstance();
            } else {
               throw new IllegalArgumentException(
                       "Unknown fragment type requested: " + name);
            }
        }
    }
}
