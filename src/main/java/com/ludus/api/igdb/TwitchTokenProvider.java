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
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TwitchTokenProvider.class);
    private final RestClient http;
    private final String clientId, secret, staticToken;
    private final Clock clock;
    private String cached;
    private long expiresAt, retryAt;
    private String lastFailure = "OAuth temporalmente no disponible";
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
        if(now<retryAt) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,lastFailure);
        var form=new LinkedMultiValueMap<String,String>();
        form.add("client_id",clientId); form.add("client_secret",secret); form.add("grant_type","client_credentials");
        try {
            var result=http.post().uri("/oauth2/token").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form).retrieve().body(Token.class);
            if(result==null || result.access_token()==null || result.access_token().isBlank()
                    || result.expires_in()<=0) {
                failed("Respuesta OAuth invalida", "invalid_response", null);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,lastFailure);
            }
            cached=result.access_token();
            expiresAt=clock.millis()+Math.max(1,result.expires_in()-Math.min(60,result.expires_in()/2))*1000;
            retryAt=0;
            return cached;
        } catch(ResourceAccessException e) {
            failed("No fue posible conectar con Twitch OAuth; revise salida HTTPS, DNS y TLS del servidor", "connection", null);
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,lastFailure);
        } catch(RestClientResponseException e) {
            int status=e.getStatusCode().value();
            String reason = switch(status) {
                case 400, 401 -> "Twitch rechazo las credenciales OAuth; revise Client ID y Client Secret de la misma aplicacion";
                case 403 -> "Twitch denego la solicitud OAuth; revise la aplicacion y el acceso desde el servidor";
                case 429 -> "Twitch limito las solicitudes OAuth; reintente mas tarde";
                default -> "Twitch OAuth no disponible";
            };
            failed(reason, "http", status);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,lastFailure);
        } catch(RestClientException e) {
            failed("Respuesta no valida de Twitch OAuth", "response", null);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,lastFailure);
        }
    }
    private void failed(String reason, String category, Integer status) {
        lastFailure=reason;
        // Start cooldown after failure, including requests that took longer than five seconds.
        retryAt=clock.millis()+5000;
        // Never log exception messages, bodies, credentials or the returned access token.
        log.warn("Twitch OAuth failed: category={} providerStatus={} retryDelaySeconds=5", category, status);
    }
    public synchronized void invalidate(String rejected) {
        if(rejected.equals(cached)) {cached=null; expiresAt=0;}
    }
    @JsonIgnoreProperties(ignoreUnknown=true)
    public record Token(String access_token, long expires_in) {}
}
