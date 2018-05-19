package sec.innovatesoft.secian_achatbot;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText emailText;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailText = findViewById(R.id.emailText);
        login = findViewById(R.id.login);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInternet();
            }
        });
    }

    private void checkInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Service.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {

                checkError();

            } else {
                Toast.makeText(LoginActivity.this,"Please check your internet connection", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(LoginActivity.this,"Please enable internet to chat", Toast.LENGTH_LONG).show();
        }
    }

    private void checkError() {
        String email = emailText.getText().toString().trim();

        if ((email.contains("@") && email.contains(".")) && email.length() > 6) {

            email = email.replace(".", "DOT");

            SharedPreferences.Editor editor = getSharedPreferences("secian", MODE_PRIVATE).edit();
            editor.putString("email", email);
            editor.apply();

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
        }
    }
}
