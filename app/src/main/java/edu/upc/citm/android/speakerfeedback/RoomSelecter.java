package edu.upc.citm.android.speakerfeedback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class RoomSelecter extends AppCompatActivity {

    private EditText room_name;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView mylist;
    private Adapter adapter;
    private List<String> rooms = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_selecter);
        room_name = findViewById(R.id.edit_room_enter);


        mylist = findViewById(R.id.users_list);
        mylist.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        mylist.setAdapter(adapter);


    }

    private EventListener<QuerySnapshot> roomListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null) {
                Log.e("SpeakerFeedback", "Error getting users", e);
                return;
            }

            /*for(DocumentSnapshot doc : documentSnapshots){

                if(doc.getString("name") == room_name.getText().toString()){
                    room_exists=true;
                }
            }*/

        }
    };

    //@Override
    public void onCreateDialog(final String room, final String real_pass) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();
        View roomView = inflater.inflate(R.layout.password, null);
        final EditText pass_edit = roomView.findViewById(R.id.password);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(roomView)
                // Add action buttons
                .setPositiveButton("Sign in", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...

                        String pass = pass_edit.getText().toString();
                        if (pass.equals(real_pass)) {
                            //Enter the room
                            Toast.makeText(RoomSelecter.this, "Correct password", Toast.LENGTH_SHORT).show();
                            ReturnRoomName(room);
                        } else {
                            Toast.makeText(RoomSelecter.this, "Wrong password", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.create().show();

    }

    public void onEnterRoomClick(View view)
    {
        final String room = room_name.getText().toString();

        db.collection("rooms").document(room).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    //hemos encontrado la room.

                    if(documentSnapshot.contains("open") && documentSnapshot.getBoolean("open"))
                    {
                        if(documentSnapshot.contains("password") && !documentSnapshot.getString("password").equals(""))
                        {
                          onCreateDialog(room, documentSnapshot.getString("password"));

                        }
                        else
                        {
                            ReturnRoomName(room);
                        }
                    }
                    else
                    {
                        Toast.makeText(RoomSelecter.this, "Room closed", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(RoomSelecter.this, "Room doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //fallo en la red
            }
        });

    }

    private void ReturnRoomName(String room) {
        if(!rooms.contains(room))
            rooms.add(room);
        Intent data = new Intent();
        data.putExtra("room_name", room);
        setResult(RESULT_OK, data);
        finish();
    }

    private void GetRoomID(int pos)
    {
        ReturnRoomName(rooms.get(pos));
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        Button item;


        public ViewHolder(View itemView){
            super(itemView);
            this.item=itemView.findViewById(R.id.item);
            this.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    GetRoomID(pos);
                }
            });
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder>{

        @Override public int getItemCount() {  return rooms.size();  }

        @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View itemView = getLayoutInflater().inflate(R.layout.user_in_room_item , parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position){
            String model_item = rooms.get(position);
            holder.item.setText(model_item);
        }
    }

}
