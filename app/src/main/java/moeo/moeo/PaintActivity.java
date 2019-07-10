package moeo.moeo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class PaintActivity extends AppCompatActivity {
    PaintView myView;
    ImageButton pinkBtn, redBtn, blueBtn, orangeBtn, yellowBtn, greenBtn, puppleBtn, ezBtn;
    Button addImageBtn;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mAuth = FirebaseAuth.getInstance();

        myView = (PaintView) findViewById(R.id.myView);
        pinkBtn = (ImageButton)findViewById(R.id.pink);
        redBtn = (ImageButton) findViewById(R.id.red);
        blueBtn = (ImageButton) findViewById(R.id.blue);
        greenBtn = (ImageButton) findViewById(R.id.green);
        yellowBtn = (ImageButton) findViewById(R.id.yellow);
        orangeBtn = (ImageButton) findViewById(R.id.orange);
        puppleBtn = (ImageButton) findViewById(R.id.pupple);
        ezBtn = (ImageButton)findViewById(R.id.ez);
        addImageBtn = (Button) findViewById(R.id.addimgBtn);
        addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PaintActivity.this,"잠시만 기다려주세요, 예쁜 그림을 모에요에 보내고 있어요.",Toast.LENGTH_SHORT).show();
                myView.buildDrawingCache();
                BitmapDrawable drawable = new BitmapDrawable(myView.getDrawingCache());
                Bitmap bitmap = getBitmapFromDrawable(drawable);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                // Create a storage reference from our app
                StorageReference storageRef = storage.getReference();
                // Create a reference to "mountains.jpg"
                final StorageReference mountainsRef = storageRef.child("images/"+mAuth.getCurrentUser().getUid()+"_profile.jpg");

                UploadTask uploadTask = mountainsRef.putBytes(data);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return mountainsRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(downloadUri)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(PaintActivity.this,"저장되었습니다.",Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        }
                                    });
                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });



                //saveBitmaptoJpeg(bitmap,"moeo","profile");
            }
        });
    }
    public void setIbMargin(ImageButton ib){
        final int top = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        lp.setMargins(0,top,0,0);
        redBtn.setLayoutParams(lp);
        orangeBtn.setLayoutParams(lp);
        pinkBtn.setLayoutParams(lp);
        blueBtn.setLayoutParams(lp);
        puppleBtn.setLayoutParams(lp);
        greenBtn.setLayoutParams(lp);
        yellowBtn.setLayoutParams(lp);
        lp = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2);
        lp.setMargins(0,top,0,0);
        ezBtn.setLayoutParams(lp);
        if(ib.getId()==R.id.ez){
            lp.setMargins(0, 0, 0, 0);
            ib.setLayoutParams(lp);
        } else {
            lp = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
            lp.setMargins(0, 0, 0, 0);
            ib.setLayoutParams(lp);
        }
    }
    public void changeColor(View v) {
        switch (v.getId()) {
            case R.id.pink:
                myView.colorState = PaintView.PINK_STATE;
                setIbMargin(pinkBtn);
                break;
            case R.id.red:
                myView.colorState = PaintView.RED_STATE;
                setIbMargin(redBtn);
                break;
            case R.id.blue:
                setIbMargin(blueBtn);
                myView.colorState = PaintView.BLUE_STATE;
                break;
            case R.id.yellow:
                setIbMargin(yellowBtn);
                myView.colorState = PaintView.YELLOW_STATE;
                break;
            case R.id.pupple:
                setIbMargin(puppleBtn);
                myView.colorState = PaintView.PUPPLE_STATE;
                break;
            case R.id.green:
                setIbMargin(greenBtn);
                myView.colorState = PaintView.GREEN_STATE;
                break;
            case R.id.orange:
                setIbMargin(orangeBtn);
                myView.colorState = PaintView.ORANGE_STATE;
                break;
            case R.id.ez:
                setIbMargin(ezBtn);
                myView.colorState = PaintView.ERAZER_STATE;
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "저장");
        menu.add(0, 2, 0, "읽어오기");
        menu.add(0, 3, 0, "나가기");

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint({ "ShowToast", "WorldWriteableFiles" })
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                try {
                    @SuppressWarnings("deprecation")
                    FileOutputStream fos = openFileOutput("picture.dat", Context.MODE_WORLD_WRITEABLE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(myView.list);
                    oos.close();
                } catch (Exception e) {
                    Log.e("error:", e.getMessage());
                }
                break;
            case 2:
                try {
                    FileInputStream fis = openFileInput("picture.dat");
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    @SuppressWarnings("unchecked")
                    ArrayList<Point> readedObject = (ArrayList<Point>) ois.readObject();
                    myView.list = readedObject;
                    myView.invalidate();
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
                break;
            case 3:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public Bitmap getBitmapFromDrawable(BitmapDrawable drawable){
        Bitmap b = drawable.getBitmap();
        return b;
    }
    public static void saveBitmaptoJpeg(Bitmap bitmap,String folder, String name){
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard
        String foler_name = "/"+folder+"/";
        String file_name = name+".jpg";
        String string_path = ex_storage+foler_name;

        File file_path;
        try{
            file_path = new File(string_path);
            if(!file_path.isDirectory()){
                file_path.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(string_path+file_name);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

        }catch(FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        }catch(IOException exception){
            Log.e("IOException", exception.getMessage());
        }
    }

}
