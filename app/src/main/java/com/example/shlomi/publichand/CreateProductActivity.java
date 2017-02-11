package com.example.shlomi.publichand;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;

import Logic.Product;
import Logic.Sell;

public class CreateProductActivity extends AppCompatActivity {
    FirebaseAuth fbAuth;
    DatabaseReference dbref;
    ArrayAdapter<String> AllCategoriesAdapter, FurnitureAdapter,  JewelryAdapter, ElectronicDevicesAdapter,  CellPhonesAdapter, BicyclesAdapter;
    Spinner categorySpn;
    Spinner subCategorySpn;
    ImageView selectedImage;
    Bitmap b;
    TableRow tblTitle, tblDescription, tblPrice, tblCategories, tblSubCategories;
    LinearLayout llImage , llSubmit;
    TextView validationTxt;
    final static int RESULT_LOAD_IMAGE =1;
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
        dbref= FirebaseDatabase.getInstance().getReference();
        fbAuth =FirebaseAuth.getInstance();
        final int FONT_SIZE_LABEL =height/85;


        //setting main layout
        LinearLayout llMain = new LinearLayout(this);
        llMain.setOrientation(LinearLayout.VERTICAL);
        Drawable image = ContextCompat.getDrawable(this,R.drawable.background2);
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
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        llImage.addView(galleryBtn);
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
                Product theProduct;
                Sell theSell;
                if (isValidation()){
                    theProduct = makeProduct();
                    String key =  dbref.child(theProduct.getCategory()).child(theProduct.getSubCategory()).push().getKey();
                    theSell = new Sell(theProduct, new Date(), "", key);
                    dbref.child(theProduct.getCategory()).child(theProduct.getSubCategory()).child(key).setValue(theSell);
                    Intent intentData = new Intent(thisActivity,SellProductActivity.class);
                    String fromJsonToStr = new Gson().toJson(theSell);
                    intentData.putExtra(getString(R.string.the_sell),fromJsonToStr);
                    finish();
                   startActivity(intentData);
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
        Product theProduct;
        String title =((EditText) tblTitle.getChildAt(1)).getText()+"";
        String description =((EditText) tblDescription.getChildAt(1)).getText()+"";
        int price =Integer.parseInt(((EditText) tblPrice.getChildAt(1)).getText()+"");
        String categoryStr =  categorySpn.getSelectedItem().toString();
        String subCategoryStr =  subCategorySpn.getSelectedItem().toString();
        String image;
        if (b==null) {
            image ="";
        }
        else{
            ByteArrayOutputStream bitMapToByte =  new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.PNG, 100,bitMapToByte);
            b.recycle();
            byte[] byteArr = bitMapToByte.toByteArray();
            image = Base64.encodeToString(byteArr , Base64.DEFAULT);
        }
       String userID = fbAuth.getCurrentUser().getUid();

        theProduct= new Product(userID , categoryStr , subCategoryStr , description , image , price, title);
        return theProduct;
    }



    //create tool bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }



    //create options in tool bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click

        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.edit){
            Intent intentData = new Intent(thisActivity,SettingUserDetailsActivity.class);
            startActivity(intentData);
        }


        else if(item.getItemId() == R.id.sign_out){
            fbAuth.signOut();
            Intent intentData = new Intent(this,LoginActivity.class);
            intentData.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intentData);
        }
        return true;

    }


    //check if we got a picture from the gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImageUri = data.getData();
            try {
                b = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
           Bitmap bResize = Bitmap.createScaledBitmap(b,width/2,height/4,false);
            selectedImage.setImageBitmap(bResize);
        }
    }
}
