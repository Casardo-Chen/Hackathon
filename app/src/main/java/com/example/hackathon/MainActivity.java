package com.example.hackathon;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.os.Bundle;

import com.example.hackathon.database.Collector;
import com.example.hackathon.database.DatabaseManager;
import com.example.hackathon.database.User;
import com.example.hackathon.ui.dialog.CollectorConfigurationDialogWrapper;
import com.example.hackathon.ui.dialog.CreateCollectorFromConfigDialogBuilder;
import com.example.hackathon.ui.dialog.CreateCollectorFromURLDialogBuilder;
import com.example.hackathon.ui.dialog.SetUsernameDialogBuilder;
import com.example.hackathon.ui.main_activity.DataFragment;
import com.example.hackathon.ui.main_activity.HomeFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.hackathon.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FloatingActionButton fabBtn;
    private FloatingActionButton addUrlBtn;
    private FloatingActionButton createNewBtn;

    private ActionBarDrawerToggle sidemenuToggle;
    private DrawerLayout drawerLayout;
    private NavigationView sidebarNavView;
    private View navHeader;


    private Animation top_appear_anim;
    private Animation top_disappear_anim;
    private Animation left_appear_anim;
    private Animation left_disappear_anim;

    // clicked toggle variable for fab icons
    private Boolean clicked = false;

    private Collector testCollector;
    private Collector testCollector2;
    private Collector testCollector3;
    private DatabaseManager dbManager;

    private CreateCollectorFromURLDialogBuilder createCollectorFromURLDialogBuilder;
    private CreateCollectorFromConfigDialogBuilder createCollectorFromConfigDialogBuilder;

    private Fragment currentFragment;

    // the unique id extracted from the user's device, used as their user id
    private String androidId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: this is test code, clean database and generate example collectors
        dbManager = new DatabaseManager(this);
        try {
            dbManager.clearDatabase(MainActivity.this);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Error clearing database", Toast.LENGTH_SHORT).show();
        }
        try {

            testCollector = new Collector("1", "1", "Uber", "description for a Uber collector", "what", "https", 100, 100, "graphQuery","DataFields","active");
            testCollector2 = new Collector("2", "1", "Doordash", "description for a Doordash collector", "what", "https", 139148015, 1491789595, "graphQuery","DataFields","active");
            Boolean addResult = dbManager.addOneCollector(testCollector);
            Boolean addResult2 = dbManager.addOneCollector(testCollector2);

        }
        catch (Exception e) {
            Toast.makeText(MainActivity.this, "Error Creating Collector", Toast.LENGTH_SHORT).show();
        }

        // Check for the device's unique IMEI ID, see if it's already in the database
        // If not, add the new user to database
        androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Boolean userExists = false;
        if(androidId == null) {
            Log.e("User id", "Fail to retrieve device id, userid is null");
        } else {
            userExists = dbManager.checkIfUserExists(androidId);
            if (!userExists) {
                long currentTime = Calendar.getInstance().getTimeInMillis();

                // Create a new user object, with name being an empty string
                User user = new User(androidId, "", currentTime, currentTime);
                dbManager.addOneUser(user);
                userExists = true;
            }
        }


        // load animations
        top_appear_anim = AnimationUtils.loadAnimation( this, R.anim.top_appear);
        top_disappear_anim = AnimationUtils.loadAnimation( this, R.anim.top_disappear);
        left_appear_anim = AnimationUtils.loadAnimation( this, R.anim.left_appear);
        left_disappear_anim = AnimationUtils.loadAnimation( this, R.anim.left_disappear);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // display the home fragment
        // the id here is the id for the nav menu, due to the design of the function.
        // see details of the function in later sections of this file
        displaySelectedScreen(R.id.nav_menu_home);

        // sidebar toggle
        drawerLayout = findViewById(R.id.drawer_layout);
        sidemenuToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        sidemenuToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(sidemenuToggle);

        // set sidebar use name based on user profile
        sidebarNavView = findViewById(R.id.sidebarNavView);
        navHeader = sidebarNavView.getHeaderView(0);

        TextView userNameTextView = navHeader.findViewById(R.id.userName);

        // TODO:call set username popup
        ImageButton setName = (ImageButton) navHeader.findViewById(R.id.setUsernameImageButton);
        setName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetUsernameDialogBuilder nextPopup = new SetUsernameDialogBuilder(currentFragment.getActivity(), androidId);
                Dialog newDialog = nextPopup.build();
                newDialog.show();
                userNameTextView.setText(dbManager.getUsername(androidId));
            }
        });

        if (userExists) {
            String username = dbManager.getUsername(androidId);
            if (!username.isEmpty()) {
                userNameTextView.setText(username);
            }
            // else the text will be default "set username"
        }


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        sidebarNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                displaySelectedScreen(item.getItemId());
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        Runnable refreshCollectorListRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentFragment instanceof  HomeFragment) {
                    try {
                        ((HomeFragment) currentFragment).initCollectorList();
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        this.createCollectorFromURLDialogBuilder = new CreateCollectorFromURLDialogBuilder(this);
        this.createCollectorFromConfigDialogBuilder = new CreateCollectorFromConfigDialogBuilder(this, refreshCollectorListRunnable);

        // get the fab icons
        fabBtn = findViewById(R.id.fab);
        addUrlBtn = findViewById(R.id.fab_url);
        createNewBtn = findViewById(R.id.fab_add_new);


        addUrlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = createCollectorFromURLDialogBuilder.build();
                dialog.show();
                displaySelectedScreen(R.id.nav_menu_home);
            }
        });

        createNewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CollectorConfigurationDialogWrapper wrapper = createCollectorFromConfigDialogBuilder.buildDialogWrapperWithNewCollector();
                wrapper.show();

            }
        });
    }


    // a function to switch between fragments using the navDrawer
    private void displaySelectedScreen(int itemId) {

        // initialize a fragment for switching

        switch (itemId) {
            case R.id.nav_menu_home:
                // Change this back
                currentFragment = new HomeFragment();
                break;
            case R.id.nav_menu_data:
                currentFragment = new DataFragment();
                break;
            default:
                Log.i("Menu Selection", "Menu Item Selection Error: no selection detected");
                break;
        }

        //replacing the fragment
        if (currentFragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, currentFragment);
            ft.commit();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clicked = !clicked;
                Log.i(null, "clicked value: " + clicked);
                setVisibility(clicked);
                setAnimation(clicked);
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        sidemenuToggle.syncState();
        super.onPostCreate(savedInstanceState);

    }

    private void setVisibility(Boolean clicked) {
        // if the fab icon is clicked, show the small buttons
        if(!clicked) {
            addUrlBtn.setVisibility(View.VISIBLE);
            createNewBtn.setVisibility(View.VISIBLE);
        } else {
            // if the fab icon is clicked to be closed, set the visibilities to invisible
            addUrlBtn.setVisibility(View.INVISIBLE);
            createNewBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void setAnimation(Boolean clicked) {
        if(clicked) {
            addUrlBtn.startAnimation(left_appear_anim);
            createNewBtn.startAnimation(top_appear_anim);
        } else {
            addUrlBtn.startAnimation(left_disappear_anim);
            createNewBtn.startAnimation(top_disappear_anim);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(sidemenuToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}