package com.example.alinmatov.testapplication;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class PreviewImageActivity extends Activity {

    Bitmap image;
    File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);

        getExtraData();

        initInstant();
    }

    private void getExtraData() {
        Intent intent = getIntent();
        String message = intent.getStringExtra(Helper.SELECTED_IMAGE);

        try {
            //image = decodeUri(Uri.parse(message));
            image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(message));

            imageFile = new File(getPath(Uri.parse(message)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void initInstant() {
        ImageView imageView = (ImageView)findViewById(R.id.ivPreview);
        imageView.setImageBitmap(image);

        Button btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncHttpClient client = new AsyncHttpClient();

                RequestParams params = new RequestParams();
                try {
                    params.put("fileToUpload", imageFile);
                } catch(FileNotFoundException e) {
                    e.printStackTrace();
                }
                client.post("http://192.168.199.248/test_upload/upload.php",params,new TextHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        // called before request is started
                        Log.d("app","on start");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String response) {
                        // called when response HTTP status is "200 OK"
                        Toast.makeText(getApplicationContext(),"Upload Success",Toast.LENGTH_LONG).show();
                        Log.d("app","on Success:"+response.toString());
                        if (headers != null) {
                            Log.d("app", "Return Headers:");
                            StringBuilder builder = new StringBuilder();
                            for (Header h : headers) {
                                String _h = String.format(Locale.US, "%s : %s", h.getName(), h.getValue());
                                Log.d("app", _h);
                                builder.append(_h);
                                builder.append("\n");
                            }
                        } else {
                            Log.d("app","null");
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        Log.d("app","on Fail"+statusCode);
                        if (headers != null) {
                            Log.d("app", "Return Headers:");
                            StringBuilder builder = new StringBuilder();
                            for (Header h : headers) {
                                String _h = String.format(Locale.US, "%s : %s", h.getName(), h.getValue());
                                Log.d("app", _h);
                                builder.append(_h);
                                builder.append("\n");
                            }
                        } else {
                            Log.d("app","null");
                        }
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                        Log.d("app","on Retry");
                    }
                });
            }
        });

    }
    private File prepairImage() throws IOException {
        //create a file to write bitmap data
        File f = new File(getCacheDir(), "image.jpg");
        f.createNewFile();

//Convert bitmap to byte array
        Bitmap bitmap = image;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        return f;
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 500;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preview_image, menu);
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
}
