package com.angelbroking.smartapi.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A wrapper for user id, access token, refresh token.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenSet {

	@SerializedName("clientcode")
	public String userId;
	@SerializedName("access_token")
	public String accessToken;
	@SerializedName("refresh_token")
	public String refreshToken;

}
