package moeo.moeo;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Locale;

import moeo.moeo.common.BluetoothActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.example.egregory.moya.BTSockLE;
public class ChatActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    ListView m_ListView;
    CustomAdapter m_Adapter;
    private TextToSpeech tts;
    private ImageButton btnSpeak;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static final int BLUETOOTH_REQUEST = 0;
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
    BTSockLE m_bt = null;
    public Context m_ctx   = null;
    byte[] hcmd   = new byte[8];
    String stID     = new String("start");
    private ImageButton settingBtn;

    boolean isLandScape;
    interface CMD
    {
        int NONE = 0;
        int COLOR = 1;
        int BLEND = 2;
        int SERVO = 3;
        int EYE = 4;
        int SERVOS=5;
    };

    void InitBTNetwork()
    {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 1);


        // BT socket
        m_bt = new com.example.egregory.moya.BTSockLE(this);
        m_bt.m_parent = this;
    }
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
        m_ctx   = getApplicationContext();
        InitBTNetwork();
        m_bt.setOnReceive( new BTSockLE.OnReceiveListener()
        {
            @Override
            public int OnReceive(byte b)
            {
                int n 		= m_bt.GetLength();
                byte p[]	= m_bt.GetBuffer();
                String st=null,tmp=null;
                for (int i=0;i<n;i++)
                {
                    tmp     = String.format("%x,",p[i]);
                    if (st==null)   st = tmp;
                    else            st      = st+tmp;
                }

                if (stID.equals(st)==false)
                {
                    stID    = st;
                    startChat(st);
                }
                return 0;
            }

            @Override
            public int OnConnect(boolean b)
            {
                return 0;
            }
        });
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
         } else {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            getNFCData(getIntent());
        }

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


        settingBtn = (ImageButton) findViewById(R.id.setting_btn);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanBluetooth();
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
        if(mNfcAdapter!=null)
        mNfcAdapter.enableForegroundDispatch(this, pIntent, filters, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mNfcAdapter!=null)
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
            case BLUETOOTH_REQUEST :
                if (resultCode == Activity.RESULT_OK)
                {
                    BluetoothDevice d = data.getParcelableExtra("device");
                    if (d != null) m_bt.InitClient(d);
                }
                break;

        }
    }
    public void ScanBluetooth()
    {
        if (!m_bt.Open())
        {
            Toast.makeText(m_ctx,"블루투스 장치가 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!m_bt.IsConnected())
        {
            Intent bt = new Intent(m_bt.m_parent.getBaseContext(), BluetoothActivity.class);
            startActivityForResult(bt,BLUETOOTH_REQUEST);
        }
    }

    private void startChat(String st){
        Toast.makeText(m_ctx,st,Toast.LENGTH_SHORT).show();
    }

    private void sendCmd(String st){
        com.example.egregory.moya.stdafx.vToken tok  = new com.example.egregory.moya.stdafx.vToken(st);
        tok.SetSeparator(",");

        if (tok.GetSize()==0)   return;
        String cmd  = tok.GetAt(0);

        switch(tok.GetSize()){
            case 3:
                if (cmd.equals("j"))    // servo motor
                    Servo(tok.i(1),tok.i(2));
                else if (cmd.equals("e"))   // eye
                    SendEye(tok.i(1),tok.i(2));
                break;
            case 4:
                if (cmd.equals("rgb"))
                    Send(CMD.COLOR,tok.i(1),tok.i(2),tok.i(3));
                break;
            case 5:
                if (cmd.equals("blend"))
                    Send(CMD.BLEND,tok.i(1),tok.i(2),tok.i(3),tok.i(4));
                break;
            case 6:
                if (cmd.equals("j"))
                    ServoCnt(tok.i(1),tok.i(2),tok.i(3),tok.i(4),tok.i(5));
                break;
        }
    }
    public void CRC()
    {
        hcmd[0] = (byte)0xf1;
        byte crc=0;
        for (int i=1;i<7;i++)
            crc^=hcmd[i];
        hcmd[7] = crc;
    }

    public void Send(int cmd, int r,int g, int b,int d)
    {
        hcmd[0] = (byte)0xf1;
        hcmd[1] = (byte)cmd;
        hcmd[2] = (byte)(d>>8);
        hcmd[3] = (byte)r;    hcmd[4] = (byte)g;    hcmd[5] = (byte)b;
        hcmd[6] = (byte)(d & 0xff);
        CRC();
        m_bt.Send(hcmd,8);
    }

    public void Send(int cmd, int r,int g, int b)
    {
        hcmd[0] = (byte)0xf1;
        hcmd[1] = (byte)cmd;
        hcmd[2] = 0;
        hcmd[3] = (byte)r;    hcmd[4] = (byte)g;    hcmd[5] = (byte)b;
        CRC();
        m_bt.Send(hcmd,8);
    }

    public void Servo(int no,int pwm)
    {
        hcmd[1] = CMD.SERVO;
        hcmd[2] = (byte)no;

        if (pwm==0) hcmd[3] = 0;
        else        hcmd[3] = (byte)( ((float)pwm)*65./100.);
        CRC();
        m_bt.Send(hcmd,8);
    }

    public void ServoCnt(int no,int start,int end, int count, int time)
    {
        hcmd[1] = CMD.SERVOS;
        hcmd[2] = (byte)no;

        hcmd[3] = (byte)start;
        hcmd[4] = (byte)end;
        hcmd[5] = (byte)count;
        hcmd[6] = (byte)time;
        CRC();
        m_bt.Send(hcmd,8);
    }

    public void SendEye(int l,int r)
    {
        hcmd[0] = (byte)0xf1;
        hcmd[1] =  CMD.EYE;
        hcmd[2] = 0;
        hcmd[3] = (byte)l;    hcmd[4] = (byte)r;
        CRC();
        m_bt.Send(hcmd,8);
    }
    public void post(String requestURL, String message) {
        message= "{\"content\":\""+message+"\",\"user_id\":\""+mAuth.getCurrentUser().getUid()+"\"}";
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
                    String result =  response.body().string();
                    try {
                        JsonParser parser = new JsonParser();
                        Object obj = parser.parse( result );
                        JsonObject object = (JsonObject)obj;
                       String answer = object.get("text").toString();
                        answer = URLDecoder.decode(answer);
                       text = answer;
                        handler.sendEmptyMessage(0);

                    } catch (JsonParseException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            speakOut();
        }
    };
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


