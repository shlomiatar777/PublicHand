package com.example.shlomi.publichand;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import Logic.FirebaseAuthenticationController;
import Logic.FirebaseDB_Controller;
import Logic.Product;
import Logic.Sell;

public class CreateProductActivity extends AppCompatActivity {
    FirebaseAuthenticationController fbAuth;
    FirebaseStorage fbStore;
    FirebaseDB_Controller dbController;
    ArrayAdapter<String> AllCategoriesAdapter, FurnitureAdapter,  JewelryAdapter, ElectronicDevicesAdapter,  CellPhonesAdapter, BicyclesAdapter;
    Spinner categorySpn;
    Spinner subCategorySpn;
    ImageView selectedImage;
    Bitmap b;
    Product theProduct;
    TableRow tblTitle, tblDescription, tblPrice, tblCategories, tblSubCategories;
    LinearLayout llImage , llSubmit , llAddPicture;
    TextView validationTxt;
    boolean isKey;

    String key;
    final static int LOAD_EXIST_IMAGE =1;
    final static int LOAD_NEW_IMAGE =2;
    final static  int HALF_HOUR_BEFORE_END =60*60*47+60*30;

    private int width,height;
    Activity thisActivity= this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  screen size parameters
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;


        //init global params
        fbAuth = new FirebaseAuthenticationController();
        fbStore = FirebaseStorage.getInstance();
        dbController = new FirebaseDB_Controller();
        isKey=false;
        final int FONT_SIZE_LABEL =height/85;


        //setting main layout
        LinearLayout llMain = new LinearLayout(this);
        llMain.setOrientation(LinearLayout.VERTICAL);
        final Drawable image = ContextCompat.getDrawable(this,R.drawable.background2);
        llMain.setBackgroundDrawable(image);
        setContentView(llMain);


        //setting the sub views
        TableLayout tblAllActions = new TableLayout(this);
        tblAllActions.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        tblTitle = fillTableRow(getString(R.string.title), FONT_SIZE_LABEL, Color.BLACK, Typeface.BOLD);
        tblDescription = fillTableRow(getString(R.string.description), FONT_SIZE_LABEL, Color.BLACK, Typeface.BOLD);
        tblPrice = fillTableRow(getString(R.string.start_price), FONT_SIZE_LABEL, Color.BLACK, Typeface.BOLD);


        //setting the image views
        llImage = new LinearLayout(this);
        llImage.setOrientation(LinearLayout.VERTICAL);
        llImage.setGravity(Gravity.CENTER);

