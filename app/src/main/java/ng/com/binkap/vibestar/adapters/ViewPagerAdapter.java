package ng.com.binkap.vibestar.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

import ng.com.binkap.vibestar.fragments.AlbumsFragment;
import ng.com.binkap.vibestar.fragments.ArtistsFragment;
import ng.com.binkap.vibestar.fragments.PlayListsFragment;
import ng.com.binkap.vibestar.fragments.SongsFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    ArrayList<Fragment> fragments = new ArrayList<>();

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragments.add(SongsFragment.newInstance());
        fragments.add(AlbumsFragment.newInstance());
        fragments.add(ArtistsFragment.newInstance());
        fragments.add(PlayListsFragment.newInstance());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}
