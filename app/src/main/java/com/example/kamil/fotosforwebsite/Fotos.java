package com.example.kamil.fotosforwebsite;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.File;


public class Fotos extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotos);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fotos, menu);
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


    public String getRealPathFromURI(Uri contentUri) {
        String [] proj={MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = managedQuery( contentUri,
                proj,     // Which columns to return
                null,     // WHERE clause; which rows to return (all rows)
                null,     // WHERE clause selection arguments (none)
                null);     // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    public String fotoDirectory="";
    public File fotos[];
    public int aktualne=0;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.w("mam PATH","catch");
        if (requestCode == 0xDEAD) // Check which request we're responding to
        {
            if (resultCode == RESULT_OK) // Make sure the request was successful
            {
                aktualne=0;
                // The user picked a foto.
                // The Intent's data Uri identifies which contact was selected.
                Log.w("mam PATH","catch2");
                Uri uri=data.getData();
                String PATH=getRealPathFromURI(uri);
                int index = PATH.lastIndexOf("/");
                fotoDirectory = PATH.substring(0,index);
                Log.w("mam PATH",fotoDirectory);
                try {
                    fotos = new File(fotoDirectory).listFiles();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                //    Log.w("mam PATH",fileName);
                //try
                //{
                //    TextView t=new TextView(this);
                //    t=(TextView)findViewById(R.id.textView2);
                //    t.setText(PATH);
                //}catch (Exception e) {
                //    Log.e("TextView Exception",e.toString());
                //}
                //ImageView i=new ImageView(R.id.imageView);
                //i.setImageResource();
                try {
                    File imgFile = new File(String.valueOf(fotos[aktualne]));
                    Log.d("ShowedFile",fotos[aktualne].toString());
                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        ImageView myImage = (ImageView) findViewById(R.id.imageView);
                        int nh = (int) (myBitmap.getHeight() * (512.0 / myBitmap.getWidth()));
                        Bitmap scaled = Bitmap.createScaledBitmap(myBitmap, 512, nh, true);
                        myImage.setImageBitmap(scaled);
                    }
                }catch (Exception e) {
                    Log.e("Return File Exception",e.toString());
                }
            }
        }
    }
    public void nextFoto(View view)
    {

        aktualne++;
        aktualne%=fotos.length;
        try {
            File imgFile = new File(String.valueOf(fotos[aktualne]));
            Log.d("ShowedFile",fotos[aktualne].toString());
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ImageView myImage = (ImageView) findViewById(R.id.imageView);
                int nh = (int) (myBitmap.getHeight() * (512.0 / myBitmap.getWidth()));
                Bitmap scaled = Bitmap.createScaledBitmap(myBitmap, 512, nh, true);
                myImage.setImageBitmap(scaled);
            }
        }catch (Exception e) {
            Log.e("Return File Exception",e.toString());
        }
    }
    public void selectDirectory(View view)
    {
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("file/*"); // intent type to filter application based on your requirement
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        int REQUEST_CODE=0xDEAD;
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), REQUEST_CODE);
    }


}
