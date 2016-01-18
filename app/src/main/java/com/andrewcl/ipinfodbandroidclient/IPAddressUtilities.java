package com.andrewcl.ipinfodbandroidclient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by andrewliu on 1/18/16.
 */
public class IPAddressUtilities {

    public static ArrayList<String> stringArrayFromRange(Boolean skipSearch, String startIPAddressRange, String stopIPAddressRange) {
        int startInteger = convertIPAddressStringToInt(startIPAddressRange);
        int stopInteger = convertIPAddressStringToInt(stopIPAddressRange);

        //Switches order in case user has reversed ip address range fields
        if (startInteger > stopInteger) {
            int newStopInt = startInteger;
            startInteger = stopInteger;
            stopInteger = newStopInt;
        }

        ArrayList<String> ipAddressesArray = new ArrayList<>();
        while (startInteger <= stopInteger) {
            InetAddress inetAddress = ipIntToInetAddress(startInteger);
            ipAddressesArray.add(inetAddress.toString());
            startInteger += skipSearch ? 255 : 1;
        }
        return ipAddressesArray;
    }

    public static InetAddress ipIntToInetAddress(int ipAsInt) {
        byte[] ipAsByteArray = intToByteArray(ipAsInt);
        try {
            InetAddress inetAddress = InetAddress.getByAddress(ipAsByteArray);
            return inetAddress;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] intToByteArray(int a) {
        byte[] ret = new byte[4];
        ret[3] = (byte) (a & 0xFF);
        ret[2] = (byte) ((a >> 8) & 0xFF);
        ret[1] = (byte) ((a >> 16) & 0xFF);
        ret[0] = (byte) ((a >> 24) & 0xFF);
        return ret;
    }

    public static int convertIPAddressStringToInt(String ipAddressString) {
        String[] ipAddressInArray = ipAddressString.split("\\.");
        int ipAddress = 0;
        for (int i = 0; i < ipAddressInArray.length; i++) {
            int ipComponentAsInt = Integer.parseInt(ipAddressInArray[i]);
            ipAddress += ipComponentAsInt * Math.pow(256, 3 - i);
        }
        return ipAddress;
    }
}
