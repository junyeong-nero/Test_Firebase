package ad.agio.test_firebase.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import ad.agio.test_firebase.activities.LoginActivity;
import ad.agio.test_firebase.activities.NeighborActivity;
import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.FragmentProfileBinding;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.utils.RequestCodes;
import gun0912.tedbottompicker.TedBottomPicker;

import static ad.agio.test_firebase.activities.HomeActivity.authController;
import static ad.agio.test_firebase.activities.HomeActivity.currentUser;
import static ad.agio.test_firebase.activities.HomeActivity.userController;


public class ProfileFragment extends Fragment {

    private void log(String text) {
        Log.d(this.getClass().getSimpleName(), text);
    }
    private FragmentProfileBinding binding;

    // onCreate -> onCreateView -> onViewCreated -> onStart -> onResume

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        if(currentUser != null) {
            drawProfile(currentUser);
            setProfileImage(currentUser);
        } else {
            userController.readMe(me -> {
                currentUser = me;
                drawProfile(currentUser);
                setProfileImage(currentUser);
            });
        }

        binding.buttonNeighbor.setOnClickListener(v -> {
            startActivityForResult(new Intent(requireContext(), NeighborActivity.class),
                    RequestCodes.NEIGHBOR_ACTIVITY);
        });

        binding.imageSelect.setOnClickListener(v ->
                TedBottomPicker.with(getActivity())
                        .show(uri -> {
                            binding.imageProfile.setImageURI(uri);
                            userController.updateUser("profile", uri.getPath());
                            currentUser.setProfile(uri.getPath());
                            try {
                                userController.writeProfileImage(uri.getPath());
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            log(uri.getPath());
                        })
        );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void drawProfile(User user) {

        if(!isAdded())
            return;

        binding.layoutUser.removeAllViews();
        try {
            JSONObject obj = new JSONObject(user.toString());
            Iterator iterator = obj.keys();
            while (iterator.hasNext()) {
                String next = iterator.next().toString();
                if(isAdded()) {
                    TextView textView = new TextView(requireContext());
                    textView.setText(next);
                    binding.layoutUser.addView(textView, ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

                    EditText editText = new EditText(requireContext());
                    editText.setText(obj.getString(next));
                    binding.layoutUser.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setProfileImage(User user) {

        if(!isAdded())
            return;

        userController.readProfileImage(user.getUid(), bytes -> {
            // binding.imageProfile.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            if(isAdded())
                Glide.with(this)
                        .load(bytes)
                        .into(binding.imageProfile);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        log("onActivityResult");
        if (resultCode == RequestCodes.LOGOUT) {
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        }
        switch (requestCode) {
            case RequestCodes.NEIGHBOR_ACTIVITY:
                log("NEIGHBOR_ACTIVITY");

                if (data != null && data.hasExtra("neighbor")) {
                    String neighbor = data.getStringExtra("neighbor");
                    userController.updateUser("neighbor", neighbor);
                    currentUser.setNeighbor(neighbor);
                }

                drawProfile(currentUser);
                setProfileImage(currentUser);
                break;

            case RequestCodes.MENU_ACTIVITY:
                if(authController.isAuth()) {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new LoginFragment()).commit();
                }
                break;

            default:
                log("default!");
        }
    }
}