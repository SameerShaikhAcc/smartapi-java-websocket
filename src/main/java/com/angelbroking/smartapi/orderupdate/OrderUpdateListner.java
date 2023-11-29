package com.angelbroking.smartapi.orderupdate;

import com.angelbroking.smartapi.smartstream.models.SmartStreamError;

public interface OrderUpdateListner {
    void onConnected();
    void onDisconnected();
    void onError(SmartStreamError error);
    void onPong();

    void onOrderUpdate(String data);
}
