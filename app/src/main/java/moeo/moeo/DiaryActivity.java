package moeo.moeo;

import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import moeo.moeo.common.CustomDiaryAdapter;
import moeo.moeo.common.DiaryItem;

public class DiaryActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private DatabaseReference bigdataRef;
    private DatabaseReference momRef;
    private FirebaseAuth mAuth;
    private CustomDiaryAdapter mom_adapter;
    private CustomDiaryAdapter adapter;
    private ArrayList<DiaryItem> momList;
    private ArrayList<DiaryItem> arrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#303F9F"));
        }
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();


        arrayList = new ArrayList<DiaryItem>();
        momList = new ArrayList<DiaryItem>();
        final ListView momListView = (ListView)findViewById(R.id.mom_list);
        final ListView listView = (ListView)findViewById(R.id.wordlist);
        bigdataRef = mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("word");
        momRef = mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("mom");
        adapter = new CustomDiaryAdapter(arrayList,Color.rgb(92,209,229));
        mom_adapter= new CustomDiaryAdapter(momList,Color.rgb(165,102,255));
        momListView.setAdapter(mom_adapter);
        listView.setAdapter(adapter);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    JSONObject jsonObject = new JSONObject(dataSnapshot.getValue().toString().replace(" ","_"));
                    //arrayList.add(jsonObject.getString(jsonObject.keys().next()));
                    Iterator<String> iterable = jsonObject.keys();
                    while(iterable.hasNext()) {
                        DiaryItem item = new DiaryItem();
                        item.setItem(iterable.next().replace("_"," "));
                        arrayList.add(item);
                    }
                    adapter.notifyDataSetChanged();


                } catch (JSONException e) {
                    e.printStackTrace();
                }catch (Exception e ){

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("aa", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        bigdataRef.addValueEventListener(postListener);
        ValueEventListener momListner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    JSONObject jsonObject = new JSONObject(dataSnapshot.getValue().toString().replace(" ","_"));
                    //arrayList.add(jsonObject.getString(jsonObject.keys().next()));
                    Iterator<String> iterable = jsonObject.keys();
                    while(iterable.hasNext()) {
                        DiaryItem item = new DiaryItem();
                        item.setItem(iterable.next().replace("_"," "));
                        momList.add(item);
                    }
                    mom_adapter.notifyDataSetChanged();


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e ){

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("aa", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        momRef.addValueEventListener(momListner);
    }
}
