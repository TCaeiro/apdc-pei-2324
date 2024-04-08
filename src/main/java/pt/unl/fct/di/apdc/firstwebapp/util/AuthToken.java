package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.UUID;

public class AuthToken {
	public static final long EXPIRATION_TIME = 1000 * 60 * 60 * 2; // 2h
	public String username;
	public String tokenID;
	public long creationData;
	public long expirationData;

	public AuthToken(String username) {
		this.username = username;
		this.tokenID = UUID.randomUUID().toString();
		this.creationData = System.currentTimeMillis();
		this.expirationData = this.creationData + AuthToken.EXPIRATION_TIME;
	}
	
	
	
	public String getUsername() {
        return username;
    }

    // Getter para o tokenID
    public String getTokenID() {
        return tokenID;
    }

    // Getter para a data de criação
    public long getCreationData() {
        return creationData;
    }

    // Getter para a data de expiração
    public long getExpirationData() {
        return expirationData;
    }
}