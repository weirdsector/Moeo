package moeo.moeo;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class UserInfoActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private CheckBox boyCkbox;
    private CheckBox girlCkbox;
    private Spinner spinner1;
    private EditText inputName;
    private EditText inputYear;
    private EditText inputMonth;
    private EditText inputDate;
    private EditText inputFriend;
    private EditText inputFa0;
    private EditText inputFa1;
    private EditText inputFa2;
    private ImageButton profileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#303F9F"));
        }
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("우리 아이 등록");

        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        inputName = (EditText)findViewById(R.id.inputName);
        inputYear = (EditText)findViewById(R.id.inputYear);
        inputMonth = (EditText)findViewById(R.id.inputMonth);
        inputDate = (EditText)findViewById(R.id.inputDate);
        inputFriend = (EditText)findViewById(R.id.inputFriend);
        inputFa0 = (EditText)findViewById(R.id.inputFa0);
        inputFa1 = (EditText)findViewById(R.id.inputFa1);
        inputFa2 = (EditText)findViewById(R.id.inputFa2);
        boyCkbox = (CheckBox) findViewById(R.id.boy_selected);
        girlCkbox = (CheckBox)findViewById(R.id.girl_selected);
        profileBtn = (ImageButton)findViewById(R.id.profileBtn);
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),  PaintActivity.class);
                startActivity(intent);
            }
        });
        Uri uri = mAuth.getCurrentUser().getPhotoUrl();
        if(uri!=null) {
            Picasso.with(UserInfoActivity.this).load(uri).into(profileBtn);
        }
        boyCkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    girlCkbox.setChecked(!isChecked);
            }
        });
        girlCkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boyCkbox.setChecked(!isChecked);
            }
        });
        spinner1 = (Spinner)findViewById(R.id.spinner_type);
        Button confirm = (Button)findViewById(R.id.confirm_btn);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeNewUser();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
    private void writeNewUser() {
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("name").setValue(inputName.getText().toString());
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("date").setValue(inputYear.getText().toString()+"."+inputMonth.getText().toString()+"."+inputDate.getText().toString());
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("friend").setValue(inputFriend.getText().toString());
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("favourite").child("fa0").setValue(inputFa0.getText().toString());
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("favourite").child("fa1").setValue(inputFa1.getText().toString());
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("favourite").child("fa2").setValue(inputFa2.getText().toString());
        if(boyCkbox.isChecked()) {
            mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("gender").setValue("남자 아이");
        } else{
            mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("gender").setValue("여자 아이");

        }
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("type").setValue(spinner1.getSelectedItem().toString());
        finish();
    }
}
