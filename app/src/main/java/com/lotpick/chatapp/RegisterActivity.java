package com.lotpick.chatapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lotpick.chatapp.Model.User;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    MaterialEditText username, email, password;
    Button btReg;

    boolean hasTaken;

    FirebaseAuth auth;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //TOOLBAR
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btReg = findViewById(R.id.btReg);

        auth = FirebaseAuth.getInstance();
        btReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String txUser = Objects.requireNonNull(username.getText()).toString();
                final String txEmail = Objects.requireNonNull(email.getText()).toString();
                final String txPass = Objects.requireNonNull(password.getText()).toString();

                if (TextUtils.isEmpty(txUser) || TextUtils.isEmpty(txEmail) || TextUtils.isEmpty(txPass)) {
                    Toast.makeText(RegisterActivity.this, "Fill everything", Toast.LENGTH_SHORT).show();
                } else if (txPass.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password should have atleast  6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    hasTaken = false;

                    DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Users");

                    reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User user = snapshot.getValue(User.class);


                                assert user != null;
                                if (user.getUsername().equals(username.getText().toString())) {
                                    hasTaken = true;
                                    break;
                                }
                            }
                            if (hasTaken) {
                                Toast.makeText(RegisterActivity.this, "Username taken", Toast.LENGTH_SHORT).show();
                            } else {
                                register(txUser, txEmail, txPass);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }
        });
    }



    private void register(final String username, final String email, final String password) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;
                            String userid = firebaseUser.getUid();

                            ref = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("username", username);
                            hashMap.put("imageURL", "default");
                            hashMap.put("status", "offline");

                            ref.setValue(hashMap);
                        } else {
                            Toast.makeText(RegisterActivity.this, "Check Email or Password", Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }
}
