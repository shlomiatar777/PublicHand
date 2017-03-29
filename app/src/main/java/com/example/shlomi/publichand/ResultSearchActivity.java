package com.example.shlomi.publichand;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import Logic.FirebaseAuthenticationController;
import Logic.FirebaseDB_Controller;
import Logic.Product;
import Logic.Sell;

public class ResultSearchActivity extends AppCompatActivity {
    FirebaseAuthenticationController fbAuth;
    ProgressDialog progressDialog ;
    ArrayList<Sell> allDemandedSells;
    LinearLayout llAllCurrentResult;
    TextView currentPageLbl;
    FirebaseDB_Controller dbController;
    int currentPage;
    final int PRODUCTS_PER_PAGE =10;
    final int NOT_EXIST =-1;
    private int width,height;
    private String allSellsId;
    Date currentDate;
    Activity thisActivity = this;
    LinearLayout llPage;
    TextView noResultTxt ;
    Button previous, forward;
    public static enum SearchType {SEARCH_PRODUCT, SHOW_MY_OFFERS, SHOW_MY_SELLS }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_search);



        //  screen size parameters
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;


        //init global params
        final int FONT_SIZE_LABEL =height/85;
        final int FONT_SIZE_TEXT =2*FONT_SIZE_LABEL/3;


        fbAuth =new FirebaseAuthenticationController();
        progressDialog = new ProgressDialog(this);
        dbController = new FirebaseDB_Controller();
        allDemandedSells = new ArrayList<>();
        currentPage = 1;
        currentDate = new Date();
        allSellsId="";


        //setting main layout
        final LinearLayout llMain = (LinearLayout)findViewById(R.id.the_layout);
        llMain.setGravity(Gravity.CENTER);
        Drawable image = ContextCompat.getDrawable(this,R.drawable.background2);
        llMain.setBackgroundDrawable(image);


        //setting the sub views
        llAllCurrentResult = new LinearLayout(this);
        llAllCurrentResult.setOrientation(LinearLayout.VERTICAL);
        llAllCurrentResult.setGravity(Gravity.CENTER);


        llPage = new LinearLayout(this);
        llPage.setOrientation(LinearLayout.HORIZONTAL);
        llPage.setGravity(Gravity.CENTER);

        currentPageLbl = new TextView(this);
        currentPageLbl.setText(currentPage+"");
        currentPageLbl.setTextSize(FONT_SIZE_LABEL);

        //set buttons forward and preview
        previous = new Button(this);
        previous.setText(getString(R.string.right));

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage > 1){
                    currentPage--;
                    showCurrentPage();
                    currentPageLbl.setText(currentPage+"");
                }
            }
        });


        forward = new Button(this);
        forward.setText(getString(R.string.left));

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage*10 - allDemandedSells.size() < 0){
                    currentPage++;
                    showCurrentPage();
                    currentPageLbl.setText(currentPage+"");
                }
            }
        });

     //   llPage.addView(previous);
     //   llPage.addView(currentPageLbl);
     //   llPage.addView(forward);

        // get number from other activities , 1 means search products
        //                                    2 means search my offers on products
        //                                    3 means search my products which i sell
        final int searchType =getIntent().getIntExtra(getString(R.string.type_result),-1);
                switch (SearchType.values()[searchType]) {
                    case SEARCH_PRODUCT:
                        searchProduct();
                        break;

                    case SHOW_MY_OFFERS:
                        showMyOffers();
                        break;

                    case SHOW_MY_SELLS:
                        showMySells();
                        break;
                }


        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();
        llMain.addView(llAllCurrentResult);
        llMain.addView(llPage);
    }


    //  search products
    public void searchProduct(){
        final String category = getIntent().getStringExtra(getString(R.string.category_intent));
        final String subcategory = getIntent().getStringExtra(getString(R.string.sub_category_intent));
        final int minPrice = getIntent().getIntExtra(getString(R.string.min_price), NOT_EXIST);
        final int maxPrice = getIntent().getIntExtra(getString(R.string.max_price), NOT_EXIST);


        dbController.searchProduct(this , category, subcategory, minPrice, maxPrice);
    }



    // search my offers on products
    public void showMyOffers(){
        dbController.showMyOffers(this);
    }



    // search my products which i sell
    public void showMySells(){
        dbController.showMySells(this);
    }



    //UI of current page
     public void showCurrentPage(){
         llPage.removeAllViews();
         llAllCurrentResult.removeAllViews();
         int totalDemandedSells =  allDemandedSells.size();
         if (totalDemandedSells==0){
             noResultTxt =  new TextView(this);
             noResultTxt.setTextSize(height/66);
             noResultTxt.setText(getString(R.string.no_result));
             llAllCurrentResult.addView(noResultTxt);
             return;
         }
         if (totalDemandedSells<PRODUCTS_PER_PAGE ){
             llPage.addView(currentPageLbl);
         }
         else if (currentPage==1){
             llPage.addView(currentPageLbl);
             llPage.addView(forward);
         }
         else if ((currentPage)*PRODUCTS_PER_PAGE>totalDemandedSells){
             llPage.addView(previous);
             llPage.addView(currentPageLbl);
         }
         else{
             llPage.addView(previous);
             llPage.addView(currentPageLbl);
             llPage.addView(forward);
         }
         int i ;
         int limit;
         if(PRODUCTS_PER_PAGE*currentPage <= totalDemandedSells)
             limit = PRODUCTS_PER_PAGE;
         else limit = PRODUCTS_PER_PAGE - (PRODUCTS_PER_PAGE*currentPage -totalDemandedSells);
         for (i = 0 ; i <limit ; i++){
             final Sell currentSell = allDemandedSells.get(PRODUCTS_PER_PAGE*(currentPage-1)+i);
             TextView tv = new TextView(this);
             tv.setTextSize(height/66);
             tv.setText(currentSell.getTheProduct().getTitle());
             tv.setSingleLine();
             tv.setPadding(width/15,width/21 ,0,0);
             if (allSellsId.indexOf(currentSell.getId())!=NOT_EXIST)
                 tv.setTextColor(Color.BLUE);
             else
                 tv.setTextColor(Color.RED);

             tv.setPaintFlags(tv.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
             tv.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     Intent dataIntent =new Intent(thisActivity,SellProductActivity.class);
                     String strSell = new Gson().toJson(currentSell);
                     dataIntent.putExtra(getString(R.string.the_sell), strSell);
                     startActivity(dataIntent);
                 }
             });
             llAllCurrentResult.addView(tv);
         }
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



    public ArrayList<Sell> getAllDemandedSells() {
        return allDemandedSells;
    }

    public void setAllDemandedSells(ArrayList<Sell> allDemandedSells) {
        this.allDemandedSells = allDemandedSells;
    }

    public String getAllSellsId() {return allSellsId;}

    public void setAllSellsId(String allSellsId) {
        this.allSellsId = allSellsId;
    }

    public ProgressDialog getProgressDialog() {return progressDialog;}

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }
}
