package edu.upc.citm.android.speakerfeedback;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.ExecutorService;

public class RoomSelecter extends AppCompatActivity {

    private EditText room_name;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration roomRegistration;
    private boolean room_exists=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_selecter);
        room_name = findViewById(R.id.edit_room_enter);





    }

    private EventListener<QuerySnapshot> roomListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null) {
                Log.e("SpeakerFeedback", "Error getting users", e);
                return;
            }

            for(DocumentSnapshot doc : documentSnapshots){

                if(doc.getString("name") == room_name.getText().toString()){
                    room_exists=true;
                }
            }

        }
    };

    public void onEnterRoomClick(View view)
    {
        String room = room_name.getText().toString();

        db.collection("rooms").document(room).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    //hemos encontrado la room. TODO: poner el if de abajo aqui y mirar si esta open = true
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //fallo en la red
            }
        });


        if (room_exists) {
            Intent data = new Intent();
            data.putExtra("room_name", room);
            setResult(RESULT_OK, data);
            finish();
        }else{

            //room not found
        }
    }
}
