package com.tapsdk.antiaddiction.entities.request;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AuthenticateRequestParams implements Serializable {

    public String token = "";

    @SerializedName("game")
    public String gameIdentifier = "";

    public int carrier = -1;

    public AuthenticateRequestParams(String token, String gameIdentifier, int carrier) {
        this.token = token;
        this.gameIdentifier = gameIdentifier;
        this.carrier = carrier;
    }
}
