package ch.hevs.android.demoapplication.ui.fragment.client;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.entity.ClientEntity;
import ch.hevs.android.demoapplication.ui.activity.MainActivity;

public class EditClientFragment extends Fragment {

    private final String TAG = "EditClientFragment";
    private static final String ARG_PARAM1 = "clientEmail";

    private ClientEntity mClient;
    private boolean mAdminMode;
    private Toast mToast;
    private String mClientUid;

    private EditText mEtFirstName;
    private EditText mEtLastName;
    private EditText mEtEmail;
    private EditText mEtPwd1;
    private EditText mEtPwd2;
    private Switch mAdminSwitch;

    public EditClientFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param clientUid Uid of ClientEntity.
     * @return A new instance of fragment EditClientFragment.
     */
    public static EditClientFragment newInstance(String clientUid) {
        EditClientFragment fragment = new EditClientFragment();
        Bundle args = new Bundle();

        if (!clientUid.isEmpty()) {
            args.putString(ARG_PARAM1, clientUid);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        mAdminMode = settings.getBoolean(MainActivity.PREFS_ADM, false);

        if (getArguments() != null) {
            mClientUid = getArguments().getString(ARG_PARAM1);
            ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.fragment_title_edit_client));
            mToast = Toast.makeText(getContext(), getString(R.string.client_edited), Toast.LENGTH_LONG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_client, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeForm();
        FirebaseDatabase.getInstance()
                .getReference("clients")
                .child(mClientUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            mClient = dataSnapshot.getValue(ClientEntity.class);
                            populateForm();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "getAll: onCancelled", databaseError.toException());
                    }
                });
    }

    private void initializeForm() {
        mEtFirstName = getActivity().findViewById(R.id.firstName);
        mEtLastName = getActivity().findViewById(R.id.lastName);
        mEtEmail = getActivity().findViewById(R.id.email);
        mEtPwd1 = getActivity().findViewById(R.id.password);
        mEtPwd2 = getActivity().findViewById(R.id.passwordRep);
        mAdminSwitch = getActivity().findViewById(R.id.adminSwitch);
        Button saveBtn = getActivity().findViewById(R.id.editButton);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges(
                        mEtFirstName.getText().toString(),
                        mEtLastName.getText().toString(),
                        mEtPwd1.getText().toString(),
                        mEtPwd2.getText().toString(),
                        mAdminSwitch.isChecked()
                );
            }
        });
    }

    private void populateForm() {
        mEtFirstName.setText(mClient.getFirstName());
        mEtLastName.setText(mClient.getLastName());
        mEtEmail.setText(mClient.getEmail());
        mEtEmail.setFocusable(false);
        mEtEmail.setLongClickable(false);
        mEtEmail.setEnabled(false);
        mAdminSwitch.setChecked(mClient.getAdmin());
        if (!mAdminMode) {
            mAdminSwitch.setVisibility(View.GONE);
        }
    }

    private void saveChanges(String firstName, String lastName, String pwd, String pwd2, boolean admin) {
        if (!pwd.equals(pwd2)) {
            mEtPwd1.setError(getString(R.string.error_incorrect_password));
            mEtPwd1.requestFocus();
            mEtPwd1.setText("");
            mEtPwd2.setText("");
        }
        mClient.setFirstName(firstName);
        mClient.setLastName(lastName);
        mClient.setPassword(pwd);
        mClient.setAdmin(admin);
        updateClient(mClient);
    }

    private void updateClient(final ClientEntity client) {
        FirebaseDatabase.getInstance()
                .getReference("clients")
                .child(client.getUid())
                .updateChildren(client.toMap(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d(TAG, "Update failure!", databaseError.toException());
                        } else {
                            Log.d(TAG, "Update successful!");
                            getActivity().onBackPressed();
                            mToast.show();
                        }
                    }
                });
    }
}
