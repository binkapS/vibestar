package ng.com.binkap.vibestar.screens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.LinkedList;
import java.util.List;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.adapters.SongsAdapter;
import ng.com.binkap.vibestar.helpers.UserSettings;
import ng.com.binkap.vibestar.models.SongsModel;

public class SearchScreen extends AppCompatActivity {

    RecyclerView recyclerView;

    TextView noMatchFound;

    SearchView searchView;

    ImageView backButton;

    MaterialCardView topBar;

    ConstraintLayout mainBody;

    LinkedList<SongsModel> filtered;

    static LinkedList<SongsModel> toSearchFromList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);
        setContentIds();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtered = new LinkedList<>();
                if (!newText.isEmpty()){
                    for (SongsModel song: toSearchFromList) {
                        if (song.getTitle().toLowerCase().contains(newText.toLowerCase())){
                            filtered.add(song);
                        }
                    }
                    bindRecycler(true);
                }else {
                    bindRecycler(false);
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySettings();
    }

    public static void loadToSearchFromList(List<SongsModel> list){
        toSearchFromList = new LinkedList<>(list);
    }

    protected void setContentIds(){
        mainBody = findViewById(R.id.search_screen_main_body);
        recyclerView = findViewById(R.id.search_screen_recycler);
        noMatchFound = findViewById(R.id.search_screen_no_match_found);
        searchView = findViewById(R.id.search_screen_search_view);
        backButton = findViewById(R.id.search_screen_back_button);
        topBar = findViewById(R.id.search_screen_header);
        setClickListeners();
        searchView.setIconified(false);
    }

    protected void setClickListeners(){
        backButton.setOnClickListener(view -> onBackPressed());
    }

    protected void bindRecycler(boolean itemsSearched){
        if (itemsSearched) {
            if (filtered.size() > 0){
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setAdapter(new SongsAdapter(filtered));
                recyclerView.setVisibility(View.VISIBLE);
                noMatchFound.setVisibility(View.GONE);
            }else {
                recyclerView.setVisibility(View.GONE);
                noMatchFound.setVisibility(View.VISIBLE);
            }
        } else {
            recyclerView.setVisibility(View.GONE);
            noMatchFound.setVisibility(View.GONE);
        }
    }

    private void applySettings(){
        int colorPrimary = UserSettings.getColorPrimary(getApplicationContext());
        int colorPrimaryVariant = UserSettings.getColorPrimaryVariant(getApplicationContext());

        topBar.setCardBackgroundColor(colorPrimaryVariant);
        mainBody.setBackgroundColor(colorPrimary);
        setStatusBarColor(colorPrimary);
    }

    public void setStatusBarColor(int color){
        getWindow().setStatusBarColor(color);
    }
}