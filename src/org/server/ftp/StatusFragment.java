package org.server.ftp;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static org.server.ftp.Constants.*;

public class StatusFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.status, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    text1.append(String.format(ADDRESS, address.getHostAddress(), PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(Constants.PREFERENCE_PORT, Constants.DEFAULT_PORT)));
                }
            }
        } catch (SocketException ignored) {
        }
    }

}