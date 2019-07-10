package moeo.moeo.common;


import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.List;

import moeo.moeo.ChatActivity;
import moeo.moeo.R;
import moeo.moeo.cloudtextrecognition.CloudTextGraphic;
import moeo.moeo.cloudtextrecognition.VisionProcessorBase;

/**
 * Processor for the cloud text detector demo.
 */
public class CloudTextRecognitionProcessor extends VisionProcessorBase<FirebaseVisionText> {

    private static final String TAG = "CloudTextRecProc";
    private ChatActivity chatActivity = null;
    private final FirebaseVisionTextRecognizer detector;

    public CloudTextRecognitionProcessor() {
        super();
        detector = FirebaseVision.getInstance().getCloudTextRecognizer();
    }
    public CloudTextRecognitionProcessor(ChatActivity chatActivity){
        super();
        detector=FirebaseVision.getInstance().getCloudTextRecognizer();
        this.chatActivity = chatActivity;
    }
    @Override
    protected Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull FirebaseVisionText text,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (text == null) {
            return; // TODO: investigate why this is needed
        }

        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int l = 0; l < elements.size(); l++) {
                    CloudTextGraphic cloudTextGraphic = new CloudTextGraphic(graphicOverlay,
                            elements.get(l));
                    graphicOverlay.add(cloudTextGraphic);
                    Log.e("!!!!!!",elements.get(l).getText());
                    String resultText = elements.get(l).getText();
                    if (resultText.equals("딸기")) {
                        chatActivity.setQA(chatActivity.getResources().getStringArray(R.array.strawberry_q),chatActivity.getResources().getStringArray(R.array.strawberry_a));
                        chatActivity.setText(chatActivity.getQuestion());
                        chatActivity.speakOut();
                    } else if(resultText.equals("거북이")){
                        chatActivity.setQA(chatActivity.getResources().getStringArray(R.array.turtle_q),chatActivity.getResources().getStringArray(R.array.turtle_a));
                        chatActivity.setText(chatActivity.getQuestion());
                        chatActivity.speakOut();
                    } else if(resultText.equals("바나나")){
                        chatActivity.setQA(chatActivity.getResources().getStringArray(R.array.banana_q),chatActivity.getResources().getStringArray(R.array.banana_a));
                        chatActivity.setText(chatActivity.getQuestion());
                        chatActivity.speakOut();
                    }
                }
            }
        }
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        chatActivity.setQA(chatActivity.getResources().getStringArray(R.array.strawberry_q),chatActivity.getResources().getStringArray(R.array.strawberry_a));
        chatActivity.setText(chatActivity.getQuestion());
        chatActivity.speakOut();
        Log.w(TAG, "Cloud Text detection failed." + e);
    }
}
