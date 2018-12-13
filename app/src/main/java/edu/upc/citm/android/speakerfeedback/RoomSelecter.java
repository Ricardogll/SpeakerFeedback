package edu.upc.citm.android.speakerfeedback;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class RoomSelecter extends AppCompatActivity {

    private EditText room_name;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration roomRegistration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_selecter);
        room_name = findViewById(R.id.edit_room_enter);


        roomRegistration = db.collection("rooms").whereEqualTo("open",true).addSnapshotListener(this, roomListener);



    }

    private EventListener<QuerySnapshot> roomListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null) {
                Log.e("SpeakerFeedback", "Error getting users", e);
                return;
            }



        }
    };

    public void onEnterRoomClick(View view)
    {

        //if (room exists)
        String room = room_name.getText().toString();
        Intent data = new Intent();
        data.putExtra("room_name", room);
        setResult(RESULT_OK, data);
        finish();
    }
}
