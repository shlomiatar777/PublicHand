package com.example.shlomi.publichand;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    ProgressDialog progressDialog;
    FirebaseAuth fbAuth;
    TextView statusRegisterMsg;
    EditText userNameTxt;
    EditText passwordTxT;
    Activity thisActivity = this;
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
        final int FONT_SIZE_LABEL =height/70;
        final int FONT_SIZE_TEXT =2*FONT_SIZE_LABEL/3;

        progressDialog = new ProgressDialog(this);
        fbAuth =FirebaseAuth.getInstance();
        final Intent intent = new Intent(this,RegisterActivity.class);

        //check if the is already connect user
        if(fbAuth.getCurrentUser()!=null){
            Intent intentData = new Intent(thisActivity,HomePageActivity.class);
            finish();
            startActivity(intentData);
        }

        //setting main layout
        LinearLayout llMain = new LinearLayout(this);
        llMain.setOrientation(LinearLayout.VERTICAL);
        Drawable image = ContextCompat.getDrawable(this,R.drawable.background2);
        llMain.setBackgroundDrawable(image);
        setContentView(llMain);

        //show error if was invalid login || show if just made Successful register (in the register activity)
        statusRegisterMsg = makeLbl("",FONT_SIZE_LABEL/2 ,Color.RED, Typeface.NORMAL );
        llMain.addView(statusRegisterMsg);

        //UI of user name
        LinearLayout llUser = new LinearLayout(this);
        llUser.setOrientation(LinearLayout.HORIZONTAL);

        userNameTxt = makeTxt(Color.WHITE,getString(R.string.write_name),FONT_SIZE_TEXT, 4*width/5);
        TextView userNameLbl = makeLbl(getString(R.string.user_name), FONT_SIZE_LABEL,Color.BLACK,Typeface.BOLD);
        llUser.addView(userNameLbl);
        llUser.addView(userNameTxt);
        LinearLayout llPassword = new LinearLayout(this);
        llPassword.setOrientation(LinearLayout.HORIZONTAL);

        //UI of password
        passwordTxT = makeTxt(Color.WHITE,getString(R.string.write_password),FONT_SIZE_TEXT, 4*width/5);
        passwordTxT.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordTxT.setTransformationMethod(PasswordTransformationMethod.getInstance());
        TextView passwordLbl = makeLbl(getString(R.string.password) , FONT_SIZE_LABEL ,Color.BLACK,Typeface.BOLD );
        llPassword.addView(passwordLbl);
        llPassword.addView(passwordTxT);

        //UI of login
        LinearLayout llLogin = new LinearLayout(this);
        llLogin.setOrientation(LinearLayout.HORIZONTAL);
        llLogin.setGravity(Gravity.CENTER_HORIZONTAL);

        //logic of login option
        Button loginBtn = new Button(this);
        loginBtn.setTextSize(FONT_SIZE_TEXT);
        loginBtn.setText(getString(R.string.login));
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
          login();
            }
        });

        //UI and logic of register option
        LinearLayout llRegister= new LinearLayout(this);
        llRegister.setOrientation(LinearLayout.HORIZONTAL);

        TextView registerTxt = makeLbl(getString(R.string.is_user_name_already),FONT_SIZE_TEXT ,Color.BLACK,Typeface.NORMAL);
        TextView registerRef = makeLbl(getString(R.string.register_here), FONT_SIZE_TEXT, Color.BLUE, Typeface.NORMAL  );
        registerRef.setPaintFlags(registerRef.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        registerRef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(intent,1);
            }
        });

        //add all params to sub layout and then to main layout
        llMain.addView(llUser);

        llPassword.setPadding(0,height/20,0,0);
        llMain.addView(llPassword);

        llLogin.setPadding(0,height/3,0,0);
        llLogin.addView(loginBtn);
        llMain.addView(llLogin);

        llRegister.addView(registerTxt);
        llRegister.addView(registerRef);
        llMain.addView(llRegister);

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

    //check validation of login details , move us to home page in case of valid details
    public void login(){
        String userName = userNameTxt.getText()+getString(R.string.public_hand_com);
        String password = passwordTxT.getText()+"";
        if(!password.equals("")) {
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.show();
            fbAuth.signInWithEmailAndPassword(userName, password)
                    .addOnCompleteListener(thisActivity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                Intent intentData = new Intent(thisActivity, HomePageActivity.class);
                                finish();
                                startActivity(intentData);
                            } else {
                                statusRegisterMsg.setText(getString(R.string.invalid_details));
                                statusRegisterMsg.setTextColor(Color.RED);
                            }
                        }
                    });
        }
        else{
            statusRegisterMsg.setText(getString(R.string.password_miss));
        }
    }

//check if we just made successful register
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == resultCode){
            statusRegisterMsg.setText(getString(R.string.register_success));
            statusRegisterMsg.setTextColor(Color.BLACK);
            fbAuth.signOut();
        }
    }

}
