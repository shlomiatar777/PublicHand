package Logic;

import android.app.Activity;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.shlomi.publichand.R;
import com.example.shlomi.publichand.ResultSearchActivity;
import com.example.shlomi.publichand.SellProductActivity;
import com.example.shlomi.publichand.SettingUserDetailsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Shlomi on 22/03/2017.
 */

public class FirebaseDB_Controller {

    final long TWO_DAYS = 2*24*60*60*1000;
    final int NOT_EXIST =-1;
    DatabaseReference dbref;
    FirebaseAuth fbAuth;
    Date currentDate;

  public  FirebaseDB_Controller(){
      dbref= FirebaseDatabase.getInstance().getReference();
      fbAuth =FirebaseAuth.getInstance();
      currentDate = new Date();
  }

    //  search products
    public void searchProduct(final ResultSearchActivity activity, String category, String subcategory, final int minPrice, final int maxPrice ){
        dbref.child(category).child(subcategory).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                activity.getAllDemandedSells().clear();
                activity.setAllSellsId("");
                boolean isDemandedSell;
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    isDemandedSell = false;
                    long time = s.child(activity.getString(R.string.create_date)).child(activity.getString(R.string.time)).getValue(Long.class);
                    Date currentDate = new Date();
                    int diffTime = (int)TWO_DAYS;
                    if (currentDate.getTime() - time <= diffTime && currentDate.getTime() - time > 0) {
                        int currentPrice = s.child(activity.getString(R.string.the_product)).child(activity.getString(R.string.current_price)).getValue(Integer.class);
                        if (minPrice == NOT_EXIST && maxPrice == NOT_EXIST)
                            isDemandedSell = true;
                        else if ((minPrice != NOT_EXIST && minPrice <= currentPrice) && maxPrice == NOT_EXIST)
                            isDemandedSell = true;
                        else if (minPrice == NOT_EXIST && (maxPrice >= currentPrice && maxPrice != NOT_EXIST))
                            isDemandedSell = true;
                        else if ((minPrice != NOT_EXIST && minPrice <= currentPrice) && (maxPrice >= currentPrice && maxPrice != NOT_EXIST))
                            isDemandedSell = true;

                        if (isDemandedSell) {

                            Sell theSell = makeSell(s, activity);

                            activity.getAllDemandedSells().add(theSell);
                            activity.setAllSellsId(activity.getAllSellsId() + theSell.getId());

                        }
                    }
                }
                //sort by date
                Collections.sort(activity.getAllDemandedSells(), new Comparator<Sell>() {
                    @Override
                    public int compare(Sell o1, Sell o2) {
                        return (int) (o1.getCreateDate().getTime() - o2.getCreateDate().getTime());
                    }
                });


                activity.showCurrentPage();
                activity.getProgressDialog().dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    // search my offers on products
    public void showMyOffers(final ResultSearchActivity activity){
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                activity.getAllDemandedSells().clear();
                activity.setAllSellsId("");
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    if (!s.getKey().equals(activity.getString(R.string.users))) {
                        for (DataSnapshot subCategory : s.getChildren()) {
                            for (DataSnapshot product : subCategory.getChildren()) {
                                String allOffers = product.child(activity.getString(R.string.all_offers)).getValue(String.class);
                                String Uid = fbAuth.getCurrentUser().getUid();
                                int match = allOffers.indexOf(Uid);
                                if (match != NOT_EXIST) {
                                    Sell theSell =  makeSell(product, activity);
                                    activity.getAllDemandedSells().add(theSell);

                                    if (match == 1) {
                                        activity.setAllSellsId(activity.getAllSellsId() + theSell.getId());

                                    }
                                }
                            }
                        }
                    }
                }

