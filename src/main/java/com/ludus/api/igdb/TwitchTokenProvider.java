package com.ludus.api.igdb;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Clock;

@Component
public class TwitchTokenProvider {
    private final RestClient http;
    private final String clientId, secret, staticToken;
    private final Clock clock;
    private String cached;
    private long expiresAt, retryAt;
    @Autowired
    public TwitchTokenProvider(@Value("${igdb.client-id:}") String clientId,
            @Value("${igdb.client-secret:}") String secret, @Value("${igdb.access-token:}") String staticToken) {
        this(HttpClients.create("https://id.twitch.tv"), clientId, secret, staticToken, Clock.systemUTC());
    }
    public TwitchTokenProvider(RestClient http, String clientId, String secret, String staticToken, Clock clock) {
        this.http=http; this.clientId=clientId; this.secret=secret; this.staticToken=staticToken; this.clock=clock;
    }
    public String clientId() { return clientId; }
    public boolean renewable() { return !secret.isBlank(); }
    public synchronized String token() {
        if (clientId.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"IGDB no configurado");
        if (!renewable()) {
            if (staticToken.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"IGDB no configurado");
            return staticToken;
        }
        long now=clock.millis();
        if(cached!=null && now<expiresAt) return cached;
        if(now<retryAt) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"OAuth temporalmente no disponible");
        retryAt=now+5000;
        var form=new LinkedMultiValueMap<String,String>();
        form.add("client_id",clientId); form.add("client_secret",secret); form.add("grant_type","client_credentials");
        try {
            var result=http.post().uri("/oauth2/token").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form).retrieve().body(Token.class);
            if(result==null || result.access_token()==null || result.access_token().isBlank()
                    || result.expires_in()<=0) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Respuesta OAuth invalida");
            cached=result.access_token();
            expiresAt=now+Math.max(1,result.expires_in()-Math.min(60,result.expires_in()/2))*1000;
            retryAt=0;
            return cached;
        } catch(ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,"OAuth no respondio a tiempo");
        } catch(RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"No fue posible autenticar con Twitch");
        }
    }
    public synchronized void invalidate(String rejected) {
        if(rejected.equals(cached)) {cached=null; expiresAt=0;}
    }
    @JsonIgnoreProperties(ignoreUnknown=true)
    public record Token(String access_token, long expires_in) {}
}
