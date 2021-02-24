package ad.agio.test_firebase.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.function.Consumer;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.activities.ChatActivity;
import ad.agio.test_firebase.controller.ChatController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.FragmentChatBinding;
import ad.agio.test_firebase.utils.GraphicComponents;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private void log(String t) {
        Log.e(this.getClass().getSimpleName(), t);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false);

        UserController userController = new UserController();
        userController.readMe(me -> draw(me.getArrayChatId()));

        return binding.getRoot();
    }

    private void draw(String rawData) {
        GraphicComponents g = new GraphicComponents(requireContext());

        cook(rawData, chatId -> {
            log(chatId);
            View view = getLayoutInflater().inflate(R.layout.chat_thumb_inflater, null);
            TextView t1 = view.findViewById(R.id.title);
            TextView t2 = view.findViewById(R.id.subtitle);

            ChatController chatController = new ChatController(chatId);
            chatController.readChat(chat -> {
                String[] split = chat.text.split("\n");
                t1.setText(chat.chatName);
                t2.setText(split[split.length - 1]);
            });

            Button button = view.findViewById(R.id.button);
            button.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ChatActivity.class);
                intent.putExtra("chatId", chatId);
                startActivity(intent);
            });

            View line = new View(requireContext());
            line.setBackgroundColor(Color.BLACK);
            binding.layout.addView(line, g.getScreenWidth(), g.dp(1));

            binding.layout.addView(view);
        });
    }

    private void cook(String rawData, Consumer<String> consumer) {
        String[] split = rawData.split("\n");
        for (String chatId : split) {
            if (chatId != null && !chatId.equals("")) {
                consumer.accept(chatId);
            }
        }
    }
}