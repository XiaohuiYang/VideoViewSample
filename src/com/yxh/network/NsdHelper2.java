package com.yxh.network;


import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;


public class NsdHelper2 {
  private final String DEBUG_TAG = NsdHelper2.class.getName();
  private final String TYPE = "_xyang._tcp.local.";
  private final String SERVICE_NAME = "LocalCommunication";

  private Context mContext;
  private JmDNS mJmDNS;
  private ServiceInfo mServiceInfo;
  private ServiceListener mServiceListener;
  private WifiManager.MulticastLock mMulticastLock;
  
  private List<ServiceInfo> services = new ArrayList<ServiceInfo>();

  public NsdHelper2(Context context) {
    mContext = context;
    try {
      WifiManager wifi = (WifiManager) mContext.getSystemService(android.content.Context.WIFI_SERVICE);
      WifiInfo wifiInfo = wifi.getConnectionInfo();
      int intaddr = wifiInfo.getIpAddress();

      byte[] byteaddr = new byte[]{
          (byte) (intaddr & 0xff),
          (byte) (intaddr >> 8 & 0xff),
          (byte) (intaddr >> 16 & 0xff),
          (byte) (intaddr >> 24 & 0xff)
      };
      InetAddress addr = InetAddress.getByAddress(byteaddr);
      mJmDNS = JmDNS.create(addr);
    } catch (IOException e) {
      Log.d(DEBUG_TAG, "Error in JmDNS creation: " + e);
    }
  }

  public void registerService(int port) {
    try {
      wifiLock();
      mServiceInfo = ServiceInfo.create(TYPE, SERVICE_NAME, port, SERVICE_NAME);
      mJmDNS.registerService(mServiceInfo);
    } catch (IOException e) {
      Log.d(DEBUG_TAG, "Error in JmDNS initialization: " + e);
    }
  }

  public void discoverServices() {
    mJmDNS.addServiceListener(TYPE, mServiceListener = new ServiceListener() {
      @Override
      public void serviceAdded(ServiceEvent serviceEvent) {
        ServiceInfo info = mJmDNS.getServiceInfo(serviceEvent.getType(), serviceEvent.getName());
        Log.d(DEBUG_TAG, "adding" + info.toString());
        Log.d(DEBUG_TAG, "fsdfsdfsdffsdf");
        services.add(info);
      }

      @Override
      public void serviceRemoved(ServiceEvent serviceEvent) {
    	  ServiceInfo info = mJmDNS.getServiceInfo(serviceEvent.getType(), serviceEvent.getName());
          for (ServiceInfo s : services) {
              if (s == info) {
                  services.remove(s);
              }
          }
      }

      @Override
      public void serviceResolved(ServiceEvent serviceEvent) {
        mJmDNS.requestServiceInfo(serviceEvent.getType(), serviceEvent.getName(), 1);
      }
    });
  }

  public void reset() {
    if (mJmDNS != null) {
      if (mServiceListener != null) {
        mJmDNS.removeServiceListener(TYPE, mServiceListener);
        mServiceListener = null;
      }
      mJmDNS.unregisterAllServices();
    }
    if (mMulticastLock != null && mMulticastLock.isHeld()) {
      mMulticastLock.release();
    }
  }

  private void wifiLock() {
    WifiManager wifiManager = (WifiManager) mContext.getSystemService(android.content.Context.WIFI_SERVICE);
    mMulticastLock = wifiManager.createMulticastLock(SERVICE_NAME);
    mMulticastLock.setReferenceCounted(true);
    mMulticastLock.acquire();
  }

public List<ServiceInfo> getServiceInfos() {
	return services;
}
}
