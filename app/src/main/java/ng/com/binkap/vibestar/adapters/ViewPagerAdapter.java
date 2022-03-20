package ng.com.binkap.vibestar.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ng.com.binkap.vibestar.fragments.AlbumsFragment;
import ng.com.binkap.vibestar.fragments.ArtistsFragment;
import ng.com.binkap.vibestar.fragments.PlayListsFragment;
import ng.com.binkap.vibestar.fragments.SongsFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public static final int TAB_COUNTS = 4;

    SongsFragment songsFragment = SongsFragment.newInstance();

    AlbumsFragment albumsFragment = AlbumsFragment.newInstance();

    PlayListsFragment playListsFragment = PlayListsFragment.newInstance();

    ArtistsFragment artistsFragment = ArtistsFragment.newInstance();

    Fragment fragment;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                fragment = songsFragment;
                break;
            case 1:
                fragment = albumsFragment;
              break;
            case 2:
                fragment = artistsFragment;
                break;
            case 3:
                fragment = playListsFragment;
                break;
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return TAB_COUNTS;
    }
}
