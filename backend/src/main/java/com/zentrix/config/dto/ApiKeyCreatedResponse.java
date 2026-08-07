package com.zentrix.config.dto;

import com.zentrix.config.ApiKey;

/** El campo apiKey (valor en claro) solo se devuelve una vez, al crear la clave. */
public record ApiKeyCreatedResponse(Integer id, String name, String prefix, String apiKey) {

    public static ApiKeyCreatedResponse of(ApiKey stored, String plainKey) {
        return new ApiKeyCreatedResponse(stored.getId(), stored.getName(), stored.getPrefix(), plainKey);
    }
}
