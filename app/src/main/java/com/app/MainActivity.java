package com.app;

import android.content.Context;
import android.database.DataSetObserver;
import android.media.Image;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener, CameraTestFragment.OnFragmentInteractionListener {

    // Adapter for the tab sections at the top
    private SectionsPagerAdapter mSectionsPagerAdapter;
    // The view pager that will hold the tabs
    private ViewPager mViewPager;
    // The tab layout to display the sections
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view for activity
        setContentView(R.layout.activity_main);

        // Create the adapter that will return the proper fragment for each of the three
        // tabs in the pager
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter
        mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }

        // Set up the TabLayout with the view pager
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        if (mTabLayout != null) {
            mTabLayout.setupWithViewPager(mViewPager);
        }
        setupTabLayout();

        // Set up the floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }

    }

    /**
     * setupTabLayout
     * Setup the Tab Layout
     *  - Set the icons and custom views
     *  - Set selected tab to middle tab
     *  - Set tab gravity
     */
    private void setupTabLayout() {
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mTabLayout.setSelectedTabIndicatorHeight(0);

        TabLayout.Tab leftTab = mTabLayout.getTabAt(0);
        if (leftTab != null) {
            View view = getLayoutInflater().inflate(R.layout.profile_tab, null);
            ImageView icon = (ImageView) view.findViewById(R.id.profile_icon);
            icon.setImageResource(R.drawable.profile_icon);
            leftTab.setCustomView(view);
        }

        TabLayout.Tab centerTab = mTabLayout.getTabAt(1);
        if (centerTab != null) {
            View view = getLayoutInflater().inflate(R.layout.candid_tab, null);
            ImageView title = (ImageView) view.findViewById(R.id.candid_title);
            title.setImageResource(R.drawable.title);
            centerTab.setCustomView(view);
            centerTab.select();
        }

        TabLayout.Tab rightTab = mTabLayout.getTabAt(2);
        if (rightTab != null) {
            View view = getLayoutInflater().inflate(R.layout.connect_tab, null);
            ImageView icon = (ImageView) view.findViewById(R.id.connect_icon);
            icon.setImageResource(R.drawable.connect_icon);
            rightTab.setCustomView(view);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {}

    /**
     * SectionsPagerAdapter
     * Handles paging for the tab sections
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public final static int PROFILE_TAB = 0;
        public final static int MAIN_TAB = 1;
        public final static int CONNECT_TAB = 2;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case PROFILE_TAB: {
                    return new CameraTestFragment();
                }
                case MAIN_TAB: {
                    return new MainFragment();
                }
                default: return new Fragment();
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }
}
