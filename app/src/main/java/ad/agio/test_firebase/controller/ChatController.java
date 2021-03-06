package ad.agio.test_firebase.controller;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import ad.agio.test_firebase.domain.Chat;
import ad.agio.test_firebase.domain.Meeting;
import ad.agio.test_firebase.domain.User;

public class ChatController {
    public String chatId;
    private Chat currentChat;
    private DatabaseReference db;

    public ChatController(String chatId) {
        this.chatId = chatId;
        this.db = FirebaseDatabase.getInstance().getReference()
                .child("chat")
                .child(chatId);
    }

    public void writeChat(Chat chat) {
        db.setValue(chat);
    }

    public void readChat(Consumer<Chat> consumer) {
        db.get()
                .addOnSuccessListener(dataSnapshot -> {
                            if(dataSnapshot.exists() && dataSnapshot != null)
                                consumer.accept(dataSnapshot.getValue(Chat.class));
                        })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public void removeChat() {
        removeTextListener();
        removeConfirmListener();
        db.removeValue();
    }

    public void writeUser(User user) {
        db.child("users")
                .child(user.getUid())
                .setValue(user);
    }

    public void removeUser(String uid) {
        db.child("users")
                .child(uid)
                .removeValue();
    }

    public void writeUserOnComplete(User user, Consumer<Task> consumer) {
        db.child("users")
                .child(user.getUid())
                .setValue(user)
                .addOnCompleteListener(consumer::accept);
    }

    public void writeMeeting(Meeting meeting) {
        db.child("meeting")
                .setValue(meeting);
    }

    public void readMeeting(Consumer<Meeting> consumer) {
        db.child("meeting")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        consumer.accept(task.getResult().getValue(Meeting.class));
                    }
                });

    }

    public void sendMatchResult(String result) {
        db.child("result")
                .setValue(result);
    }

    private ValueEventListener confirmListener;
    public void addConfirmListener(Consumer<String> consumer) {
        // db/chat/chatId/match 를 확인하는 listener
        // receiver 동의했을 때 success 문자열이 들어오는 것을 확인
        confirmListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String post = snapshot.getValue(String.class);
                if (snapshot.exists() && post != null && !post.equals(""))
                    consumer.accept(post);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        db.child("result") // Check chat's match child
                .addValueEventListener(confirmListener);
    }
    public void removeConfirmListener() {
        if(confirmListener != null)
            db.child("result")
                .removeEventListener(confirmListener);
        confirmListener = null;
    }

    public void readUserBy(Predicate<User> condition, Consumer<ArrayList<User>> consumer) {
        db.child("users")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        ArrayList<User> list = new ArrayList<>();
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            User user = d.getValue(User.class);
                            if (condition.test(user))
                                list.add(user);
                        }
                        consumer.accept(list);
                    } else {
                        consumer.accept(null);
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public void readAllUsers(Consumer<ArrayList<User>> consumer) {
        readUserBy(user -> true, consumer);
    }

    public void readOtherUsers(Consumer<ArrayList<User>> consumer) {
        AuthController auth = new AuthController();
        readUserBy(user -> !user.getUid().equals(auth.getUid()), consumer);
    }

    public void readMe(Consumer<ArrayList<User>> consumer) {
        AuthController auth = new AuthController();
        readUserBy(user -> user.getUid().equals(auth.getUid()), consumer);
    }

    public void sendText(User user, String text) {
        Calendar cal = Calendar.getInstance();
        String temp = user.getUserName() + "|"
                + user.getUid() + "|"
                + cal.get(Calendar.YEAR) + ":"
                + (cal.get(Calendar.MONTH) + 1) + ":"
                + cal.get(Calendar.DAY_OF_MONTH) + ":"
                + cal.get(Calendar.HOUR_OF_DAY) + ":"
                + cal.get(Calendar.MINUTE) + ":"
                + cal.get(Calendar.SECOND) + "|"
                + text + "\n";

        db.child("textChange").setValue(temp);
        if (textListener == null) {
            readText(re -> db.child("text").setValue(re + temp));
        } else {
            db.child("text").setValue(currentText + temp);
        }
    }

    public void readText(Consumer<String> consumer) {
        db.child("text")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    if(dataSnapshot.exists())
                        consumer.accept(dataSnapshot.getValue(String.class));
                });
    }

    private String currentText = "";
    private ValueEventListener textListener;
    public void addTextListener(Consumer<String> consumer) {
        textListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String temp = snapshot.getValue(String.class);
                    if(!currentText.equals(temp)) {
                        currentText = temp;
                        consumer.accept(currentText);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        db.child("text")
                .addValueEventListener(textListener);
    }

    public void removeTextListener() {
        if(textListener != null)
            db.child("text")
                .removeEventListener(textListener);
        textListener = null;
    }

    private String changeText = "";
    private ValueEventListener textChangeListener;
    public void addTextChangeListener(Consumer<String> consumer) {
        textChangeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String temp = snapshot.getValue(String.class);
                    if(!changeText.equals(temp)) {
                        changeText = temp;
                        consumer.accept(changeText);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        db.child("textChange")
                .addValueEventListener(textChangeListener);
    }

    public void removeTextChangeListener() {
        if(textChangeListener != null)
            db.child("text")
                .removeEventListener(textChangeListener);
        textChangeListener = null;
    }

}
