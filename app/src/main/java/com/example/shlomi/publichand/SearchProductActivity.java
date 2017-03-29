package com.example.shlomi.publichand;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import Logic.FirebaseAuthenticationController;

public class SearchProductActivity extends AppCompatActivity {
    FirebaseAuthenticationController fbAuth;
    ArrayAdapter<String> AllCategoriesAdapter, FurnitureAdapter,  JewelryAdapter,
            ElectronicDevicesAdapter,  CellPhonesAdapter, BicyclesAdapter;
    Spinner categorySpn;
    Spinner subCategorySpn;
    TableRow tblCategories, tblSubCategories;
    LinearLayout llPrice;
    TextView errorLbl;
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
        fbAuth =new FirebaseAuthenticationController();
        final int FONT_SIZE_LABEL =height/85;


        //setting main layout
        LinearLayout llMain = new LinearLayout(this);
        llMain.setOrientation(LinearLayout.VERTICAL);
        Drawable image = ContextCompat.getDrawable(this,R.drawable.background2);
        llMain.setBackgroundDrawable(image);
        setContentView(llMain);


        //setting the sub views
        llPrice = initRegisterLayout(getString(R.string.from_price) , getString(R.string.until_price), FONT_SIZE_LABEL, Color.BLACK,Color.WHITE, Typeface.BOLD);
        errorLbl = new TextView(this);
        errorLbl.setTextColor(Color.RED);


        TableLayout tblAllActions = new TableLayout(this);
        tblAllActions.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));



        //setting the spinners
        tblCategories = makeTableRow();
        TextView categoryTxt = makeTextView(getString(R.string.category), FONT_SIZE_LABEL, Color.BLACK, Typeface.BOLD);
        categorySpn = new Spinner(this);

        tblSubCategories = makeTableRow();
        TextView subCategoryTxt = makeTextView(getString(R.string.sub_category), FONT_SIZE_LABEL, Color.BLACK, Typeface.BOLD);
        subCategorySpn = new Spinner(this);

        tblAllActions.addView(tblCategories);
        tblAllActions.addView(tblSubCategories);

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


        //send a details search from user input , can limit minimum price ,maximum price , both of them , or none of them
        Button searchBtn = new Button(this);
        searchBtn.setText(getString(R.string.search));
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidation()){
                    Intent intentData = new Intent(thisActivity,ResultSearchActivity.class);
                    intentData.putExtra(getString(R.string.category_intent),((Spinner) tblCategories.getChildAt(1)).getSelectedItem()+"");
                    intentData.putExtra(getString(R.string.sub_category_intent),((Spinner) tblSubCategories.getChildAt(1)).getSelectedItem()+"");
                    intentData.putExtra(getString(R.string.type_result), ResultSearchActivity.SearchType.SEARCH_PRODUCT.ordinal());
                    if (!(((EditText)llPrice.getChildAt(1)).getText()+"").equals(""))
                        intentData.putExtra(getString(R.string.min_price),Integer.parseInt(((TextView)llPrice.getChildAt(1)).getText()+""));
                    if (!(((EditText)llPrice.getChildAt(3)).getText()+"").equals(""))
                        intentData.putExtra(getString(R.string.max_price),Integer.parseInt(((TextView)llPrice.getChildAt(3)).getText()+""));
                    startActivity(intentData);
                }
            }
        });
        llMain.addView(tblAllActions);
        llMain.addView(llPrice);
        llMain.addView(searchBtn);
        llMain.addView(errorLbl);

    }



    //generic method for make EditText
    public EditText makeTxt (int color ,String hint,int size, int txtWidth){
        EditText txt = new EditText(this);
        txt.setBackgroundColor(color);
        txt.setSingleLine();
        txt.setTextSize(size);
        txt.setMinWidth(txtWidth);
        txt.setMaxWidth(txtWidth);
        txt.setHint(hint);
        return txt;
    }



    //generic method for make TextView
    public TextView makeTextView(String str, int fontSize, int color, int typeFace){
        TextView txt = new TextView(this);
        txt.setText(str);
        txt.setTextSize(fontSize);
        txt.setTextColor(color);
        txt.setTypeface(null,typeFace);

        return  txt;
    }



    //generic method for make TableRow
    public TableRow makeTableRow(){
        TableRow tableRow = new TableRow(this);
        tableRow.setOrientation(LinearLayout.HORIZONTAL);
        tableRow.setGravity(Gravity.CENTER_HORIZONTAL);
        return tableRow;
    }



    //generic method for make ArrayAdapter<String>
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



    //generic method for make LinearLayout
    public LinearLayout initRegisterLayout (String str1, String str2, int fontSize, int colorTxT, int colorBackground, int typeFace){
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);

        TextView tv1 = makeTextView(str1, fontSize, colorTxT, typeFace);
        EditText et1 = makeTxt(colorBackground,"", 2*fontSize/3, width/8);


        TextView tv2 = makeTextView(str2, fontSize, colorTxT, typeFace);
        EditText et2 = makeTxt(colorBackground,"", 2*fontSize/3, width/8);

        ll.addView(tv1);
        ll.addView(et1);
        tv2.setPadding(width/15,0,0,0);
        ll.addView(tv2);
        ll.addView(et2);
        return ll;
    }


    //check validation of user inputs in minimum price and maximum price fields (can be empty or int, and min<max)
    public boolean isValidation(){
        boolean isValidMin=true;
        boolean isValidMax=true;

        String minPrice =((EditText)llPrice.getChildAt(1)).getText()+"";
        String maxPrice =((EditText)llPrice.getChildAt(3)).getText()+"";
        if (!minPrice.equals("")) {
            if (!(minPrice.matches("[0-9]+"))) {
                isValidMin = false;
                errorLbl.setText(getString(R.string.please_enter_number));
                return isValidMin;
            }
        }
        if (!maxPrice.equals("")) {
            if (!(maxPrice.matches("[0-9]+"))) {
                isValidMax = false;
                errorLbl.setText(getString(R.string.please_enter_number));
                return isValidMax;
            }
        }
        if (!maxPrice.equals("") && !minPrice.equals("")){
            if(Integer.parseInt(minPrice)>Integer.parseInt(maxPrice)) {
                errorLbl.setText(getString(R.string.prices_validation));
                return false;
            }
        }
        return (isValidMax && isValidMin);
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
}
