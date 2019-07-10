package moeo.moeo;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static boolean loading = false;
    private FirebaseAuth mAuth;
    private ImageButton profileBtn;
    private ImageButton quiz_btn;
    private ImageButton freetalk_btn;
    private ImageButton playBtn;
    private DatabaseReference mDatabase;
    private DatabaseReference profileRef;
    private TextView nameText;

    private TextView like;
    private TextView like1;
    private TextView like2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        nameText = (TextView)findViewById(R.id.name_text);
        like = (TextView)findViewById(R.id.like_text);
        like1 = (TextView)findViewById(R.id.like_text1);
        like2 = (TextView)findViewById(R.id.like_text2);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        ImageButton userInfo_btn = (ImageButton)findViewById(R.id.user_info_btn);
        userInfo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                startActivity(intent);
            }
        });
        ImageButton wordcard_btn = (ImageButton)findViewById(R.id.wordcard_btn);
        wordcard_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                startActivity(intent);
            }
        });
        profileBtn = (ImageButton)findViewById(R.id.profileBtn);
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),  PaintActivity.class);
                startActivity(intent);
            }
        });
        quiz_btn = (ImageButton)findViewById(R.id.quiz_btn);
        quiz_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),  PlayActivity.class);
                startActivity(intent);
            }
        });
        freetalk_btn = (ImageButton)findViewById(R.id.freetalk_btn);
        freetalk_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),  BingleActivity.class);
                startActivity(intent);
            }
        });
        playBtn = (ImageButton)findViewById(R.id.playBtn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),  DiaryActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAuth.getCurrentUser()==null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }else {
            Uri uri = mAuth.getCurrentUser().getPhotoUrl();
            if(uri!=null) {
                Picasso.with(MainActivity.this).load(uri).into(profileBtn);
            }
            profileRef = mDatabase.child("users").child(mAuth.getCurrentUser().getUid());
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String date = dataSnapshot.child("date").getValue(String.class);
                    String fa0 = dataSnapshot.child("favourite").child("fa0").getValue(String.class);
                    String fa1 = dataSnapshot.child("favourite").child("fa1").getValue(String.class);
                    String fa2 = dataSnapshot.child("favourite").child("fa2").getValue(String.class);
                    nameText.setText(name+" , "+date);
                    like.setText("#"+fa0);
                    like1.setText(" #"+fa1);
                    like2.setText(" #"+fa2);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };
            profileRef.addListenerForSingleValueEvent(eventListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            mAuth.signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            // Handle the camera action
        } else if (id == R.id.nav_myinfo) {
            Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_pass) {
            String emailAddress = mAuth.getCurrentUser().getEmail();

            mAuth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this,"비밀번호 재설정 메일을 발송 했습니다.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else if (id == R.id.nav_notice) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/모에요-150532662258048/"));
            startActivity(intent);
        } else if (id == R.id.nav_store) {
            Toast.makeText(MainActivity.this,"준비 중입니다.",Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_term) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forestlab.kr/moeyo.html"));
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
