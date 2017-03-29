package com.example.shlomi.publichand;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Display;
import android.view.Gravity;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;


import java.util.ArrayList;

import Logic.FirebaseDB_Controller;
import Logic.User;

public class RegisterActivity extends AppCompatActivity {

    User theUser;
    ProgressDialog progressDialog;
    FirebaseAuth fbAuth;
    FirebaseDB_Controller dbController;
    Activity thisActivity = this;
    TableRow tblRegisterFirstName, tblRegisterLastName, tblRegisterPhone, tblRegisterUserName, tblRegisterPassword,
            tblRegisterConfirmPassword, tblRegisterLocation;
    TextView messageLbl;
    private int width,height;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //  screen size parameters
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        //init global params
        fbAuth =FirebaseAuth.getInstance();
        dbController = new FirebaseDB_Controller();
        final int FONT_SIZE_LABEL =height/85;
        final int FONT_SIZE_TEXT =2*FONT_SIZE_LABEL/3;
        progressDialog = new ProgressDialog(this);
        Spinner locationSpn;


        //setting main layout
        LinearLayout llMain = new LinearLayout(this);
        llMain.setOrientation(LinearLayout.VERTICAL);
        Drawable image = ContextCompat.getDrawable(this,R.drawable.background2);
        llMain.setBackgroundDrawable(image);
        setContentView(llMain);


        //setting the sub views
        tblRegisterFirstName = fillTableRow(getString(R.string.first_name), false, FONT_SIZE_LABEL, Color.BLACK, Color.WHITE, Typeface.BOLD);
        tblRegisterLastName = fillTableRow(getString(R.string.last_name), false, FONT_SIZE_LABEL, Color.BLACK, Color.WHITE, Typeface.BOLD);
        tblRegisterPhone = fillTableRow(getString(R.string.phone), false, FONT_SIZE_LABEL, Color.BLACK, Color.WHITE, Typeface.BOLD);
        tblRegisterUserName = fillTableRow(getString(R.string.user_name), false, FONT_SIZE_LABEL, Color.BLACK, Color.WHITE, Typeface.BOLD);
        tblRegisterPassword = fillTableRow(getString(R.string.password), true, FONT_SIZE_LABEL, Color.BLACK, Color.WHITE, Typeface.BOLD);
        tblRegisterConfirmPassword = fillTableRow(getString(R.string.confirm_password), true, FONT_SIZE_LABEL, Color.BLACK, Color.WHITE, Typeface.BOLD);

        tblRegisterLocation =makeTableRow();
        TextView locationLbl = makeTextView(getString(R.string.location), FONT_SIZE_LABEL, Color.BLACK, Typeface.BOLD);


        //setting the spinners
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
        tblAllActions.addView(tblRegisterUserName);
        tblAllActions.addView(tblRegisterPassword);
        tblAllActions.addView(tblRegisterConfirmPassword);

        messageLbl= new TextView(this);
        messageLbl.setTextColor(Color.RED);

        //make new user (in case it valid ) and save it in firebase database
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
                    fbAuth.createUserWithEmailAndPassword(theUser.getUserName()
                            +getString(R.string.public_hand_com),((EditText)tblRegisterPassword.getChildAt(1)).getText()+"")
                            .addOnCompleteListener(thisActivity, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressDialog.dismiss();
                                    if(task.isSuccessful()){
                                        Intent intentData = new Intent();
                                        setResult(1,intentData);
                                        String usersNode =getString(R.string.users);
                                        dbController.storeUser(task.getResult().getUser().getUid(),theUser,usersNode);
                                        finish();
                                    }
                                    else if (task.getException() instanceof FirebaseAuthUserCollisionException)
                                        messageLbl.setText(getString(R.string.userName_already_exist));
                                    else
                                        messageLbl.setText(getString(R.string.login_faild));
                                }
                            });
                }
            }
        });


        //setting return to login activity
        Button returnBtn = new Button(this);
        returnBtn.setText(getString(R.string.returnn));
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(-1);
                finish();
            }
        });
        llOptions.addView(submitBtn);
        llOptions.addView(returnBtn);

        llMain.addView(messageLbl);
        llMain.addView(tblAllActions);
        llMain.addView(llOptions);
    }



    ////generic method for UI of tableRow
    public TableRow fillTableRow (String str, boolean isPassword,int fontSize, int colorTxt , int colorBackground, int typeFace){
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

    //check validation of user inputs
    public  int isValidRegister(){

        int valid = 0;

        String firstName = ((EditText) tblRegisterFirstName.getChildAt(1)).getText()+"";
        String lastName = ((EditText) tblRegisterLastName.getChildAt(1)).getText()+"";
        String phone = ((EditText) tblRegisterPhone.getChildAt(1)).getText()+"";
        String location = ((Spinner) tblRegisterLocation.getChildAt(1)).getSelectedItem().toString();
        String userName = ((EditText) tblRegisterUserName.getChildAt(1)).getText()+"";
        String password = ((EditText) tblRegisterPassword.getChildAt(1)).getText()+"";
        String confirmPassword = ((EditText) tblRegisterConfirmPassword.getChildAt(1)).getText()+"";


        if (firstName.equals(""))
            return valid=1;

        if (lastName.equals(""))
            return valid=2;

        if (!(phone.matches("[0-9]+") && phone.length() == 10))
            return valid=3;

        if (userName.equals(("")))
            return valid=4;

        if(password.equals("") || password.length()<6)
            return valid=5;

        if(confirmPassword.equals("") )
            return valid=6;

        if(!password.equals(confirmPassword))
            return valid=7;
        theUser = new User(firstName,lastName,phone,location,userName);

        return  valid;
    }



    //make fit error input validation
    public String makeErrorMessage(int valid){
        String message="";
        switch (valid){
            case  0:
                break;
            case 1:
                message= getString(R.string.please_enter_first_name);
                break;
            case 2:
                message= getString(R.string.please_enter_last_name);
                break;
            case 3:
                message= getString(R.string.please_enter_number_only_10_digits);
                break;
            case 4:
                message= getString(R.string.please_enter_user_name);
                break;
            case 5:
                message= getString(R.string.please_enter_password_at_least_6_digits);
                break;
            case 6:
                message= getString(R.string.please_enter_confirm_password);
                break;
            case 7:
                message= getString(R.string.please_match_between_password_and_confirm_password);
                break;
        }
        return  message;
    }

}


