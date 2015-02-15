package org.server.ftp;

import android.app.ListFragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static org.server.ftp.Constants.*;

public class StatusFragment extends ListFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1);
        try {
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
                NetworkInterface networkInterface = interfaces.nextElement();
                for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                    adapter.add(String.format(ADDRESS, address.getAddress().getHostAddress(), PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(Constants.PREFERENCE_PORT, Constants.DEFAULT_PORT)));
                }
            }
        } catch (SocketException ignored) {
        }
        setListAdapter(adapter);
    }
}
