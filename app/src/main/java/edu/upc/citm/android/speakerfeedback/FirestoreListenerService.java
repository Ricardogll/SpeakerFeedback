package edu.upc.citm.android.speakerfeedback;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class FirestoreListenerService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("SpeakerFeedback","FirestoreListenerService.onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("SpeakerFeedback","FirestoreListenerService.onStartCommand");

        createForegroundNotification();

        return START_NOT_STICKY;
    }

    private void createForegroundNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);


        //Creem una notificació o crode, startForeground (Perque el servei no pari mai fins que tu no ho diguis)
        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle(String.format("Connectat a Testroom"))
                .setSmallIcon(R.drawable.ic_message)
                .setContentIntent(pendingIntent).build();

        startForeground(1,notification);
    }

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
