package edu.upc.citm.android.speakerfeedback;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REGISTER_USER = 0;
    private static final int ROOM_SELECTER = 1;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private TextView textView;
    private String userId;
    private ListenerRegistration roomRegistration;
    private ListenerRegistration usersRegistration;
    private List<Poll> polls = new ArrayList<>();
    private RecyclerView polls_view;
    private Adapter adapter;
    private String roomID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        polls_view = findViewById(R.id.pollsView);
        getRoomSelected();
        getOrRegisterUser();

        adapter = new Adapter();

        polls_view.setLayoutManager(new LinearLayoutManager(this));
        polls_view.setAdapter(adapter);


    }

    @Override
    protected void onDestroy() {
        exitRoom();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.close_menu,menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.menu_close:
                Log.i("SpeakerFeedback", "Closing app");

                stopFirestoreListenerService();

                finish();
                //System.exit(0);

                break;
        }

        return true;
    }

    private void enterRoom() {
        db.collection("users").document(userId).update("room", roomID);
        startFirestoreListenerService();

    }

    private void exitRoom() {
        db.collection("users").document(userId).update("room", FieldValue.delete());
    }

    private void startFirestoreListenerService() {
        Intent intent = new Intent(this, FirestoreListenerService.class);
        intent.putExtra("roomID", roomID);

        startService(intent);
    }

    private void stopFirestoreListenerService() {
        Intent intent = new Intent(this, FirestoreListenerService.class);
        stopService(intent);
    }

    private EventListener<DocumentSnapshot> roomListener = new EventListener<DocumentSnapshot>() {
        @Override
        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
            if (e != null) {
                Log.e("SpeakerFeedback", "Error al rebre rooms/rooom", e);
                return;
            }
            String name = documentSnapshot.getString("name");
            setTitle(name);
        }
    };

    private EventListener<QuerySnapshot> usersListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null) {
                Log.e("SpeakerFeedback", "Error al rebre usuaris dins d'un room", e);
                return;
            }
            textView.setText(String.format("Num user: %d", documentSnapshots.size()));
            //String usersNames ="";
            //for(DocumentSnapshot doc : documentSnapshots){
            //    usersNames+=doc.getString("name") + "\n";
            //}
            //textView.setText(usersNames);
        }
    };

    private EventListener<QuerySnapshot> pollListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null) {
                Log.e("SpeakerFreedback", "Error al rebre la llista de polls");
                return;
            }
            polls.clear();
            for (DocumentSnapshot doc : documentSnapshots) {
                Poll poll = doc.toObject(Poll.class);
                poll.setId(doc.getId());
                polls.add(poll);
            }
            //TODO: avisar adaptador
            Log.i("SpeakerFeedback", String.format("He carregat %d polls.", polls.size()));
            adapter.notifyDataSetChanged();
        }
    };


    @Override
    protected void onStart() {
        super.onStart();

        roomRegistration = db.collection("rooms").document(roomID).addSnapshotListener(this, roomListener);

        usersRegistration = db.collection("users").whereEqualTo("rooms", roomID).addSnapshotListener(this, usersListener);
        db.collection("rooms").document(roomID).collection("polls")
                .orderBy("start", Query.Direction.DESCENDING)
                .addSnapshotListener(this, pollListener);
    }

    //@Override
    // protected void onStop() {
    //    super.onStop();

    //    roomRegistration.remove();
    //    usersRegistration.remove();
    //   db.collection("users").document(userId).update("room", FieldValue.delete());
    //}

    private void getOrRegisterUser() {
        // Busquem a les preferències de l'app l'ID de l'usuari per saber si ja s'havia registrat
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        userId = prefs.getString("userId", null);
        if (userId == null) {
            // Hem de registrar l'usuari, demanem el nom
            Intent intent = new Intent(this, RegisterUserActivity.class);
            startActivityForResult(intent, REGISTER_USER);
            Toast.makeText(this, "Encara t'has de registrar", Toast.LENGTH_SHORT).show();
        } else {
            // Ja està registrat, mostrem el id al Log
            Log.i("SpeakerFeedback", "userId = " + userId);
        }
    }

    private void getRoomSelected()
    {
        Intent intent = new Intent(this, RoomSelecter.class);
        startActivityForResult(intent, ROOM_SELECTER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REGISTER_USER:
                if (resultCode == RESULT_OK) {
                    String name = data.getStringExtra("name");
                    registerUser(name);
                } else {
                    Toast.makeText(this, "Has de registrar un nom", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case ROOM_SELECTER:
                if (resultCode == RESULT_OK) {
                    String room_name = data.getStringExtra("room_name");
                    roomID = room_name;
                    if (userId != null && roomID != null) {
                        enterRoom();
                    }
                } else {
                    Toast.makeText(this, "Put a correct room ID", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void registerUser(String name) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", name);
        fields.put("last_active", new Date());
        db.collection("users").add(fields).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                // textview.setText(documentReference.getId());
                userId = documentReference.getId();
                SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
                prefs.edit()
                        .putString("userId", userId)
                        .commit();
                Log.i("SpeakerFeedback", "New user: userId = " + userId);
                enterRoom();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("SpeakerFeedback", "Error creant objecte", e);
                Toast.makeText(MainActivity.this,
                        "No s'ha pogut registrar l'usuari, intenta-ho més tard", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public void onClickUsers(View view) {
        Log.i("SpeakerFeedback", "Clicked bar");
        Intent intent = new Intent(this, UsersInRoomActivity.class);
        startActivity(intent);
    }

    public void onClickCardView(int pos) {


        final Poll current_poll = polls.get(pos);

        //String[] options = (String[]) current_poll.getOptions().toArray();


        //CharSequence[] options = new CharSequence[current_poll.getOptions().size()];

        if (current_poll.isOpen()) {
            List<String> optlist = current_poll.getOptions();
            String[] options = new String[optlist.size()];
            for (int i = 0; i < optlist.size(); i++) {
                options[i] = optlist.get(i);
                Log.i("SpeakerFeedback", options[i].toString());
            }

            Log.i("SpeakerFeedback", "Clicked poll");
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Vote")
                    //.setMessage("Get poll question here")
                    .setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            saveVote(current_poll.getId(), which);
                            dialog.dismiss();
                        }
                    }).show();
        }
    }

    private void saveVote(String pollid, int which) {


        Map<String, Object> map = new HashMap<String, Object>();
        map.put("option", which);
        map.put("pollid", pollid);
        db.collection("rooms").document(roomID).collection("votes").document(userId).set(map);
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView question_view;
        private TextView options_view;
        private TextView label_view;
        private CardView card_view;


        public ViewHolder(View itemView) {
            super(itemView);
            question_view = itemView.findViewById(R.id.question_view);
            options_view = itemView.findViewById(R.id.options_view);
            label_view = itemView.findViewById(R.id.label_view);
            card_view = itemView.findViewById(R.id.cardview);
            card_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    onClickCardView(pos);
                }
            });
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.poll_view, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Poll poll = polls.get(position);
            if (position == 0) {
                holder.label_view.setVisibility(View.VISIBLE);
                if (poll.isOpen())
                    holder.label_view.setText("Active");
                else
                    holder.label_view.setText("Previous");
            } else {
                if (!poll.isOpen() && polls.get(position - 1).isOpen()) {
                    holder.label_view.setText("Previous");
                    holder.label_view.setVisibility(View.VISIBLE);
                } else {
                    holder.label_view.setVisibility(View.GONE);
                }
            }

            holder.card_view.setCardElevation(poll.isOpen() ? 5.0f : 0.0f);

            holder.question_view.setText(poll.getQuestion());
            holder.options_view.setText(poll.getOptionsAsString());
        }

        @Override
        public int getItemCount() {
            return polls.size();
        }
    }
}

