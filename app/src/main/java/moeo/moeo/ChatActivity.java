package moeo.moeo;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    ListView m_ListView;
    CustomAdapter m_Adapter;
    private TextToSpeech tts;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private NfcAdapter mNfcAdapter;
    PendingIntent pIntent;
    IntentFilter[] filters;    String text;
    String[] question = null;
    String[] answer = null;
    String answerValue = "";
    String voice;
    int q = 0;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private static final String TAG = "ChatActivity";

    private ImageButton settingBtn;
    private ImageView preview;


    boolean isLandScape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED&&ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
            return;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M&&ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    0);
        }
        setContentView(R.layout.activity_chat);
		mNfcAdapter =  NfcAdapter.getDefaultAdapter(this) ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#303F9F"));
        }
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
 		if (mNfcAdapter == null) {
            // NFC 미지원단말
            Toast.makeText(getApplicationContext(), "NFC를 지원하지 않는 단말기입니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        getNFCData(getIntent());


        //chat setting
        m_Adapter = new CustomAdapter(1);
        m_ListView = (ListView) findViewById(R.id.listView1);
        m_ListView.setAdapter(m_Adapter);
        //  m_Adapter.add("재미있게",1);
        // m_Adapter.add("재미있게",0);
        //  m_Adapter.add("2015/11/20",2);
        tts = new TextToSpeech(this, this);

        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                promptSpeechInput();
            }

        });

        preview = (ImageView) findViewById(R.id.previewPane);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }

        settingBtn = (ImageButton) findViewById(R.id.setting_btn);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        isLandScape =
                (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        Intent i = new Intent(this, ChatActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pIntent = PendingIntent.getActivity(this, 0, i, 0);
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            filter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
            throw new RuntimeException("fail", e);
        }

        filters = new IntentFilter[] { filter, };
        mNfcAdapter.enableForegroundDispatch(this, pIntent, filters, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        setIntent(intent);
        getNFCData(getIntent());
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.KOREA);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                btnSpeak.setEnabled(true);
                text = "안녕? 모에요 카드로 놀자!";
                speakOut();
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    final ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    refresh(result.get(0),1);
                    mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("word").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                if(postSnapshot.getKey().equals(result.get(0))){
                                    mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("word").child(result.get(0)).setValue(1+postSnapshot.getValue(Long.class));
                                    return;
                                }
                            }
                            mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("word").child(result.get(0)).setValue(1);

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    if(result.get(0).indexOf("그만")>=0){
                        refresh("안녕! 다음에 또 놀자! ",0);
                        tts.speak("안녕! 다음에 또 놀자! ", TextToSpeech.QUEUE_FLUSH, null);
                        Handler delayHandler = new Handler();
                        delayHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 2500);
                        return;
                    }

                    voice=result.get(0);
                    post("http://15.164.30.205:5000/message",voice);
                   /* if(voice.equals(answerValue)){
                        this.setText(this.getQuestion());
                        String yes = getAnswerYes();
                        refresh(yes+text,0);
                        tts.speak(yes+text, TextToSpeech.QUEUE_FLUSH, null);
                    } else if(answerValue.equals("free")){
                        this.setText(this.getQuestion());
                        String ok = getAnswerOK();
                        refresh(ok+text,0);
                        tts.speak(ok+text, TextToSpeech.QUEUE_FLUSH, null);
                    } else{
                        refresh("음, 다시 한 번 생각해 볼까?"+text,0);
                        tts.speak("음, 다시 한 번 생각해 볼까?"+text, TextToSpeech.QUEUE_FLUSH, null);
                    }*/
                }
                break;
            }

        }
    }
    public void post(String requestURL, String message) {
        message= "{content:딸기,user_id:user_1}";
        try{
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(requestURL)
                    .post(RequestBody.create(MediaType.parse("application/json"), message))
                    .build();

            //비동기 처리 (enqueue 사용)
            client.newCall(request).enqueue(new Callback() {
                //비동기 처리를 위해 Callback 구현
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("error + Connect Server Error is " + e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    System.out.println("Response Body is " + response.body().string());
                }
            });

        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_SPACE:
                promptSpeechInput();
            default:
                return super.onKeyUp(keyCode, event);

        }
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
    public void setText(String text){
        this.text=text;
    }
    public void setQA(String[] Q , String[] A){
        this.question = Q;
        this.answer = A;
    }
    public String getQuestion(){
        if(q<this.question.length) {
            q++;
        } else {
            q = (int)(Math.random()*(this.question.length-1));
        }
        this.answerValue= this.answer[q-1];
        return this.question[q-1];
    }
    public String getAnswerYes(){
        String [] array = getResources().getStringArray(R.array.yes);
        int rand = (int)(Math.random()*(array.length-1));
        return array[rand];
    }
    public String getAnswerOK(){
        String [] array = getResources().getStringArray(R.array.ok);
        int rand = (int)(Math.random()*(array.length-1));
        return array[rand];
    }
	 private void getNFCData(Intent intent) {
        System.out.println(getIntent().getAction());
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Parcelable[] rawMsgs = intent
                    .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (rawMsgs != null) {
                NdefMessage[] messages = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    messages[i] = (NdefMessage) rawMsgs[i];
                }
                byte[] payload = messages[0].getRecords()[0].getPayload();
                text = new String(payload).substring(3);
                speakOut();
            }
        }
    }
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }
    private void refresh (String inputValue, int _str) {
        m_Adapter.add(inputValue,_str) ;
        m_Adapter.notifyDataSetChanged();
    }
    public void speakOut() {
        refresh(text,0);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
            } else {
                // User refused to grant permission.
            }
        }
    }


}


