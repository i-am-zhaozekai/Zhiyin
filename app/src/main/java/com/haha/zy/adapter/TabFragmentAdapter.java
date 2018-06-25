package com.haha.zy.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 07/06/2018
 */

public class TabFragmentAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragments;

    public TabFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);

        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
