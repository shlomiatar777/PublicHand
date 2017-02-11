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

import Logic.Product;
import Logic.Sell;

public class ResultSearchActivity extends AppCompatActivity {
    FirebaseAuth fbAuth;
    DatabaseReference dbref;
    ProgressDialog progressDialog ;
    ArrayList<Sell> allDemandedSells;
    LinearLayout llAllCurrentResult;
    TextView currentPageLbl;
    int currentPage;
    final long TWO_DAYS = 2*24*60*60*1000;
    final int PRODUCTS_PER_PAGE =10;
    final int NOT_EXIST =-1;

    private int width,height;
    String allSellsId;
    Date currentDate;
    Activity thisActivity = this;


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

        dbref= FirebaseDatabase.getInstance().getReference();
        fbAuth =FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
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


        LinearLayout llPage = new LinearLayout(this);
        llPage.setOrientation(LinearLayout.HORIZONTAL);
        llPage.setGravity(Gravity.CENTER);

        currentPageLbl = new TextView(this);
        currentPageLbl.setText(currentPage+"");
        currentPageLbl.setTextSize(FONT_SIZE_LABEL);

        //set buttons forward and preview
        Button previous = new Button(this);
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


        Button forward = new Button(this);
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

        llPage.addView(previous);
        llPage.addView(currentPageLbl);
        llPage.addView(forward);

        // get number from other activities , 1 means search products
        //                                    2 means search my offers on products
        //                                    3 means search my products which i sell
        final int searchType =getIntent().getIntExtra(getString(R.string.type_result),-1);

                switch (searchType) {
                    case 1:
                        searchProduct();
                        break;

                    case 2:
                        showMyOffers();
                        break;

                    case 3:
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
        dbref.child(category).child(subcategory).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allDemandedSells.clear();
                allSellsId="";
                boolean isDemandedSell;
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    isDemandedSell = false;
                    long time = s.child(getString(R.string.create_date)).child(getString(R.string.time)).getValue(Long.class);
                    Date currentDate = new Date();
                    int diffTime = (int)TWO_DAYS;
                    if (currentDate.getTime() - time <= diffTime && currentDate.getTime() - time > 0) {
                        int currentPrice = s.child(getString(R.string.the_product)).child(getString(R.string.current_price)).getValue(Integer.class);
                        if (minPrice == NOT_EXIST && maxPrice == NOT_EXIST)
                            isDemandedSell = true;
                        else if ((minPrice != NOT_EXIST && minPrice <= currentPrice) && maxPrice == NOT_EXIST)
                            isDemandedSell = true;
                        else if (minPrice == NOT_EXIST && (maxPrice >= currentPrice && maxPrice != NOT_EXIST))
                            isDemandedSell = true;
                        else if ((minPrice != NOT_EXIST && minPrice <= currentPrice) && (maxPrice >= currentPrice && maxPrice != NOT_EXIST))
                            isDemandedSell = true;

                        if (isDemandedSell) {

                            Sell theSell = makeSell(s);

                            allDemandedSells.add(theSell);
                            allSellsId+=theSell.getId();

                        }
                    }
                }
                //sort by date
                Collections.sort(allDemandedSells, new Comparator<Sell>() {
                    @Override
                    public int compare(Sell o1, Sell o2) {
                        return (int) (o1.getCreateDate().getTime() - o2.getCreateDate().getTime());
                    }
                });


                showCurrentPage();
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    // search my offers on products
    public void showMyOffers(){
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allSellsId ="";
                allDemandedSells.clear();
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    if (!s.getKey().equals(getString(R.string.users))) {
                        for (DataSnapshot subCategory : s.getChildren()) {
                            for (DataSnapshot product : subCategory.getChildren()) {
                                String allOffers = product.child(getString(R.string.all_offers)).getValue(String.class);
                                String Uid = fbAuth.getCurrentUser().getUid();
                                int match = allOffers.indexOf(Uid);
                                if (match != NOT_EXIST) {
                                    Sell theSell =  makeSell(product);
                                    allDemandedSells.add(theSell);

                                    if (match == 1) {
                                        allSellsId+=theSell.getId();
                                    }
                                }
                            }
                        }
                    }
                }

