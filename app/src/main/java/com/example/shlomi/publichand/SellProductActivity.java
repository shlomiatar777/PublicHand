package com.example.shlomi.publichand;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;

import Logic.Sell;
import Logic.User;

public class SellProductActivity extends AppCompatActivity {
    FirebaseAuth fbAuth;
    Sell theSell;
    Activity thisActivity = this;
    int width,height;
    int currentPrice;
    final long TWO_DAYS = 2*24*60*60*1000;
    DatabaseReference dbref;
    User theUser;
    TextView makeOfferLbl;
    String allOffersStr;

    TableRow tblSellerDetails1;
    LinearLayout llSellerDetails2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_product);


        //  screen size parameters
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;


        //init global params
        dbref= FirebaseDatabase.getInstance().getReference();
        fbAuth =FirebaseAuth.getInstance();

        String fromStrToJason= getIntent().getStringExtra(getString(R.string.the_sell));
        theSell = new Gson().fromJson(fromStrToJason,Sell.class);

        final int FONT_SIZE_LABEL =height/90;


        //setting main layout
        LinearLayout llMain= (LinearLayout)findViewById(R.id.the_layout);
        llMain.setOrientation(LinearLayout.VERTICAL);
        llMain.setGravity(Gravity.CENTER);
        Drawable image = ContextCompat.getDrawable(this,R.drawable.background2);
        llMain.setBackgroundDrawable(image);



        //setting the title
        TextView title = makeLbl(theSell.getTheProduct().getTitle(), 5*FONT_SIZE_LABEL/2, Color.BLACK, Typeface.BOLD);
        title.setPadding(width/4 ,0 ,0,width/10);

        //the image saved in the firebas databas as String , we creat a BitMap  from the String
        String imageStr = theSell.getTheProduct().getImage();
        ImageView productImage = new ImageView(this);
        if (!imageStr.equals("")) {
            byte[] byteArr = Base64.decode(imageStr, Base64.DEFAULT);
            Bitmap b = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length);
            Bitmap bResize = Bitmap.createScaledBitmap(b, width / 2, height / 4, false);
            productImage.setImageBitmap(bResize);
        }
        currentPrice=theSell.getTheProduct().getCurrentPrice();


        //setting the sub views
        LinearLayout llDescription = makeLinearLayout(getString(R.string.description), theSell.getTheProduct().getDescription()
                ,FONT_SIZE_LABEL, Color.BLACK,Typeface.BOLD);

        TableLayout tblAllActions = new TableLayout(this);
        tblAllActions.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout llCategory = makeLinearLayout(getString(R.string.category)+ theSell.getTheProduct().getCategory()
                ,getString(R.string.sub_category)+ theSell.getTheProduct().getSubCategory(), FONT_SIZE_LABEL, Color.BLACK, Typeface.BOLD);

        tblSellerDetails1 = fillTableRow(getString(R.string.price)+ theSell.getTheProduct().getCurrentPrice()+"  ", getString(R.string.location) + ""
                ,FONT_SIZE_LABEL, Color.BLACK, Typeface.BOLD);


        //when the Sell is still active, it show the current automatic addition  to the offer
        // 0 > product price > 100 ----> addition  to the offer = 20;
        // 101 > product price > 1000 ----> addition  to the offer = 50;
        // 1001 > product price > 10000 ----> addition  to the offer = 500;
        //10001 > product price  ----> addition  to the offer = 5000;
        makeOfferLbl = makeLbl(getString(R.string.every_offer_raise)+checkNewPrice(), FONT_SIZE_LABEL, Color.BLACK, Typeface.BOLD);


      //check if pass 2 days (48 hours) from create this product , and show how much time left to the sell
        Date finishDate = new Date (theSell.getCreateDate().getTime()+TWO_DAYS);
        Date currentDate = new Date ();
        long dif = finishDate.getTime() - currentDate.getTime();
       TableRow tblDate;
        if(dif>0){
            long hours =  dif /(1000*60*60);
            long minutes = dif /(1000*60);
            tblDate = fillTableRow(getString(R.string.time_left),getString(R.string.hours)+ hours+getString(R.string.minutes)+ minutes % 60
                    ,FONT_SIZE_LABEL, Color.BLACK, Typeface.BOLD);
        }
        else{
            tblDate= fillTableRow(getString(R.string.the_sell_is_over),"",FONT_SIZE_LABEL, Color.BLACK, Typeface.BOLD);
        }


        //setting the sub views , get this Sell details from firebas database
        llSellerDetails2 = makeLinearLayout(getString(R.string.seller_name), getString(R.string.phone)
                ,FONT_SIZE_LABEL, Color.BLACK, Typeface.BOLD);

        dbref.child(getString(R.string.users)).child(theSell.getTheProduct().getTheSeller()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<String> userParams= new ArrayList<>();
                for (DataSnapshot s : dataSnapshot.getChildren()){
                    userParams.add(s.getValue(String.class));
                }
                theUser= new User(userParams.get(0), userParams.get(1), userParams.get(2), userParams.get(3),
                        userParams.get(4), userParams.get(5) );
                if (theUser!=null){
                    ((TextView) llSellerDetails2.getChildAt(0)).setText(getString(R.string.seller_name)+theUser.getFirstName());
                    ((TextView) llSellerDetails2.getChildAt(1)).setText(getString(R.string.phone)+theUser.getPhone());
                    ((TextView) tblSellerDetails1.getChildAt(1)).setText(getString(R.string.location)+theUser.getLocation());
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //check if pass 2 days (48 hours) from create this product , and raise the current price
        // by fit automatic addition(20, 50, 500, 5000)
        Button makeOfferBtn = new Button(this);
        makeOfferBtn.setText("Offer");
        makeOfferBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date finishDate = new Date (theSell.getCreateDate().getTime()+TWO_DAYS);
                Date currentDate = new Date ();
                long dif = finishDate.getTime() - currentDate.getTime();
                if(dif>0) {
                    currentPrice += checkNewPrice();
                    String str = "|" + fbAuth.getCurrentUser().getUid() + "," + currentPrice;
                    str += allOffersStr;
                    dbref.child(theSell.getTheProduct().getCategory()).child(theSell.getTheProduct().getSubCategory())
                            .child(theSell.getId()).child(getString(R.string.all_offers)).setValue(str);
                    dbref.child(theSell.getTheProduct().getCategory()).child(theSell.getTheProduct().getSubCategory())
                            .child(theSell.getId()).child(getString(R.string.the_product)).child(getString(R.string.current_price)).setValue(currentPrice);
                }
            }
        });


        //save the new current highes offer in the firebas database
        dbref.child(theSell.getTheProduct().getCategory()).child(theSell.getTheProduct().getSubCategory()).child(theSell.getId())
                .addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot) {
                   currentPrice =  dataSnapshot.child(getString(R.string.the_product)).child(getString(R.string.current_price)).getValue(Integer.class);
                   ((TextView) tblSellerDetails1.getChildAt(0)).setText(getString(R.string.price) +currentPrice+"   ");
                   makeOfferLbl.setText(getString(R.string.every_offer_raise)+checkNewPrice());
                   allOffersStr=   dataSnapshot.child(getString(R.string.all_offers)).getValue(String.class);

               }

               @Override
               public void onCancelled(DatabaseError databaseError) {

               }
           });



        llMain.addView(title);
        llMain.addView(productImage);
        llMain.addView(llDescription);
        llMain.addView(llCategory);
        tblAllActions.addView(tblDate);
        tblAllActions.addView(tblSellerDetails1);
        llMain.addView(llSellerDetails2);
        llMain.addView(tblAllActions);
        if(dif>0)
             llMain.addView(makeOfferBtn);

        makeOfferLbl.setPadding(0,width/10,0,0);
        llMain.addView(makeOfferLbl);
    }



    // //generic method for UI of tableRow
    public TableRow fillTableRow(String strLbl1 , String strLbl2, int fontSize , int color , int typeFace ){
        TableRow tableRow = new TableRow(this);
        tableRow.setPadding(0,width/10,0,0);
        tableRow.setOrientation(LinearLayout.HORIZONTAL);
        tableRow.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView lbl1 = makeLbl(strLbl1, fontSize, color, typeFace);
        TextView lbl2 = makeLbl(strLbl2, fontSize, color, typeFace);

        tableRow.addView(lbl1);
        tableRow.addView(lbl2);
        return tableRow;
    }


    //generic method for make TextView
    public TextView makeLbl(String str , int size , int color , int typeFace){
        TextView theLbl = new TextView(this);
        theLbl.setText(str);
        theLbl.setTextSize(size);
        theLbl.setTextColor(color);
        theLbl.setTypeface(null, typeFace);
        return theLbl;
    }



    //when the Sell is still active, it show the current automatic addition  to the offer
    // 0 > product price > 100 ----> addition  to the offer = 20;
    // 101 > product price > 1000 ----> addition  to the offer = 50;
    // 1001 > product price > 10000 ----> addition  to the offer = 500;
    //10001 > product price  ----> addition  to the offer = 5000;
    public int checkNewPrice(){
        int difference=0;
        if (currentPrice<100)
            return 20;
         if (currentPrice<1000)
             return 50;
        if (currentPrice<10000)
            return 500;
        return 5000;
    }


     //generic method for UI of LinearLayout
    public LinearLayout makeLinearLayout (String strLbl1 , String strLbl2,  int fontSize , int color , int typeFace){
        LinearLayout ll = new LinearLayout(this);
        ll.setPadding(0,width/10,0,0);
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView lbl1 = makeLbl(strLbl1, fontSize, color, typeFace);
        TextView lbl2 = makeLbl(strLbl2, fontSize, color, typeFace);

        ll.addView(lbl1);
        ll.addView(lbl2);

        return ll;
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
