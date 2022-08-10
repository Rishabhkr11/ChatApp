package rishabh.example.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sdsmdg.tastytoast.TastyToast;

public class RegisterActivity extends AppCompatActivity {

    private Button CreateAccountButton;
    private EditText UserEmail, UserPassword;
    private TextView AlreadyHaveAccountLink;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();


        InitializeFields();

        AlreadyHaveAccountLink.setOnClickListener(view -> SendUserToLoginActivity());


        CreateAccountButton.setOnClickListener(view -> CreateNewAccount());
    }

    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            UserEmail.setError("Required");
            UserEmail.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            UserPassword.setError("Required");
            UserPassword.requestFocus();
        } else {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we are creating new account...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            String currentUserID = mAuth.getCurrentUser().getUid();
                            RootRef.child("Users").child(currentUserID).setValue("");

                            RootRef.child("Users").child(currentUserID).child("device_token")
                                    .setValue(deviceToken);

                            UserEmail.setText("");
                            UserPassword.setText("");
                            SendUserToMainActivity();
                            TastyToast.makeText(getApplicationContext(), "Registered successfully...", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                            loadingBar.dismiss();
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(RegisterActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    });
        }
    }

    private void InitializeFields() {
        CreateAccountButton = (Button) findViewById(R.id.register_button);
        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        AlreadyHaveAccountLink = (TextView) findViewById(R.id.already_have_an_account_link);
        loadingBar = new ProgressDialog(this);
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}