package com.example.chatall;

//import android.support.v7.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessAdapter myTabsAccessAdapter;
    private DatabaseReference RootRef;
    private String currentUserID;

    TextView username , userstatus;
    RelativeLayout rl;

    public static String Theme_Color = "Light";

    private FirebaseAuth mAuth;

    String nname = "ChatAll";
   // @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        rl = findViewById(R.id.rl_main_activity);

        RootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(nname);

        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccessAdapter = new TabsAccessAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessAdapter);
        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

        username = findViewById(R.id.user_profile_name);
        userstatus = findViewById(R.id.user_status);

    }



    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            SendUserToLoginActivity();
        }
        else
        {
            //updateUserStatus("online");

            VerifyUserExistance();
            //Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT).show();


        }



    }



  /*  @Override
    protected void onStop()
    {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null)
        {
            updateUserStatus("offline");
        }
    }  */

   /* @Override
    protected void onDestroy()
    {
        super.onDestroy();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null)
        {
            updateUserStatus("offline");
        }
    } */

    private void VerifyUserExistance()
    {
        String currentUserID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if((dataSnapshot.child("name").exists()))
                {

                    Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    SettingsNotice();
                    //Toast.makeText(MainActivity.this,"Set Username",Toast.LENGTH_SHORT).show();
                    //SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }



    private void SendUserToLoginActivity()
    {
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void SendUserToSettingsActivity()
    {
        Intent settingsintent = new Intent(MainActivity.this,SettingsActivity.class);
        settingsintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsintent);
        finish();
    }







    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
       super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
         super.onOptionsItemSelected(item);

         if(item.getItemId() == R.id.main_logout_option)
         {
             updateUserStatus("offline");

             mAuth.signOut();
             SendUserToLoginActivity();
             finish();
         }
        if(item.getItemId() == R.id.main_settings_option)
        {
            Intent sintent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(sintent);

            //SendUserToSettingsActivity();
        }
        if(item.getItemId() == R.id.main_find_friends_option)
        {
            SendUserToFindFriendsActivity();
        }
        if(item.getItemId() == R.id.main_create_group_option)
        {
            ForumNotice();
            //RequestNewGroup();
        }
        /*
        if(item.getItemId() == R.id.dark_theme)
        {
            ThemeColor.theme = "Black";
            //startActivity(new Intent(MainActivity.this, ChatActivity.class));

            //private static colordata = "Black";

            Theme();
        }
        if(item.getItemId() == R.id.light_theme)
        {
            ThemeColor.theme = "Light";

            //private static String colordata = "Light";

            Theme();
        }

         */


         return true;
    }


    private void RequestNewGroup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this , R.style.AlertDialog);
        builder.setTitle("Enter Forum Name.");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g. : Android Development Forum");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which)
            {
                String groupName = groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this,"Please Enter Forum Name",Toast.LENGTH_LONG).show();
                }
                else
                {
                    CreateNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which)
            {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void CreateNewGroup(final String groupName)
    {
        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this,groupName + " Forum is created Successfully",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void ForumNotice()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);

        builder
                .setTitle("Attention :")
                .setMessage("Forums are visible and accessible to all the users universally, and are for the purpose of global discussions only hence we recommend you not to share any sensitive data on Forum i.e. Passwords, Bank Details, etc.  CREATE A NEW FORUM ? ")
                .setPositiveButton("Yes",  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        RequestNewGroup();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }


    private void SettingsNotice()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);

        builder
                .setTitle("Attention :")
                .setMessage("Before you proceed, please setup your profile. i.e. Your Username , Status and Profile picture(optional) in the settings. Or else you would not be found by any of our users on the platform.")
                .setPositiveButton("Go to Settings",  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        SendUserToSettingsActivity();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int id) {
                        mAuth.signOut();
                        SendUserToLoginActivity();
                        finish();
                    }
                })
                .show();
    }


    private void SendUserToFindFriendsActivity()
    {
        Intent findFriendintent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findFriendintent);
    }


    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        currentUserID = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);



    }


    public void Theme()
    {
        if(ThemeColor.theme.equals("Black"))
        {

            rl.setBackgroundColor(Color.parseColor("#000000"));

        }
        else if(ThemeColor.theme.equals("Light"))
        {
           rl.setBackgroundColor(Color.parseColor("#ffffff"));
        }
    }

    /*public void LightTheme()
    {
        rl.setBackgroundColor(Color.parseColor("#ffffff"));
    }*/


    @Override
    protected void onResume()
    {
        super.onResume();
        updateUserStatus("online");

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        updateUserStatus("offline");
    }


}
