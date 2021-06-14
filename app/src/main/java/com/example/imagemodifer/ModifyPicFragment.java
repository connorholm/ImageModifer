package com.example.imagemodifer;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


public class ModifyPicFragment extends Fragment {
    String photo;
    DataBaseHandler databaseHandler;
    private SQLiteDatabase db;
    Bitmap theImage;
    private DataBaseHandler myDatabase;
    private ArrayList<LocalResponse> singleRowArrayList;
    private LocalResponse singleRow;
    private int rightClicks = 0;
    String image;
    int uid;
    Cursor cursor;
    int imageWidth = 200;
    int imageHeight = 200;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_modify_pic,container,false);
        ImageView image = view.findViewById(R.id.newsImage);
        Button rightButton = view.findViewById(R.id.rotateRight_btn);
        Button confirmButton = view.findViewById(R.id.finishModify_btn);

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rightClicks++;
                image.setRotation(rightClicks*-90);
            }
        });

        myDatabase = new DataBaseHandler(getContext());
        databaseHandler = new DataBaseHandler(getContext());
        db = myDatabase.getWritableDatabase();
        setData(image);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clicks = rightClicks;
                Toast.makeText(getContext(), "Clicks: "+clicks, Toast.LENGTH_SHORT).show();

                Matrix matrix = new Matrix();

                matrix.postRotate(-90*clicks);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(getBitmapFromEncodedString(singleRowArrayList.get(0).image), imageWidth, imageHeight, true);

                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                myDatabase.deleteAll();
                theImage = (Bitmap) rotatedBitmap;
                setDataToDataBase();
                ((MainActivity) getActivity()).loadFragment(new DisplayFragment(), true);
            }
        });
        return view;
    }

    private void setData(ImageView newsImage) {
        db = myDatabase.getWritableDatabase();
        singleRowArrayList = new ArrayList<>();
        String[] columns = {DataBaseHandler.KEY_ID, DataBaseHandler.KEY_IMG_URL};
        cursor = db.query(DataBaseHandler.TABLE_NAME, columns, null, null, null, null, null);
        while (cursor.moveToNext()) {

            int index1 = cursor.getColumnIndex(DataBaseHandler.KEY_ID);
            int index2 = cursor.getColumnIndex(DataBaseHandler.KEY_IMG_URL);
            uid = cursor.getInt(index1);
            image = cursor.getString(index2);
            singleRow = new LocalResponse(image,uid);
            singleRowArrayList.add(singleRow);
        }
        if (singleRowArrayList.size()==0){
            //empty.setVisibility(View.VISIBLE);
            Log.i("Testing","No pictures to display");
        }else {
           // LocalDataBaseAdapter localDataBaseResponse = new LocalDataBaseAdapter(getContext(), singleRowArrayList, db, myDatabase);
            newsImage.setImageBitmap(getBitmapFromEncodedString(singleRowArrayList.get(singleRowArrayList.size()-1).image));

        }
    }
    private Bitmap getBitmapFromEncodedString(String encodedString){

        byte[] arr = Base64.decode(encodedString, Base64.URL_SAFE);

        Bitmap img = BitmapFactory.decodeByteArray(arr, 0, arr.length);

        return img;

    }
    private String getEncodedString(Bitmap bitmap){

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG,100, os);

       /* or use below if you want 32 bit images

        bitmap.compress(Bitmap.CompressFormat.PNG, (0–100 compression), os);*/
        byte[] imageArr = os.toByteArray();

        return Base64.encodeToString(imageArr, Base64.URL_SAFE);

    }
    private void setDataToDataBase() {
        db = databaseHandler.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(databaseHandler.KEY_IMG_URL,getEncodedString(theImage));

        long id = db.insert(databaseHandler.TABLE_NAME, null, cv);
        if (id < 0) {
            Toast.makeText(getContext(), "Something went wrong. Please try again later...", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Add successful", Toast.LENGTH_LONG).show();
        }
    }
}