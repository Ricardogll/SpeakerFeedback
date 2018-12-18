package edu.upc.citm.android.speakerfeedback;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirestoreListenerService extends Service {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean firestone_list_flag=false;
    private String roomID;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("SpeakerFeedback","FirestoreListenerService.onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("SpeakerFeedback","FirestoreListenerService.onStartCommand");

        if(firestone_list_flag==false) {
            createForegroundNotification();
            roomID = intent.getStringExtra("roomID");
            db.collection("rooms").document(roomID).collection("polls")
                    .whereEqualTo("open",true).addSnapshotListener(pollListener);
        }
        return START_NOT_STICKY;
    }

    private void createForegroundNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);


        //Creem una notificació o crode, startForeground (Perque el servei no pari mai fins que tu no ho diguis)
        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle(String.format("Connectat a rooom"))
                .setSmallIcon(R.drawable.ic_message)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1,notification);
        firestone_list_flag = true;

    }
    private void createNewPollNotification(String question) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);


        //Creem una notificació o crode, startForeground (Perque el servei no pari mai fins que tu no ho diguis)
        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle(question)
                .setSmallIcon(R.drawable.ic_message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{ 1000, 1000})
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .build();


        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(2,notification);

    }

    private EventListener<QuerySnapshot> pollListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null) {
                Log.e("SpeakerFreedback", "Error al rebre la llista de polls");
                return;
            }

            List<Poll> polls = new ArrayList<>();
            for (DocumentSnapshot doc : documentSnapshots) {
                Poll poll = doc.toObject(Poll.class);
                poll.setId(doc.getId());
                polls.add(poll);
                Log.i("SpeakerFeedback", String.format("Nova pregunta %s",poll.getQuestion()));
                createNewPollNotification(poll.getQuestion());
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("SpeakerFeedback","FirestoreListenerService.onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
