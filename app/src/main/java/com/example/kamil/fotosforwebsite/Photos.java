package com.example.kamil.fotosforwebsite;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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


public class Photos extends AppCompatActivity
{
	private static final String TAG="Photos";
	private File photoDirectory;
	private File[] photos;
	private int current;
	private final static int REQUEST_IMAGES_DIRECTORY=1;
	private final static int REQUEST_IMAGES_FILE=2;
	private final static String LAST_DIRECTORY_PREFERENCE="LAST_OPENED";
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

	@SuppressLint("NewApi")
	private File getRealPathFromURI(final Uri contentUri)
	{
		final String[] project={"_data"};
		try(Cursor cursor=getApplicationContext().getContentResolver()
				.query(contentUri,project,     // Which columns to return
				null,     // WHERE clause; which rows to return (all rows)
				null,     // WHERE clause selection arguments (none)
						null))
		{
			if(cursor==null)
				throw new AssertionError("cursor is null");
			final int column_index=cursor.getColumnIndexOrThrow("_data");
			if(cursor.moveToFirst())
			{
				String extracted=cursor.getString(column_index);
				final int index=extracted.lastIndexOf('/');
				return new File(extracted.substring(0,index));
			}
			throw new AssertionError("Cannot move cursor");
		}
	}


	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(final int requestCode,final int resultCode,final Intent data)
	{
		switch(requestCode)
		{
			case REQUEST_IMAGES_DIRECTORY:
			case REQUEST_IMAGES_FILE:
				if(resultCode!=RESULT_OK)
					return;
				current=0;
				final Uri uri=data.getData();
				if(requestCode==REQUEST_IMAGES_DIRECTORY)
					photoDirectory=new File(FileUtil.getFullPathFromTreeUri(uri,this));
				else
					photoDirectory=getRealPathFromURI(uri);
				//	final String PATH=;
			//	if(PATH==null)
			//		throw new AssertionError("PATH is null");
			//	Log.e(TAG,"ABC"+PATH+"DEF");
			//	final int index=PATH.lastIndexOf('/');
			//	photoDirectory=new File(PATH.substring(0,index));
				//getPreferences(MODE_PRIVATE).edit().putString(LAST_DIRECTORY_PREFERENCE,photoDirectory).commit();
			//	Log.e(TAG,"files:"+photoDirectory);
			//	Log.e(TAG,"files:"+photoDirectory.listFiles());
				photos=photoDirectory.listFiles(new FilenameFilter()
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
				final File imgFile=photos[current];
				if(imgFile.exists())
				{
					final Bitmap myBitmap=getRotatedBitmap(imgFile);
					final ImageView myImage=(ImageView)findViewById(R.id.imageView);
					final int nh=(int)(myBitmap.getHeight()*(512.0/myBitmap.getWidth()));
					final Bitmap scaled=Bitmap.createScaledBitmap(myBitmap,512,nh,true);
					myImage.setImageBitmap(scaled);
				}
				final String PATH2=photos[current].toString();
				final int index2=PATH2.lastIndexOf('.');
				final File descriptionFile=new File(PATH2.substring(0,index2)+".txt");
				if(descriptionFile.exists())
				{
					try(FileInputStream fis=new FileInputStream(descriptionFile))
					{
						final byte[] description=new byte[(int)descriptionFile.length()];
						fis.read(description);
						((TextView)findViewById(R.id.editText)).setText(
								new String(description,"UTF-8"));
					}catch(final IOException e)
					{
						e.printStackTrace();
					}
				}
				return;
			default:
				super.onActivityResult(requestCode,resultCode,data);
		}

	}

	@SuppressLint("NewApi")
	public void nextPhoto(final View view)
	{
		if(photoDirectory==null)
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
		/*if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
		{
			final Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
			startActivityForResult(Intent.createChooser(intent,"Select Picture"),
					REQUEST_IMAGES_DIRECTORY);
		}
		else
		{*/
			final Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(Intent.createChooser(intent,"Select Picture"),
					REQUEST_IMAGES_FILE);
		//}
			//String lastAccessed=getPreferences(MODE_PRIVATE).getString(LAST_DIRECTORY_PREFERENCE,null);
		//
		//if(lastAccessed!=null)
		//	intent.setDataAndType(Uri.parse(lastAccessed),"image/*");

	}
}
























