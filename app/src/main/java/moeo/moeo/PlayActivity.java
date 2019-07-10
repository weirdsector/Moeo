package moeo.moeo;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PlayActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    ListView m_ListView;
    CustomAdapter m_Adapter;
    private TextToSpeech tts;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    PendingIntent pIntent;
    IntentFilter[] filters;    String text; String voice;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String[] question = null;
    private String[] answer = null;
    private String answerValue;
    int q = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#303F9F"));
        }
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //chat setting
        m_Adapter = new CustomAdapter(3);
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

//        findViewById(R.id.button1).setOnClickListener(new Button.OnClickListener() {
//                                                          @Override
//                                                          public void onClick(View v) {
//                                                              EditText editText = (EditText) findViewById(R.id.editText1) ;
//                                                              String inputValue = editText.getText().toString() ;
//                                                              editText.setText("");
//                                                              refresh(inputValue,0);
//                                                          }
//                                                      }
//        );
//
//
//        findViewById(R.id.button2).setOnClickListener(new Button.OnClickListener() {
//                                                          @Override
//                                                          public void onClick(View v) {
//                                                              EditText editText = (EditText) findViewById(R.id.editText1) ;
//                                                              String inputValue = editText.getText().toString() ;
//                                                              editText.setText("");
//                                                              refresh(inputValue,1);
//                                                          }
//                                                      }
//        );

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
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_SPACE:
                promptSpeechInput();
            default:
                return super.onKeyUp(keyCode, event);

        }
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
    @Override
    protected void onResume() {
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
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        setIntent(intent);
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
                text = "안녕? 모에요랑 놀자! 뭐하고 놀까요? 과일가게랑 동물원 구경 중에 골라봐요.";
                speakOut();
               /* Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        text = "야옹야옹 하고 말하는 동물은 누구에요?";
                        speakOut();
                    }
                }, 5000);  // 2000은 2초를 의미합니다.

*/
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }
    private void speakOut() {
        refresh(text,0);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
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
                    if(result.get(0).indexOf("동물원")>=0){
                        question=getResources().getStringArray(R.array.zoo);
                        answer = getResources().getStringArray(R.array.zoo_answer);
                        text = getQuestion();
                        refresh("신난다! 동물원 구경하자. "+text,0);
                        tts.speak("신난다! 동물원 구경하자. "+text, TextToSpeech.QUEUE_FLUSH, null);
                        return;
                    } else if (result.get(0).indexOf("과일가게")>=0){
                        question = getResources().getStringArray(R.array.fruits);
                        answer = getResources().getStringArray(R.array.fruits_answer);
                        text = getQuestion();
                        refresh("재미있겠다! 과일가게에서 놀자. "+text,0);
                        tts.speak("재미있겠다! 과일가게에서 놀자. "+text, TextToSpeech.QUEUE_FLUSH, null);
                        return;
                    }
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
                    voice=result.get(0);
                    if(voice.indexOf(answerValue)>=0){
                        text=this.getQuestion();
                        String yes = getAnswerYes();
                        refresh(yes+text,0);
                        tts.speak(yes+text, TextToSpeech.QUEUE_FLUSH, null);
                    } else if(answerValue.indexOf("free")>=0){
                        text=this.getQuestion();
                        String ok = getAnswerOK();
                        refresh(ok+text,0);
                        tts.speak(ok+text, TextToSpeech.QUEUE_FLUSH, null);
                    } else{
                        refresh("음, 다시 한 번 생각해 볼까? "+text,0);
                        tts.speak("음, 다시 한 번 생각해 볼까? "+text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                    //new JSONTask().execute("http://52.199.178.211:5100/api/v1/strawberry/sendMessage");//AsyncTask 시작시킴

                }
                break;
            }

        }
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
    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
