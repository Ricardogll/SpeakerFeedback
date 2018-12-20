package edu.upc.citm.android.speakerfeedback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
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
        Intent data = new Intent();
        data.putExtra("room_name", room);
        setResult(RESULT_OK, data);
        finish();
    }


    private void readItemList() {
        try {
            FileInputStream inputStream = openFileInput("items.txt");
            InputStreamReader reader = new InputStreamReader(inputStream);
            Scanner scanner = new Scanner(reader);
            //items = new ArrayList<>();

            //Pasar al adapter el room
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                //String[] parts = line.split(";");
                //items.add(new ShoppingItem(parts[0], parts[1].equals("true")));
            }
        }
        catch (FileNotFoundException e) {
            Log.e("ShoppingList", "readItemList: FileNotFoundException");
        }
    }



}
