package com.example.kamil.fotosforwebsite;

import android.content.Intent;
import android.database.Cursor;
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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Comparator;


public class Fotos extends AppCompatActivity
{
	private String photoDirectory="";
	private File[] photos;
	private int current;

	private static Bitmap getRotatedBitmap(final File imgFile)
	{
		Bitmap myBitmap=BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		try
		{
			final ExifInterface exif=new ExifInterface(imgFile.getAbsolutePath());
			final int orientation=exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,1);
			final Matrix matrix=new Matrix();
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
		}catch(final IOException e)
		{
			e.printStackTrace();
		}
		return myBitmap;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fotos);
	}

	private String getRealPathFromURI(final Uri contentUri)
	{
		final String[] project={MediaStore.MediaColumns.DATA};
		try(Cursor cursor=getApplicationContext().getContentResolver()
				.query(contentUri,project,     // Which columns to return
				null,     // WHERE clause; which rows to return (all rows)
				null,     // WHERE clause selection arguments (none)
						null))
		{
			assert cursor!=null;
			final int column_index=cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
	}

	@Override
	protected void onActivityResult(final int requestCode,final int resultCode,final Intent data)
	{
		if(requestCode==0xDEAD)
		{
			if(resultCode==RESULT_OK)
			{
				current=0;
				final Uri uri=data.getData();
				final String PATH=getRealPathFromURI(uri);
				final int index=PATH.lastIndexOf('/');
				photoDirectory=PATH.substring(0,index);
				photos=new File(photoDirectory).listFiles(new FilenameFilter()
				{
					@Override
					public boolean accept(final File dir,final String name)
					{
						return name.toLowerCase().endsWith(".jpg");
					}
				});
				Arrays.sort(photos,new Comparator<File>()
				{
					@Override
					public int compare(final File lhs,final File rhs)
					{
						return lhs.getName().compareTo(rhs.getName());
					}
				});
				final File imgFile=new File(String.valueOf(photos[current]));
				Log.d("ShowedFile",photos[current].toString());
				if(imgFile.exists())
				{
					final Bitmap myBitmap=getRotatedBitmap(imgFile);
					final ImageView myImage=(ImageView)findViewById(R.id.imageView);
					final int nh=(int)(myBitmap.getHeight()*(512.0/myBitmap.getWidth()));
					final Bitmap scaled=Bitmap.createScaledBitmap(myBitmap,512,nh,true);
					myImage.setImageBitmap(scaled);
				}
			}
		}
	}

	public void nextPhoto(final View view)
	{
		if(photoDirectory.isEmpty())
			return;
		final String PATH=photos[current].toString();
		final int index=PATH.lastIndexOf('.');
		try(PrintWriter writer=new PrintWriter(PATH.substring(0,index)+".txt","UTF-8"))
		{
			final TextView textField=(TextView)findViewById(R.id.editText);
			writer.println(textField.getText());
			textField.setText("");
		}catch(FileNotFoundException|UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		if(view.equals(findViewById(R.id.button2)))
		{
			current++;
			current%=photos.length;
		}
		else if(view.equals(findViewById(R.id.button3)))
		{
			current--;
			current+=photos.length;
			current%=photos.length;
		}
		final File imgFile=new File(String.valueOf(photos[current]));
		if(imgFile.exists())
		{
			final Bitmap myBitmap=getRotatedBitmap(imgFile);
			final int nh=(int)(myBitmap.getHeight()*(512.0/myBitmap.getWidth()));
			final Bitmap scaled=Bitmap.createScaledBitmap(myBitmap,512,nh,true);
			((ImageView)findViewById(R.id.imageView)).setImageBitmap(scaled);
		}
		final String PATH2=photos[current].toString();
		final int index2=PATH2.lastIndexOf('.');
		final File descriptionFile=new File(PATH2.substring(0,index2)+".txt");
		if(descriptionFile.exists())
		{
			try(FileInputStream fis=new FileInputStream(descriptionFile))
			{
				final byte[] data=new byte[(int)descriptionFile.length()];
				fis.read(data);
				((TextView)findViewById(R.id.editText)).setText(new String(data,"UTF-8"));
			}catch(final IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void selectDirectory(@SuppressWarnings("UnusedParameters") final View ignored)
	{
		final Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		final int REQUEST_CODE=0xDEAD;
		startActivityForResult(Intent.createChooser(intent,"Select Picture"),REQUEST_CODE);
	}
}