package com.example.kamil.fotosforwebsite;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
//import android.support.v7.app.ActionBarActivity;
//import android.view.Menu;
//import android.view.MenuItem;


public class Fotos extends AppCompatActivity
{

	public String photoDirectory="";

	public File photos[];
	public int current=0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fotos);
	}

	public String getRealPathFromURI(Uri contentUri)
	{
		Log.w("getRealPathFromURI","State1");
		String[] project={MediaStore.Images.Media.DATA};
		Log.w("getRealPathFromURI","State2");
		Log.w("contentUri",contentUri.toString());
		//noinspection deprecation
		android.database.Cursor cursor=managedQuery(contentUri,project,     // Which columns to return
				null,     // WHERE clause; which rows to return (all rows)
				null,     // WHERE clause selection arguments (none)
				null);     // Order-by clause (ascending by name)
		int column_index=cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		Log.w("getRealPathFromURI","State3");
		cursor.moveToFirst();
		Log.w("getRealPathFromURI","State4");
		return cursor.getString(column_index);
	}
	public Bitmap getRotatedBitmap(File imgFile)
	{
		Bitmap myBitmap=BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		try
		{
			ExifInterface exif=new ExifInterface(imgFile.getAbsolutePath());
			int orientation=exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,1);
			Matrix matrix=new Matrix();
			Log.w("ROTATION","ROTATION START");
			switch(orientation)
			{
				case 3:
					matrix.postRotate(180);
					myBitmap=Bitmap.createBitmap(myBitmap,0,0,myBitmap.getWidth(),myBitmap.getHeight(),matrix,true);
					break;
				case 6:
					matrix.postRotate(90);
					myBitmap=Bitmap.createBitmap(myBitmap,0,0,myBitmap.getWidth(),myBitmap.getHeight(),matrix,true);
					break;
				case 8:
					matrix.postRotate(90);
					myBitmap=Bitmap.createBitmap(myBitmap,0,0,myBitmap.getWidth(),myBitmap.getHeight(),matrix,true);
					break;

			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return myBitmap;
	}
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data)
	{

		Log.w("mam PATH","catch");
		if(requestCode==0xDEAD) // Check which request we're responding to
		{
			if(resultCode==RESULT_OK) // Make sure the request was successful
			{
				current=0;
				// The user picked a photo.
				// The Intent's data Uri identifies which contact was selected.
				Log.w("mam PATH","catch2");
				Uri uri=data.getData();
				Log.w("mam PATH","catch3");
				String PATH=getRealPathFromURI(uri);
				Log.w("mam PATH","catch4");
				int index=PATH.lastIndexOf("/");
				photoDirectory=PATH.substring(0,index);
				Log.w("mam PATH",photoDirectory);
				try
				{
					photos=new File(photoDirectory).listFiles(new FilenameFilter()
					{
						public boolean accept(File dir,String name)
						{
							return name.toLowerCase().endsWith(".jpg");
						}
					});
					Arrays.sort(photos,new Comparator<File>(){
						@Override
						public int compare(File lhs,File rhs)
						{
							return lhs.getName().compareTo(rhs.getName());
						}
					});
				}catch(Exception e)
				{
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
				try
				{
					File imgFile=new File(String.valueOf(photos[current]));
					Log.d("ShowedFile",photos[current].toString());
					if(imgFile.exists())
					{
						Bitmap myBitmap=getRotatedBitmap(imgFile);
								/*BitmapFactory.decodeFile(imgFile.getAbsolutePath());
						ExifInterface exif = new ExifInterface(imgFile.getAbsolutePath());
						int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
						Matrix matrix = new Matrix();
						Log.w("ROTATION","ROTATION START");
						switch(orientation)
						{
							case 3:
								matrix.postRotate(180);
								myBitmap= Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
						        break;
							case 6:
								matrix.postRotate(90);
								myBitmap= Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
								break;
							case 8:
								matrix.postRotate(90);
								myBitmap= Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
								break;

						}*/
						ImageView myImage=(ImageView)findViewById(R.id.imageView);
						int nh=(int)(myBitmap.getHeight()*(512.0/myBitmap.getWidth()));
						Bitmap scaled=Bitmap.createScaledBitmap(myBitmap,512,nh,true);
						myImage.setImageBitmap(scaled);
					}
				}catch(Exception e)
				{
					Log.e("Return File Exception",e.toString());
				}
			}
		}
	}

	public void nextPhoto(View view)
	{
		if(photoDirectory.equals(""))
		{
			return;
		}
		//getting description
		try
		{
			TextView textField=(TextView)findViewById(R.id.editText);
			String description=textField.getText().toString();
			Log.d("Description",description);
			textField.setText("");
			//creating text file
			File describedPhoto=photos[current];
			String PATH=describedPhoto.toString();
			int index=PATH.lastIndexOf(".");
			PrintWriter writer=new PrintWriter(PATH.substring(0,index)+".txt","UTF-8");
			writer.println(description);
			writer.close();
			Log.d("DescriptionPATH",PATH.substring(0,index)+".txt");

		}catch(Exception e)
		{
			e.printStackTrace();
		}
		if(view==findViewById(R.id.button2))
		{
			current++;
			current%=photos.length;
		}
		else if(view==findViewById(R.id.button3))
		{
			current--;
			current+=photos.length;
			current%=photos.length;
		}
		try
		{
			File imgFile=new File(String.valueOf(photos[current]));
			Log.d("ShowedFile",photos[current].toString());
			if(imgFile.exists())
			{
				Bitmap myBitmap=getRotatedBitmap(imgFile);//BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				ImageView myImage=(ImageView)findViewById(R.id.imageView);
				int nh=(int)(myBitmap.getHeight()*(512.0/myBitmap.getWidth()));
				Bitmap scaled=Bitmap.createScaledBitmap(myBitmap,512,nh,true);
				myImage.setImageBitmap(scaled);
			}
			File describedPhoto=photos[current];
			String PATH=describedPhoto.toString();
			int index=PATH.lastIndexOf(".");
			File descriptionFile=new File(PATH.substring(0,index)+".txt");
			if(descriptionFile.exists())
			{
				FileInputStream fis=new FileInputStream(descriptionFile);
				byte[] data=new byte[(int)descriptionFile.length()];
				//noinspection ResultOfMethodCallIgnored
				fis.read(data);
				fis.close();
				String description=new String(data,"UTF-8");
				TextView textField=(TextView)findViewById(R.id.editText);
				textField.setText(description);
			}
		}catch(Exception e)
		{
			Log.e("Return File Exception",e.toString());
		}
	}

	public void selectDirectory(View view)
	{
		Intent fileIntent=new Intent(Intent.ACTION_GET_CONTENT);
		fileIntent.setType("file/*"); // intent type to filter application based on your requirement
		Intent intent=new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		int REQUEST_CODE=0xDEAD;
		startActivityForResult(Intent.createChooser(intent,"Select Picture"),REQUEST_CODE);
	}
}