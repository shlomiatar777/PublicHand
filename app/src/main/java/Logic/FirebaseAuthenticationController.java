package Logic;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.example.shlomi.publichand.LoginActivity;
import com.example.shlomi.publichand.R;
import com.example.shlomi.publichand.SettingUserDetailsActivity;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Shlomi on 29/03/2017.
 */

public class FirebaseAuthenticationController {

    FirebaseAuth fbAuth;

    public FirebaseAuthenticationController(){
        fbAuth =FirebaseAuth.getInstance();
    }


    public String getCurrentUid(){
      return  fbAuth.getCurrentUser().getUid();
    }
    public String getCurrentUEmail(){
        return   fbAuth.getCurrentUser().getEmail();
    }




    public void signOut(){
        fbAuth.signOut();
    }


    public boolean createToolBar (Activity activity, Menu menu){
        activity.getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    public boolean executeToolBarOption(Activity activity, MenuItem item) {
        if (item.getItemId() == R.id.edit) {
            Intent intentData = new Intent(activity, SettingUserDetailsActivity.class);
            activity.startActivity(intentData);
        } else if (item.getItemId() == R.id.sign_out) {
            fbAuth.signOut();
            Intent intentData = new Intent(activity, LoginActivity.class);
            intentData.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.finish();
            activity.startActivity(intentData);
        }
        return true;
    }


}
