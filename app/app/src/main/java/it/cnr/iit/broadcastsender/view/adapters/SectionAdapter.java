package it.cnr.iit.broadcastsender.view.adapters;

/**
 * Created by mattia on 16/01/17.
 */
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import it.cnr.iit.broadcastsender.view.GroupFragment;
import it.cnr.iit.broadcastsender.view.MainFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionAdapter extends FragmentPagerAdapter {

    private Fragment[] fragments = new Fragment[]{
            new MainFragment(),
            new GroupFragment()
    };

    public SectionAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "MAIN";
            case 1:
                return "GROUP";
        }
        return null;
    }
}
