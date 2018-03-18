package com.rutgers.neemi.interfaces;

/**
 * Created by suitcase on 3/15/18.
 */

public interface AuthenticationListener {

    void onCodeReceived(String auth_token);

}