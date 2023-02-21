package com.manuni.groceryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.manuni.groceryapp.databinding.ActivityFeedbackBinding;

public class FeedbackActivity extends AppCompatActivity {
    ActivityFeedbackBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.sendButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();

            }
        });
        binding.clearButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.nameEditTextId.setText("");
                binding.messageEditTextId.setText("");
            }
        });
    }

    private void sendFeedback() {
        if (binding.nameEditTextId.getText().toString().isEmpty()){
            binding.nameEditTextId.setError("Name Required");
            return;
        }
        if (binding.messageEditTextId.getText().toString().isEmpty()){
            binding.messageEditTextId.setError("Your Feedback Required");
            return;
        }
        String name = binding.nameEditTextId.getText().toString();
        String feedback = binding.messageEditTextId.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/email");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"bappimatubber1997@gmail.com","sabbir.islam.masud@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT,"Feedback for this app");
        intent.putExtra(Intent.EXTRA_TEXT,"Name: " + name + "\nMessage: "+feedback);
        try {
            startActivity(Intent.createChooser(intent, "Feedback With"));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}