                //sort by date, if sell is over it move to end of the list, also keep the highest offers in different list
                // it suppose to color in blue product's titles in the result page when the connected user offer the
                //current highest offer, else it color in red
                Collections.sort(activity.getAllDemandedSells(), new Comparator<Sell>() {
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

                activity.showCurrentPage();
                activity.getProgressDialog().dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }






    // search my products which i sell
    public void showMySells(final ResultSearchActivity activity){
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                activity.getAllDemandedSells().clear();
                activity.setAllSellsId("");
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    if (!s.getKey().equals(activity.getString(R.string.users))) {
                        for (DataSnapshot subCategory : s.getChildren()) {
                            for (DataSnapshot product : subCategory.getChildren()) {
                                String sellerId = product.child(activity.getString(R.string.the_product)).child(activity.getString(R.string.the_seller)).getValue(String.class);
                                String currentUserId  = fbAuth.getCurrentUser().getUid();
                                if(sellerId.equals(currentUserId)){
                                    Sell theSell = makeSell(product,activity);
                                    activity.getAllDemandedSells().add(theSell);
                                    activity.setAllSellsId(activity.getAllSellsId() + theSell.getId());
                                }
                            }
                        }
                    }
                }
                //sort by date, if sell is over it move to end of the list
                Collections.sort(activity.getAllDemandedSells(), new Comparator<Sell>() {
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

                activity.showCurrentPage();
                activity.getProgressDialog().dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }





    //create Sell from firebase database by query
    public Sell makeSell(DataSnapshot product,final ResultSearchActivity activity){

        String description = product.child(activity.getString(R.string.the_product)).child(activity.getString(R.string.descriptionDB)).getValue(String.class);
        String imageStr = product.child(activity.getString(R.string.the_product)).child(activity.getString(R.string.image)).getValue(String.class);
        String sellerId = product.child(activity.getString(R.string.the_product)).child(activity.getString(R.string.the_seller)).getValue(String.class);
        int currentPrice = product.child(activity.getString(R.string.the_product)).child(activity.getString(R.string.current_price)).getValue(Integer.class);
        String title = product.child(activity.getString(R.string.the_product)).child(activity.getString(R.string.titleDB)).getValue(String.class);
        String category = product.child(activity.getString(R.string.the_product)).child(activity.getString(R.string.categoryDB)).getValue(String.class);
        String subCategory = product.child(activity.getString(R.string.the_product)).child(activity.getString(R.string.sub_categoryDB)).getValue(String.class);

        Product p = new Product(sellerId, category, subCategory, description, imageStr, currentPrice, title);

        long time = product.child(activity.getString(R.string.create_date)).child(activity.getString(R.string.time)).getValue(Long.class);
        String sellId = product.child(activity.getString(R.string.id)).getValue(String.class);
        String allOffers = product.child(activity.getString(R.string.all_offers)).getValue(String.class);
        Sell theSell = new Sell(p, new Date(time), allOffers, sellId);
        return theSell;
    }


    public String makeKey(Spinner categorySpn, Spinner subCategorySpn){
       return dbref.child(categorySpn.getSelectedItem().toString())
                .child(subCategorySpn.getSelectedItem().toString()).push().getKey();

    }

    public void saveSell(Product theProduct, Sell theSell,String key){
        dbref.child(theProduct.getCategory()).child(theProduct.getSubCategory()).child(key).setValue(theSell);
    }



    //from RegisterActivity
    public void storeUser(String Uid , User theUser,String usersNode){
        dbref.child(usersNode).child(Uid).setValue(theUser);

    }


    //from SellProductActivity
    public void retrieveUsersDetails(final SellProductActivity activity){

        dbref.child(activity.getString(R.string.users)).child(activity.getTheSell().getTheProduct().getTheSeller()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> userParams= new ArrayList<>();
                for (DataSnapshot s : dataSnapshot.getChildren()){
                    userParams.add(s.getValue(String.class));
                }
                User theUser= new User(userParams.get(0), userParams.get(1), userParams.get(3), userParams.get(2),
                        userParams.get(4) );
                activity.setUser(theUser);
                if (theUser!=null){
                    ((TextView) activity.getLinearLayout().getChildAt(0)).setText(activity.getString(R.string.seller_name)+theUser.getFirstName());
                    ((TextView) activity.getLinearLayout().getChildAt(1)).setText(activity.getString(R.string.phone)+theUser.getPhone());
                    ((TextView) activity.getTableRow().getChildAt(1)).setText(activity.getString(R.string.location)+theUser.getLocation());
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    public void updateAllOffersArray(SellProductActivity activity, String allOffers){
        dbref.child(activity.getTheSell().getTheProduct().getCategory()).child(activity.getTheSell().getTheProduct().getSubCategory())
                .child(activity.getTheSell().getId()).child(activity.getString(R.string.all_offers)).setValue(allOffers);
    }



    public void updateCurrentPrice(SellProductActivity activity , int currentPrice){
        dbref.child(activity.getTheSell().getTheProduct().getCategory()).child(activity.getTheSell().getTheProduct().getSubCategory())
                .child(activity.getTheSell().getId()).child(activity.getString(R.string.the_product)).child(activity.getString(R.string.current_price)).setValue(currentPrice);
    }



    //save the new current highes offer in the firebas database
    public void updateUsersDetails(final SellProductActivity activity){

        dbref.child(activity.getTheSell().getTheProduct().getCategory()).child(activity.getTheSell().getTheProduct().getSubCategory()).child(activity.getTheSell().getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        activity.setCurrentPrice(dataSnapshot.child(activity.getString(R.string.the_product)).child(activity.getString(R.string.current_price)).getValue(Integer.class));
                        ((TextView) activity.getTableRowSellerDetails().getChildAt(0)).setText(activity.getString(R.string.price) +activity.getCurrentPrice()+"   ");
                        activity.getTextView().setText(activity.getString(R.string.every_offer_raise)+activity.checkNewPrice());
                        activity.setAllOffersStr(dataSnapshot.child(activity.getString(R.string.all_offers)).getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }



    public  void updateFirstNameUser(SettingUserDetailsActivity activity, String firstName){
        dbref.child(activity.getString(R.string.users)).child(activity.getUserId()).child(activity.getString(R.string.first_nameDB)).setValue(firstName);
    }

    public  void updateLastNameUser(SettingUserDetailsActivity activity, String lastName){
        dbref.child(activity.getString(R.string.users)).child(activity.getUserId()).child(activity.getString(R.string.last_nameDB)).setValue(lastName);
    }

    public  void updateLocation(SettingUserDetailsActivity activity, String location){
        dbref.child(activity.getString(R.string.users)).child(activity.getUserId()).child(activity.getString(R.string.locationDB)).setValue(location);
    }
    public  void updatePhone(SettingUserDetailsActivity activity, String phone){
        dbref.child(activity.getString(R.string.users)).child(activity.getUserId()).child(activity.getString(R.string.phoneDB)).setValue(phone);
    }




}
