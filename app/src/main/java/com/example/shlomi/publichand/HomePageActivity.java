package com.example.shlomi.publichand;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;


public class HomePageActivity extends AppCompatActivity {

    FirebaseAuth fbAuth;
    Activity thisActivity = this;
    LinearLayout llSearchProduct, llCreateProduct, llOffersList , llSellsList;
    int width , height;




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
        fbAuth =FirebaseAuth.getInstance();

        //check if the is already connect user
        if(fbAuth.getCurrentUser()==null){
            Intent intentData = new Intent(this,LoginActivity.class);
            finish();
            startActivity(intentData);
        }

        //setting main layout
        LinearLayout llMain = new LinearLayout(this);
        llMain.setOrientation(LinearLayout.VERTICAL);
        llMain.setGravity(Gravity.CENTER_HORIZONTAL);
        Drawable image = ContextCompat.getDrawable(this,R.drawable.background2);
        llMain.setBackgroundDrawable(image);
        setContentView(llMain);

        //setting title
        TextView titleLbl =new TextView(this);
        titleLbl.setText(getString(R.string.choose_action));
        titleLbl.setTextColor(Color.BLACK);
        titleLbl.setTextSize(height/45);
        titleLbl.setTypeface(null, Typeface.BOLD);
        titleLbl.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        llMain.addView(titleLbl);

        //UI and logic of all actions
        showAllAction();

        //locate all actions
        TableLayout tblAllActions = new TableLayout(this);
        tblAllActions.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TableRow bottomRow = makeTableRow(llSearchProduct, llCreateProduct);
        TableRow topRow = makeTableRow(llOffersList, llSellsList);
        bottomRow.setPadding(0,height/10,0,0);

        tblAllActions.addView(topRow);
        tblAllActions.addView(bottomRow);


        llMain.addView(tblAllActions);

    }


    //UI and logic of all actions
    public void showAllAction(){

        //init local params
        final int HEIGHT_BTN = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,(int) (height/30) , getResources().getDisplayMetrics());
        final int WIDTH_BTN = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,(int) (width/8), getResources().getDisplayMetrics());
        final int FONT_SIZE = HEIGHT_BTN/10;

        //search product action
        llSearchProduct = makeLayout();
        ImageView searchProductImg = makeImage(R.drawable.search);
        Button searchProductBtn = makeButton(getString(R.string.search_product), FONT_SIZE, WIDTH_BTN, HEIGHT_BTN);
        searchProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentData = new Intent(thisActivity,SearchProductActivity.class);
                startActivity(intentData);
            }
        });
        llSearchProduct.addView(searchProductImg);
        llSearchProduct.addView(searchProductBtn);

        //create product action
        llCreateProduct = makeLayout();
        ImageView createProductIm = makeImage(R.drawable.create);
        Button createProductBtn = makeButton(getString(R.string.create_product), FONT_SIZE, WIDTH_BTN, HEIGHT_BTN);
        createProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentData = new Intent(thisActivity,CreateProductActivity.class);
                startActivity(intentData);
            }
        });
        llCreateProduct.addView(createProductIm);
        llCreateProduct.addView(createProductBtn);

        //show all my offers list action
        llOffersList = makeLayout();
        ImageView myOffersListImg = makeImage(R.drawable.buy);
        Button myOffersListBtn = makeButton(getString(R.string.show_my_offers), FONT_SIZE, WIDTH_BTN, HEIGHT_BTN);
        myOffersListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentData = new Intent(thisActivity,ResultSearchActivity.class);
                intentData.putExtra(getString(R.string.type_result), ResultSearchActivity.SearchType.SHOW_MY_OFFERS.ordinal());
                startActivity(intentData);
            }
        });
        llOffersList.addView(myOffersListImg);
        llOffersList.addView(myOffersListBtn);

        //show all my sells list action
        llSellsList = makeLayout();
        ImageView mySellsListImg = makeImage(R.drawable.sell);
        Button mySellsListBtn = makeButton(getString(R.string.show_my_sells), FONT_SIZE, WIDTH_BTN, HEIGHT_BTN);
        mySellsListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentData = new Intent(thisActivity,ResultSearchActivity.class);
                intentData.putExtra(getString(R.string.type_result), ResultSearchActivity.SearchType.SHOW_MY_SELLS.ordinal());
                startActivity(intentData);
            }
        });
        llSellsList.addView(mySellsListImg);
        llSellsList.addView(mySellsListBtn);

    }


    //create Layout
    public LinearLayout makeLayout(){

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);

        return  ll;
    }


    ////generic method for create ImageView
    public ImageView makeImage(int id){
        ImageView img = new ImageView(this);
        img.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Drawable drawable = ContextCompat.getDrawable(this, id);
        Bitmap b = ((BitmapDrawable)drawable).getBitmap();
        Bitmap bResize = Bitmap.createScaledBitmap(b,width/5,height/5,false);
        Drawable d = new BitmapDrawable(getResources(),bResize);
        img.setBackgroundDrawable(d);
        return img;
    }


    ////generic method for create Button
    public  Button makeButton (String str, int fontSize , int widthBtn , int heightBtn ){

        Button theButton = new Button(this);
        theButton.setText(str);
        theButton.setTextSize(fontSize);
        theButton.setTypeface(null, Typeface.BOLD);
        theButton.setLayoutParams(new TableRow.LayoutParams(widthBtn, heightBtn));
        return  theButton;
    }


    ////generic method for create TableRow
    public TableRow makeTableRow(LinearLayout ll1 ,LinearLayout ll2){
    TableRow tableRow = new TableRow(this);
    tableRow.setGravity(Gravity.CENTER_HORIZONTAL);
    tableRow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    tableRow.addView(ll1);
    tableRow.addView(ll2);
    return tableRow;
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
            Intent intentData = new Intent(thisActivity,LoginActivity.class);
            finish();
            startActivity(intentData);
        }
        return true;

    }



}