        //get pictures from the gallery of the phone
        selectedImage = new ImageView(this);
        Button galleryBtn = new Button(this);
        galleryBtn.setText(getString(R.string.choose_picture));
        galleryBtn.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, LOAD_EXIST_IMAGE);
            }
        });

        Button cameraBtn = new Button(this);
        cameraBtn.setText(getString(R.string.make_picture));
        cameraBtn.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                if (checkSelfPermission(Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                            LOAD_NEW_IMAGE);
                }
                else {
                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(i, LOAD_NEW_IMAGE);
                }
            }
        });


        llAddPicture = new LinearLayout(this);
        llAddPicture.setOrientation(LinearLayout.HORIZONTAL);
        llAddPicture.setGravity(Gravity.CENTER);

        llAddPicture.addView(galleryBtn);
        llAddPicture.addView(cameraBtn);

        llImage.addView(llAddPicture);
        llImage.addView(selectedImage);


        //setting the spinners
        tblCategories = makeTableRow();
        TextView categoryTxt = makeTextView(getString(R.string.category), FONT_SIZE_LABEL, Color.BLACK, Typeface.BOLD);
        categorySpn = new Spinner(this);


        tblSubCategories = makeTableRow();
        TextView subCategoryTxt =  makeTextView(getString(R.string.sub_category), FONT_SIZE_LABEL, Color.BLACK, Typeface.BOLD);
        subCategorySpn = new Spinner(this);

        String [] allCategories = {getString(R.string.furniture),getString(R.string.jewelry)
                ,getString(R.string.electronic_devices), getString(R.string.cell_phones),getString(R.string.bicycles)};

        String [] allFurniture = {getString(R.string.chairs), getString(R.string.tables), getString(R.string.sofas)
                , getString(R.string.closets), getString(R.string.others)};

        String [] allJewelry = {getString(R.string.necklace), getString(R.string.earrings), getString(R.string.rings),
                getString(R.string.bracelets), getString(R.string.others)};

        String [] allElectronicDevices = {getString(R.string.televisions), getString(R.string.ovens), getString(R.string.computers),
                getString(R.string.refrigerator), getString(R.string.washing_machines), getString(R.string.microwaves),
                getString(R.string.others)};

        String [] allCellPhones = {getString(R.string.iphone), getString(R.string.samsung_galaxy), getString(R.string.lg)
                , getString(R.string.others)};

        String [] allBicycles = {getString(R.string.bmx), getString(R.string.mountain_bikes), getString(R.string.electronic_bikes)
                , getString(R.string.others)};

        AllCategoriesAdapter=   makeAdapter(allCategories);
        FurnitureAdapter=   makeAdapter(allFurniture);
        JewelryAdapter=   makeAdapter(allJewelry);
        ElectronicDevicesAdapter=   makeAdapter(allElectronicDevices);
        CellPhonesAdapter=   makeAdapter(allCellPhones);
        BicyclesAdapter=   makeAdapter(allBicycles);


        categorySpn.setAdapter(AllCategoriesAdapter);
        categorySpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        subCategorySpn.setAdapter(FurnitureAdapter);
                        break;
                    case 1:
                        subCategorySpn.setAdapter(JewelryAdapter);
                        break;
                    case 2:
                        subCategorySpn.setAdapter(ElectronicDevicesAdapter);
                        break;
                    case 3:
                        subCategorySpn.setAdapter(CellPhonesAdapter);
                        break;
                    case 4:
                        subCategorySpn.setAdapter(BicyclesAdapter);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        tblCategories.addView(categoryTxt);
        tblCategories.addView(categorySpn);

        tblSubCategories.addView(subCategoryTxt);
        tblSubCategories.addView(subCategorySpn);



        tblAllActions.addView(tblTitle);
        tblAllActions.addView(tblDescription);
        tblAllActions.addView(tblCategories);
        tblAllActions.addView(tblSubCategories);
        tblAllActions.addView(tblPrice);

        //make new product and add to firebase database
        llSubmit = new LinearLayout(this);
        llSubmit.setOrientation(LinearLayout.HORIZONTAL);
        Button submitBtn = new Button(this);
        submitBtn.setText(getString(R.string.submit));
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidation()){
                    if (!isKey) {
                        key = dbController.makeKey(categorySpn, subCategorySpn);
                        isKey = true ;
                    }
                    if(b==null) {
                       setImage(null,key);
                    }
                    else {
                        uploadToStorage(key);
                    }

                }
            }
        });

        llSubmit.addView(submitBtn);


        validationTxt = new TextView(this);
        validationTxt.setTextColor(Color.RED);

        llMain.addView(tblAllActions);
        llMain.addView(llImage);
        llMain.addView(llSubmit);
        llMain.addView(validationTxt);

    }


     //generic method for create TextView
    public TextView makeTextView(String str, int fontSize, int color, int typeFace){
        TextView txt = new TextView(this);
        txt.setText(str);
        txt.setTextSize(fontSize);
        txt.setTextColor(color);
        txt.setTypeface(null,typeFace);

        return  txt;
    }



     //generic method for create TableRow
    public TableRow makeTableRow(){
        TableRow tableRow = new TableRow(this);
        tableRow.setOrientation(LinearLayout.HORIZONTAL);
        tableRow.setGravity(Gravity.CENTER_HORIZONTAL);
        return tableRow;
    }



     //generic method for create ArrayAdapter<String>
    public ArrayAdapter<String> makeAdapter(String [] arr)
    {
        ArrayList<String> arrLst = new ArrayList<>();
        int i;
        for (i =0 ; i < arr.length ; i++){
            arrLst.add(arr[i]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this , android.R.layout.simple_spinner_item,arrLst);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }



    //generic method for UI of tableRow
    public TableRow fillTableRow(String str, int size, int color, int typeFace){
        TableRow tableRow = makeTableRow();
        tableRow.setPadding(0,width/27,0,0);
        TextView tv = makeTextView(str,size,color,typeFace);
        EditText et = new EditText(this);
        et.setBackgroundColor(Color.WHITE);
        et.setSingleLine();
        et.setTextSize(2*size/3);
        et.setMinWidth(3*width/5);
        et.setMaxWidth(3*width/5);
        tableRow.addView(tv);
        tableRow.addView(et);
        return tableRow;
    }



    // check validation input (price must be int , title must be else then ""
    public boolean isValidation(){
        String price =((EditText) tblPrice.getChildAt(1)).getText()+"";
        String title =((EditText) tblTitle.getChildAt(1)).getText()+"";
        if ( price.equals("") || !(price.matches("[0-9]+"))) {
            validationTxt.setText(getString(R.string.enter_natural_number));
            return false;
        }
        if (title.equals("")) {
            validationTxt.setText(getString(R.string.please_enter_title));
            return false;
        }

        return true;
    }



    //create product from the user input
    public Product makeProduct (){
        final Product theProduct;
        String title =((EditText) tblTitle.getChildAt(1)).getText()+"";
        String description =((EditText) tblDescription.getChildAt(1)).getText()+"";
        int price =Integer.parseInt(((EditText) tblPrice.getChildAt(1)).getText()+"");
        String categoryStr =  categorySpn.getSelectedItem().toString();
        String subCategoryStr =  subCategorySpn.getSelectedItem().toString();
        String userID = fbAuth.getCurrentUid();
        theProduct= new Product(userID , categoryStr , subCategoryStr , description , "" , price, title);
        return theProduct;
    }



    public void setImage(Uri uri, String key){

        theProduct = makeProduct();
        if (uri !=null) {
            theProduct.setImage(b.getByteCount() + "");
        }
        Sell theSell = new Sell(theProduct, new Date(), "", key);
        dbController.saveSell(theProduct, theSell, key);
        scheduleNotification(HALF_HOUR_BEFORE_END);  //47.5 hours
        Intent intentData = new Intent(thisActivity,SellProductActivity.class);
        String fromJsonToStr = new Gson().toJson(theSell);
        intentData.putExtra(getString(R.string.the_sell),fromJsonToStr);
        finish();
        startActivity(intentData);
    }

    public void uploadToStorage (final String key){
        StorageReference storeRef = fbStore.getReferenceFromUrl(getString(R.string.product_path)+key+getString(R.string.jpg));
        ByteArrayOutputStream bitMapToByte =  new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG,0,bitMapToByte);
        byte[] byteArr = bitMapToByte.toByteArray();
        UploadTask uploadTask = storeRef.putBytes(byteArr);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri uri = taskSnapshot.getDownloadUrl();
                setImage(uri,key);
            }
        });
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                validationTxt.setText(getString(R.string.upload_image_failed));
            }
        });
    }


    //delay is after how much time(in millis) from current time you want to schedule the notification
    public void scheduleNotification(int delay) {
      AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
      Intent intent = new Intent(getString(R.string.display_notification));
        intent.addCategory(getString(R.string.category_default));
        int requestCode = 100;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, delay);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

    }


    //create tool bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return fbAuth.createToolBar(this, menu);
    }



    //create options in tool bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click

        super.onOptionsItemSelected(item);
        return  fbAuth.executeToolBarOption(this,item);

    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOAD_NEW_IMAGE) {
            if ( grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(i, LOAD_NEW_IMAGE);
            }
        }
    }



    //check if we got a picture from the gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOAD_EXIST_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImageUri = data.getData();
            try {
                b = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
           Bitmap bResize = Bitmap.createScaledBitmap(b,width/2,height/4,false);
            selectedImage.setImageBitmap(bResize);

        }
        else if (requestCode == LOAD_NEW_IMAGE && resultCode == RESULT_OK){
            b = (Bitmap) data.getExtras().get(getString(R.string.data));
            Bitmap bResize = Bitmap.createScaledBitmap(b,width/2,height/4,false);
            selectedImage.setImageBitmap(bResize);
        }
    }





}