                //sort by date, if sell is over it move to end of the list, also keep the highest offers in different list
                // it suppose to color in blue product's titles in the result page when the connected user offer the
                //current highest offer, else it color in red
                Collections.sort(allDemandedSells, new Comparator<Sell>() {
                    @Override
                    public int compare(Sell o1, Sell o2) {
                        long dif1 = o1.getCreateDate().getTime() - currentDate.getTime() + TWO_DAYS;
                        long dif2 = o2.getCreateDate().getTime() - currentDate.getTime()+ TWO_DAYS;
                        if (dif1>0 && dif2<0 )
                            return -1;
                        else if(dif1<0 && dif2>0)
                            return 1;
                        return (int) (o1.getCreateDate().getTime() - o2.getCreateDate().getTime());
                    }
                });

                showCurrentPage();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    // search my products which i sell
    public void showMySells(){
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allSellsId ="";
                allDemandedSells.clear();
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    if (!s.getKey().equals(getString(R.string.users))) {
                        for (DataSnapshot subCategory : s.getChildren()) {
                            for (DataSnapshot product : subCategory.getChildren()) {
                                String sellerId = product.child(getString(R.string.the_product)).child(getString(R.string.the_seller)).getValue(String.class);
                                String currentUserId  = fbAuth.getCurrentUser().getUid();
                                if(sellerId.equals(currentUserId)){
                                    Sell theSell = makeSell(product);
                                    allDemandedSells.add(theSell);
                                    allSellsId+=theSell.getId();
                                }
                            }
                        }
                    }
                }
                //sort by date, if sell is over it move to end of the list
                Collections.sort(allDemandedSells, new Comparator<Sell>() {
                    @Override
                    public int compare(Sell o1, Sell o2) {
                        long dif1 = o1.getCreateDate().getTime() - currentDate.getTime() +TWO_DAYS;
                        long dif2 = o2.getCreateDate().getTime() - currentDate.getTime()+ TWO_DAYS;
                        if (dif1>0 && dif2<0 )
                            return -1;
                        else if(dif1<0 && dif2>0)
                            return 1;
                        return (int) (o1.getCreateDate().getTime() - o2.getCreateDate().getTime());
                    }
                });

                showCurrentPage();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    //create Sell from firebase database by query
    public Sell makeSell(DataSnapshot product){

        String description = product.child(getString(R.string.the_product)).child(getString(R.string.descriptionDB)).getValue(String.class);
        String imageStr = product.child(getString(R.string.the_product)).child(getString(R.string.image)).getValue(String.class);
        String sellerId = product.child(getString(R.string.the_product)).child(getString(R.string.the_seller)).getValue(String.class);
        int currentPrice = product.child(getString(R.string.the_product)).child(getString(R.string.current_price)).getValue(Integer.class);
        String title = product.child(getString(R.string.the_product)).child(getString(R.string.titleDB)).getValue(String.class);
        String category = product.child(getString(R.string.the_product)).child(getString(R.string.categoryDB)).getValue(String.class);
        String subCategory = product.child(getString(R.string.the_product)).child(getString(R.string.sub_categoryDB)).getValue(String.class);

        Product p = new Product(sellerId, category, subCategory, description, imageStr, currentPrice, title);

        long time = product.child(getString(R.string.create_date)).child(getString(R.string.time)).getValue(Long.class);
        String sellId = product.child(getString(R.string.id)).getValue(String.class);
        String allOffers = product.child(getString(R.string.all_offers)).getValue(String.class);
        Sell theSell = new Sell(p, new Date(time), allOffers, sellId);
        return theSell;
    }



    //UI of current page
     public void showCurrentPage(){
         llAllCurrentResult.removeAllViews();
         int totalDemandedSells =  allDemandedSells.size();
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

}
