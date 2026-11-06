package com.ludus.api.igdb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Clock;
import java.util.*;

@Component
public class IgdbClient {
    private final RestClient http;
    private final TwitchTokenProvider tokens;
    private final Clock clock;
    private long cooldown;
    private final java.util.ArrayDeque<Long> requests=new java.util.ArrayDeque<>();
    private final java.util.concurrent.Semaphore inflight=new java.util.concurrent.Semaphore(8);
    private final Map<String,Entry> cache=new LinkedHashMap<>();
    private record Entry(Object value,long until) {}
    @Autowired public IgdbClient(TwitchTokenProvider tokens) {
        this(HttpClients.create("https://api.igdb.com/v4"),tokens,Clock.systemUTC());
    }
    public IgdbClient(RestClient http,TwitchTokenProvider tokens,Clock clock) {
        this.http=http;this.tokens=tokens;this.clock=clock;
    }
    public IgdbGame[] games(String query) { return request("/games",query,IgdbGame[].class); }
    public IgdbGame.Named[] lookup(String endpoint,String query) {return request(endpoint,query,IgdbGame.Named[].class);}
    private synchronized void acquire() {
        long now=clock.millis();
        while(!requests.isEmpty() && requests.peekFirst()<=now-1000) requests.removeFirst();
        if(requests.size()>=4 || now<cooldown) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Limite temporal de IGDB; reintente");
        requests.addLast(now); // Cuatro solicitudes/s por instancia, sin dormir hilos.
    }
    private <T> T request(String endpoint,String query,Class<T> type) {
        String key=endpoint+query;
        synchronized(cache) {
            var hit=cache.get(key);
            if(hit!=null && hit.until()>clock.millis()) return type.cast(hit.value());
            cache.remove(key);
        }
        String token=tokens.token();
        for(int attempt=0;attempt<2;attempt++) {
            acquire();
            if(!inflight.tryAcquire()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"IGDB ocupado; reintente");
            try {
                T body=http.post().uri(endpoint).header("Client-ID",tokens.clientId())
                        .header("Authorization","Bearer "+token)
                        .contentType(new MediaType("text","plain",java.nio.charset.StandardCharsets.UTF_8))
                        .body(query).retrieve().body(type);
                if(body==null) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Respuesta vacia de IGDB");
                synchronized(cache) {
                    cache.entrySet().removeIf(e->e.getValue().until()<=clock.millis());
                    if(cache.size()>=200) cache.remove(cache.keySet().iterator().next());
                    cache.put(key,new Entry(body,clock.millis()+60000));
                }
                return body;
            } catch(RestClientResponseException e) {
                int status=e.getStatusCode().value();
                if(status==401 && attempt==0 && tokens.renewable()) {
                    tokens.invalidate(token); token=tokens.token();
                    // El segundo intento tambien respeta la cuota y ocurre como maximo una vez.
                    continue;
                }
                if(status==429) {
                    long seconds=1;
                    try { seconds=Math.max(1,Math.min(60,Long.parseLong(e.getResponseHeaders().getFirst("Retry-After")))); }
                    catch(Exception ignored) {}
                    synchronized(this) {cooldown=clock.millis()+seconds*1000;}
                    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"IGDB alcanzo su cuota");
                }
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"IGDB rechazo la consulta");
            } catch(ResourceAccessException e) {
                throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,"IGDB no respondio a tiempo");
            } catch(RestClientException e) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Respuesta no valida de IGDB");
            } finally {
                inflight.release();
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"IGDB no disponible");
    }
}
