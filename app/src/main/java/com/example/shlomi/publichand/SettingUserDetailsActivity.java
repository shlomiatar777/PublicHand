package com.example.shlomi.publichand;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SettingUserDetailsActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    FirebaseAuth fbAuth;
    DatabaseReference dbref;
    TableRow tblRegisterFirstName, tblRegisterLastName, tblRegisterPhone, tblRegisterLocation;
    TextView messageLbl;

    String userId ,firstName, lastName, phone, location;
    private int width,height;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_user_details);

        //  screen size parameters
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;



        //init global params
        final int FONT_SIZE_LABEL =height/85;
        progressDialog = new ProgressDialog(this);
        Spinner locationSpn;
        fbAuth = FirebaseAuth.getInstance();
        dbref= FirebaseDatabase.getInstance().getReference();


        //setting main layout
        LinearLayout llMain = new LinearLayout(this);
        Drawable image = ContextCompat.getDrawable(this,R.drawable.background2);
        llMain.setBackgroundDrawable(image);
        llMain.setOrientation(LinearLayout.VERTICAL);
        setContentView(llMain);


        //setting the sub views
        tblRegisterFirstName = fillTableRow(getString(R.string.first_name), false, FONT_SIZE_LABEL, Color.BLACK, Color.WHITE, Typeface.BOLD);
        tblRegisterLastName = fillTableRow(getString(R.string.last_name), false, FONT_SIZE_LABEL, Color.BLACK, Color.WHITE, Typeface.BOLD);
        tblRegisterPhone = fillTableRow(getString(R.string.phone), false, FONT_SIZE_LABEL, Color.BLACK, Color.WHITE, Typeface.BOLD);
        tblRegisterLocation =makeTableRow();

        //setting the location spinners
        TextView locationLbl = makeTextView(getString(R.string.location), FONT_SIZE_LABEL, Color.BLACK, Typeface.BOLD);
        locationLbl.setText(getString(R.string.location));

        locationSpn = new Spinner(this);
        ArrayList<String> allAreas = new ArrayList<>();
        allAreas.add(getString(R.string.jerusalem));
        allAreas.add(getString(R.string.haifa));
        allAreas.add(getString(R.string.tel_aviv));
        allAreas.add(getString(R.string.the_sharon));
        allAreas.add(getString(R.string.the_shfela));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this , android.R.layout.simple_spinner_item,allAreas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpn.setAdapter(adapter);

        tblRegisterLocation.addView(locationLbl);
        tblRegisterLocation.addView(locationSpn);


        TableLayout tblAllActions = new TableLayout(this);
        tblAllActions.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        tblAllActions.addView(tblRegisterFirstName);
        tblAllActions.addView(tblRegisterLastName);
        tblAllActions.addView(tblRegisterLocation);
        tblAllActions.addView(tblRegisterPhone);


        messageLbl= new TextView(this);



        // get user's input and edit the user details in firebase database
        LinearLayout llOptions = new LinearLayout(this);
        llOptions.setOrientation(LinearLayout.HORIZONTAL);
        llOptions.setGravity(Gravity.CENTER_HORIZONTAL);
        Button submitBtn = new Button(this);
        submitBtn.setText(getString(R.string.submit));
        submitBtn.setOnClickListener(new View.OnClickListener() {
            int valid;
            String message;
            @Override
            public void onClick(View v) {
                valid = isValidRegister();
                message= makeErrorMessage(valid);
                messageLbl.setText(message);
                if(message.equals("")){
                    progressDialog.setMessage(getString(R.string.please_wait));
                    progressDialog.show();
                    setDetails();
                    progressDialog.dismiss();

                }
            }
        });

        //setting return to the last activity before this activity
        Button returnBtn = new Button(this);
        returnBtn.setText(getString(R.string.returnn));
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        llOptions.addView(submitBtn);
        llOptions.addView(returnBtn);

        llMain.addView(messageLbl);
        llMain.addView(tblAllActions);

        llMain.addView(llOptions);
    }


    //generic method for UI of tableRow
    public TableRow fillTableRow (String str, boolean isPassword, int fontSize, int colorTxt , int colorBackground, int typeFace){
        TableRow tableRow = makeTableRow();
        tableRow.setPadding(0,30,0,0);

        TextView tv = makeTextView(str,fontSize, colorTxt, typeFace);

        EditText et = makeTxt(colorBackground,"",2*fontSize/3, width/30);

        if(isPassword){
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            et.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }

        tableRow.addView(tv);
        tableRow.addView(et);
        return tableRow;
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



    // get user's input and edit the user details in firebase database
    public void setDetails(){

        if (!firstName.equals(""))
         dbref.child(getString(R.string.users)).child(userId).child(getString(R.string.first_nameDB)).setValue(firstName);

        if (!lastName.equals(""))
            dbref.child(getString(R.string.users)).child(userId).child(getString(R.string.last_nameDB)).setValue(lastName);

        dbref.child(getString(R.string.users)).child(userId).child(getString(R.string.locationDB)).setValue(location);

        if (!phone.equals("")) {
                dbref.child(getString(R.string.users)).child(userId).child(getString(R.string.phoneDB)).setValue(phone);
        }
    }



    // check  user's input are valid, can be also empty
    public  int isValidRegister(){

        int valid = 0;
        userId = fbAuth.getCurrentUser().getUid();
        firstName = ((EditText) tblRegisterFirstName.getChildAt(1)).getText()+"";
        lastName = ((EditText) tblRegisterLastName.getChildAt(1)).getText()+"";
        phone = ((EditText) tblRegisterPhone.getChildAt(1)).getText()+"";
        location = ((Spinner) tblRegisterLocation.getChildAt(1)).getSelectedItem().toString();

        if (!firstName.equals(""))
            dbref.child(getString(R.string.users)).child(userId).child(getString(R.string.first_nameDB)).setValue(firstName);

        if (!lastName.equals(""))
            dbref.child(getString(R.string.users)).child(userId).child(getString(R.string.last_nameDB)).setValue(lastName);

        dbref.child(getString(R.string.users)).child(userId).child(getString(R.string.locationDB)).setValue(location);

        if (!phone.equals("")) {
            if (!(phone.matches("[0-9]+") && phone.length() == 10))
                return valid = 1;
            else
                dbref.child(getString(R.string.users)).child(userId).child(getString(R.string.phoneDB)).setValue(phone);
        }

        return  valid;
    }



    //make fit error input validation
    public String makeErrorMessage(int valid){
        String message="";
        switch (valid){
            case  0:
                break;
            case 1:
                message= getString(R.string.please_enter_number_only_10_digits);
                break;
        }
        return  message;
    }



    //create options in tool bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }



    //check if we got a picture from the gallery
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click

        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.edit){
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
