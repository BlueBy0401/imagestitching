package com.hipad.opencvforstitchingimage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class StitchingActivity extends AppCompatActivity {
    private final int CLICK_PHOTO = 1;
    private Uri fileUri;
    private ImageView ivImage;
    Mat src;
    ArrayList<Mat> clickedImages;
    private static final String FILE_LOCATION = Environment.getExternalStorageDirectory() + "/Download/PacktBook/Chapter6/";
    static int ACTION_MODE = 0, MODE_NONE = 0;
    static {
        System.loadLibrary("stitcher");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stitching);
        ivImage = (ImageView)findViewById(R.id.ivImage);
        Button bClickImage, bDone;
        clickedImages = new ArrayList<Mat>();

        bClickImage = (Button)findViewById(R.id.bClickImage);
        bDone = (Button)findViewById(R.id.bDone);

        bClickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File imagesFolder = new File(FILE_LOCATION);
                imagesFolder.mkdirs();
                File image = new File(imagesFolder, "panorama_"+ (clickedImages.size()+1) + ".jpg");
//                fileUri = Uri.fromFile(image);
                fileUri= FileProvider.getUriForFile(getApplicationContext(),"com.hipad.opencvforstitchingimage.fileProvider",image);
                Log.d("StitchingActivity", "File URI = " + fileUri.toString());
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                // start the image capture Intent
                startActivityForResult(intent, CLICK_PHOTO);
            }
        });

        bDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickedImages.size()==0){
                    Toast.makeText(getApplicationContext(), "No images clicked", Toast.LENGTH_SHORT).show();
                } else if(clickedImages.size()==1){
                    Toast.makeText(getApplicationContext(), "Only one image clicked", Toast.LENGTH_SHORT).show();
                    Bitmap image = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(src, image);
                    ivImage.setImageBitmap(image);
                } else {
                    createPanorama();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        Log.d("StitchingActivity", requestCode + " " + CLICK_PHOTO + " " + resultCode + " " + RESULT_OK);

        switch(requestCode) {
            case CLICK_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
//                        final Uri imageUri = imageReturnedIntent.getData();
                        Log.d("StitchingActivity", fileUri.toString());
                        final InputStream imageStream = getContentResolver().openInputStream(fileUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        src = new Mat(selectedImage.getHeight(), selectedImage.getWidth(), CvType.CV_8UC4);
                        Imgproc.resize(src, src, new Size(src.rows()/4, src.cols()/4));
                        Utils.bitmapToMat(selectedImage, src);
                        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2RGB);
                        clickedImages.add(src);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void createPanorama(){

        new AsyncTask<Void, Void, Bitmap>() {
            ProgressDialog dialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = ProgressDialog.show(StitchingActivity.this, "Building Panorama", "Please Wait");
            }

            @Override
            protected Bitmap doInBackground(Void... params) {
                Mat srcRes = new Mat();
                Log.e("www","imagesieze="+clickedImages.size());
                int success = StitchPanorama(clickedImages.toArray(), clickedImages.size(), srcRes.getNativeObjAddr());
                Log.d("StitchingActivity", srcRes.rows()+" "+srcRes.cols()+" "+success);
                Log.d("www", srcRes.getNativeObjAddr()+"");
                if(success==0){
                    return null;
                }

                Imgproc.cvtColor(srcRes, srcRes, Imgproc.COLOR_BGR2RGBA);
                Bitmap bitmap = Bitmap.createBitmap(srcRes.cols(), srcRes.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(srcRes, bitmap);

                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                dialog.dismiss();
                if(bitmap!=null) {
                    ivImage.setImageBitmap(bitmap);
                }
            }
        }.execute();
    }
    public native int StitchPanorama(Object images[], int size, long addrSrcRes);

}
