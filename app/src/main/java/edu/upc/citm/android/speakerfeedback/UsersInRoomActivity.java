package edu.upc.citm.android.speakerfeedback;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class UsersInRoomActivity extends AppCompatActivity {

    private List<String> item_list;
    private RecyclerView mylist;
    private Adapter adapter;
    private ListenerRegistration usersRegistration;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_in_room);

        item_list=new ArrayList<>();


        mylist = findViewById(R.id.users_list);
        mylist.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        mylist.setAdapter(adapter);

        usersRegistration = db.collection("users").whereEqualTo("room", "testroom").addSnapshotListener(usersListener);
    }


    private EventListener<QuerySnapshot> usersListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null) {
                Log.e("SpeakerFeedback", "Error al rebre usuaris dins d'un room", e);
                return;
            }
            item_list.clear();
            //String usersNames ="";
            for(DocumentSnapshot doc : documentSnapshots){
                //usersNames+=doc.getString("name") + "\n";
                item_list.add(doc.getString("name"));
            }

            adapter.notifyDataSetChanged();

        }
    };

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView item;


        public ViewHolder(View itemView){
            super(itemView);
            this.item=itemView.findViewById(R.id.item);

        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder>{

        @Override public int getItemCount() {  return item_list.size();  }

        @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View itemView = getLayoutInflater().inflate(R.layout.user_in_room_item , parent, false);
            return new ViewHolder(itemView);

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position){
            String model_item = item_list.get(position);
            holder.item.setText(model_item);


        }

    }
}
