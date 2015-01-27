package org.server.ftp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.server.ftp.Constants.*;

public class SettingsFragment extends Fragment {
    public static final Object[] charsets = Charset.availableCharsets().keySet().toArray();
    private ArrayAdapter<String> adapter;
    private SharedPreferences preferences;
    private UserManager manager;
    private final List<String> names = new ArrayList<>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        manager = ((ServerApplication) activity.getApplication()).getUserManager();
        updateNames();
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, android.R.id.text1, names);
    }

    private void updateNames() {
        names.clear();
        try {
            names.addAll(Arrays.asList(manager.getAllUserNames()));
        } catch (FtpException ignored) {
        }
        names.add(getString(R.string.new_user));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final EditText port = (EditText) view.findViewById(R.id.port);
        port.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                try {
                    Integer port = Integer.valueOf(v.getText().toString());
                    preferences.edit().putInt(PREFERENCE_PORT, port).apply();//todo: range
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });
        port.setText(String.valueOf(preferences.getInt(PREFERENCE_PORT, DEFAULT_PORT)));
        Spinner charset = (Spinner) view.findViewById(R.id.charset);
        ArrayAdapter<Object> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, charsets);
        charset.setAdapter(arrayAdapter);
        charset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String string = (String) parent.getAdapter().getItem(position);
                    Charset.forName(string);
                    preferences.edit().putString(PREFERENCE_CHARSET, string).apply();
                } catch (IllegalCharsetNameException | UnsupportedCharsetException ignored) {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        charset.setSelection(arrayAdapter.getPosition(preferences.getString(PREFERENCE_CHARSET, DEFAULT_CHARSET)));
        ListView users = (ListView) view.findViewById(R.id.users);
        users.setAdapter(adapter);
        users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == adapter.getCount() - 1) {
                    startActivityForResult(new Intent(getActivity(), UserActivity.class), UserActivity.REQUEST_CODE);
                } else {
                    startActivityForResult(new Intent(getActivity(), UserActivity.class).putExtra(EXTRA_NAME, names.get(position)), UserActivity.REQUEST_CODE);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        try {
        if (requestCode == UserActivity.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
//                    BaseUser user = (BaseUser) manager.getUserByName(data.getStringExtra(EXTRA_NAME));
//                    String password = data.getStringExtra(EXTRA_PASSWORD);
//                    if (password != null) {
//                        user.setPassword(password);
//                    }
//                    user.setEnabled(data.getBooleanExtra(EXTRA_ENABLED, false));
//                    user.setHomeDirectory(data.getStringExtra(EXTRA_ROOT));
//                    ArrayList<Authority> authorities = new ArrayList<>(3);
//                    if (!data.getBooleanExtra(EXTRA_READONLY, true)) {
//                        authorities.add(new WritePermission());
//                    }
//                    authorities.add(new ConcurrentLoginPermission(0, 0));
//                    authorities.add(new TransferRatePermission(0, 0));
//                    user.setAuthorities(authorities);
//                    manager.save(user);
                updateNames();
                adapter.notifyDataSetChanged();
            }
        }
//        } catch (FtpException e) {
//            e.printStackTrace();
//        }
    }
}