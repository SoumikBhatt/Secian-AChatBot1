package sec.innovatesoft.secian_achatbot;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements AIListener{

    RecyclerView recyclerView;
    EditText editText;
    RelativeLayout addBtn;
    DatabaseReference ref;
    FirebaseRecyclerAdapter<ChatMessage, ChatHolder> adapter;
    static String email;

    private static String TAG = "MainActivity";

    static final AIConfiguration config = new AIConfiguration("0bacb4baaa0a47458e36c7c884daacb6",
            AIConfiguration.SupportedLanguages.English,
            AIConfiguration.RecognitionEngine.System);
    static final AIDataService aiDataService = new AIDataService(config);
    static final AIRequest aiRequest = new AIRequest();

    private AIService aiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        editText = findViewById(R.id.editText);
        addBtn = findViewById(R.id.addBtn);

        checkLogin();

    }

    private void checkLogin() {
        SharedPreferences prefs = getSharedPreferences("secian", MODE_PRIVATE);
        email = prefs.getString("email", null);

        if (email != null) {

            recyclerView.setHasFixedSize(true);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setStackFromEnd(true);
            recyclerView.setLayoutManager(linearLayoutManager);

            ref = FirebaseDatabase.getInstance().getReference();
            ref.keepSynced(true);

            aiService = AIService.getService(this, config);
            aiService.setListener(this);

            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkInternet();
                }
            });

            Query query = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("chat").child(email);

            FirebaseRecyclerOptions<ChatMessage> options = new FirebaseRecyclerOptions.Builder<ChatMessage>()
                    .setQuery(query, ChatMessage.class)
                    .build();

            adapter = new FirebaseRecyclerAdapter<ChatMessage, ChatHolder>(options) {

                @NonNull
                @Override
                public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.message_list, parent, false);

                    return new ChatHolder(view);
                }

                @Override
                protected void onBindViewHolder(@NonNull ChatHolder holder, int position, @NonNull ChatMessage model) {

                    if (model.getMsgUser().equals("user")) {
                        holder.rightText.setText(model.getMsgText());

                        holder.rightText.setVisibility(View.VISIBLE);
                        holder.leftText.setVisibility(View.GONE);
                    }
                    else {
                        holder.leftText.setText(model.getMsgText());

                        holder.rightText.setVisibility(View.GONE);
                        holder.leftText.setVisibility(View.VISIBLE);
                    }

                }
            };

            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);

                    int msgCount = adapter.getItemCount();
                    int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                    if (lastVisiblePosition == -1 || (positionStart >= (msgCount - 1) &&
                            lastVisiblePosition == (positionStart - 1))) {
                        recyclerView.scrollToPosition(positionStart);

                    }

                }
            });

            adapter.startListening();
            recyclerView.setAdapter(adapter);

        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    private static class SendMessage extends AsyncTask<AIRequest, Void, AIResponse> {

        @Override
        protected AIResponse doInBackground(AIRequest... aiRequests) {

            try {
                return aiDataService.request(aiRequest);
            } catch (AIServiceException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(AIResponse response) {
            super.onPostExecute(response);
            Log.e(TAG, "Response Not NULL");

            Result result = response.getResult();
            String message = result.getResolvedQuery();
            Log.e(TAG, "User = " + message);

            String reply = result.getFulfillment().getSpeech();
            Log.e(TAG, "Bot = " + reply);
            ChatMessage chatMessage = new ChatMessage(reply, "bot");
            FirebaseDatabase.getInstance().getReference().child("chat").child(email).push().setValue(chatMessage);
        }
    }

    @Override
    public void onResult(ai.api.model.AIResponse response) {

    }

    @Override
    public void onError(ai.api.model.AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    public void checkInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Service.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {

                String message = editText.getText().toString().trim();

                if (!message.equals("")) {

                    ChatMessage chatMessage = new ChatMessage(message, "user");
                    ref.child("chat").child(email).push().setValue(chatMessage);

                    aiRequest.setQuery(message);
                    new SendMessage().execute(aiRequest);
                }
                else {
                    aiService.startListening();
                }

                editText.setText("");

            } else {
                Toast.makeText(MainActivity.this,"Please check your internet connection", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this,"Please enable internet to chat", Toast.LENGTH_LONG).show();
        }
    }

}
