package ch.hevs.android.demoapplication.ui.fragment.client;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.adapter.RecyclerAdapter;
import ch.hevs.android.demoapplication.db.async.client.GetClient;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;
import ch.hevs.android.demoapplication.ui.activity.LoginActivity;
import ch.hevs.android.demoapplication.ui.activity.MainActivity;
import ch.hevs.android.demoapplication.util.RecyclerViewItemClickListener;

public class ClientsFragment extends Fragment {

    private static final String TAG = "ClientsFragment";

    private List<ClientEntity> clients;
    private RecyclerView recyclerView;

    public ClientsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.accounts_fragment_title));
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        String user = settings.getString(MainActivity.PREFS_USER, null);
        if (user == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
        try {
            if (!new GetClient(getContext()).execute(user).get().isAdmin()) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
            //TODO: clients = new GetAllClients().execute().get();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clients_list, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.clientsRecyclerView);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContent, EditClientFragment.newInstance(null), "CreateClient")
                        .addToBackStack("accounts")
                        .commit();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (recyclerView != null) {
            try {
                //TODO: clients = new GetAllClients().execute().get();
                recyclerView.setAdapter(new RecyclerAdapter<>(clients, new RecyclerViewItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        Log.d(TAG, "clicked position:" + position);
                        Log.d(TAG, "clicked on: " + clients.get(position).getEmail());

                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.flContent, EditClientFragment.newInstance(clients.get(position)), "EditClient")
                                .addToBackStack("clients")
                                .commit();
                    }

                    @Override
                    public void onItemLongClick(View v, int position) {
                        Log.d(TAG, "longClicked position:" + position);
                        Log.d(TAG, "longClicked on: " + clients.get(position).getEmail());

                        createDeleteDialog(position);
                    }
                }));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    private void createDeleteDialog(final int position) {
        final ClientEntity client = clients.get(position);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.row_delete_item, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle(getString(R.string.fragment_title_delete_client));
        alertDialog.setCancelable(false);

        final TextView deleteMessage = (TextView) view.findViewById(R.id.tv_delete_item);
        deleteMessage.setText(String.format(getString(R.string.client_delete_msg), client.getEmail()));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.action_accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast toast = Toast.makeText(getContext(), getString(R.string.client_deleted), Toast.LENGTH_LONG);
                try {
                    //TODO: new DeleteClient(client.getEmail()).execute().get();
                    clients.remove(position);
                    recyclerView.getAdapter().notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                toast.show();
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setView(view);
        alertDialog.show();
    }
}
