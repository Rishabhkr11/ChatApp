package rishabh.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.transition.TransitionManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText registeredEmail;
    private Button resetPasswordBtn;
    private TextView goBack, emailIconText;

    private ViewGroup emailIconContainer;
    private ImageView emailIcon;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        registeredEmail = findViewById(R.id.forgot_password_email);
        resetPasswordBtn = findViewById(R.id.reset_password_button);
        goBack = findViewById(R.id.tv_forgot_password_go_back);
        emailIcon = findViewById(R.id.forgot_password_email_icon);
        emailIconContainer = findViewById(R.id.forgot_password_email_icon_container);
        emailIconText = findViewById(R.id.forgot_password_email_icon_text);
        progressBar = findViewById(R.id.forgot_password_progressBar);

        mAuth = FirebaseAuth.getInstance();

        registeredEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TransitionManager.beginDelayedTransition(emailIconContainer);
                emailIconText.setVisibility(View.GONE);

                TransitionManager.beginDelayedTransition(emailIconContainer);
                emailIcon.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                resetPasswordBtn.setEnabled(false);
                resetPasswordBtn.setTextColor(Color.argb(50, 255, 255, 255));

                mAuth.sendPasswordResetEmail(registeredEmail.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    ScaleAnimation scaleAnimation = new ScaleAnimation(1,0,1,0,emailIcon.getWidth()/2,emailIcon.getHeight()/2);
                                    scaleAnimation.setDuration(100);
                                    scaleAnimation.setInterpolator(new AccelerateInterpolator());
                                    scaleAnimation.setRepeatMode(Animation.REVERSE);
                                    scaleAnimation.setRepeatCount(1);

                                    scaleAnimation.setAnimationListener(new Animation.AnimationListener(){

                                        @Override
                                        public void onAnimationStart(Animation animation) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            emailIconText.setText("Recovery email sent successfully! Check your Inbox");
                                            emailIconText.setTextColor(getResources().getColor(R.color.successGreen));
                                            emailIconText.setVisibility(View.VISIBLE);

                                            TransitionManager.beginDelayedTransition(emailIconContainer);
                                            emailIcon.setVisibility(View.VISIBLE);
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) {
                                            emailIcon.setImageResource(R.drawable.green_email);
                                        }
                                    });

                                    emailIcon.startAnimation(scaleAnimation);
                                }
                                else {
                                    String error = task.getException().getMessage();

                                    resetPasswordBtn.setEnabled(true);
                                    resetPasswordBtn.setTextColor(Color.rgb(255, 255, 255));

                                    emailIconText.setText(error);
                                    emailIconText.setTextColor(getResources().getColor(R.color.failRed));
                                    TransitionManager.beginDelayedTransition(emailIconContainer);
                                    emailIconText.setVisibility(View.VISIBLE);
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resetPasswordLoginIntent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                startActivity(resetPasswordLoginIntent);
            }
        });
    }

    private void checkInputs() {
        if (TextUtils.isEmpty(registeredEmail.getText())){
            resetPasswordBtn.setEnabled(false);
            resetPasswordBtn.setTextColor(Color.argb(50, 255, 255, 255));
        }
        else {
            resetPasswordBtn.setEnabled(true);
            resetPasswordBtn.setTextColor(Color.rgb(255, 255, 255));
        }
    }
}