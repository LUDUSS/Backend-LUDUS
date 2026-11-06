package com.ludus.api.igdb;
import org.junit.jupiter.api.*;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
class TwitchTokenProviderTest {
    static class MutableClock extends Clock {
        long millis;
        public ZoneId getZone(){return ZoneOffset.UTC;}
        public Clock withZone(ZoneId zone){return this;}
        public Instant instant(){return Instant.ofEpochMilli(millis);}
        public long millis(){return millis;}
    }
    RestClient.Builder builder;MockRestServiceServer server;MutableClock clock;TwitchTokenProvider tokens;
    @BeforeEach void setup() {
        builder=RestClient.builder().baseUrl("https://id.twitch.tv");server=MockRestServiceServer.bindTo(builder).build();
        clock=new MutableClock();tokens=new TwitchTokenProvider(builder.build(),"client","test-secret","old-static",clock);
    }
    @Test void oauthPrecedenceCacheAndExpiry() {
        server.expect(requestTo("https://id.twitch.tv/oauth2/token")).andExpect(method(HttpMethod.POST))
            .andExpect(content().string("client_id=client&client_secret=test-secret&grant_type=client_credentials"))
            .andRespond(withSuccess("{\"access_token\":\"first\",\"expires_in\":120}",MediaType.APPLICATION_JSON));
        server.expect(anything()).andRespond(withSuccess("{\"access_token\":\"second\",\"expires_in\":120}",MediaType.APPLICATION_JSON));
        assertEquals("first",tokens.token());assertEquals("first",tokens.token());
        clock.millis=61000;assertEquals("second",tokens.token());server.verify();
    }
    @Test void concurrentCallersShareOneTokenRequest() throws Exception {
        server.expect(anything()).andRespond(withSuccess("{\"access_token\":\"shared\",\"expires_in\":3600}",MediaType.APPLICATION_JSON));
        try(var executor=Executors.newFixedThreadPool(8)) {
            var jobs=new java.util.ArrayList<Future<String>>();
            for(int i=0;i<8;i++)jobs.add(executor.submit(()->tokens.token()));
            for(var job:jobs)assertEquals("shared",job.get());
        }
        server.verify();
    }
    @Test void sanitizedFailureAndCooldown() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("secret-provider-response"));
        var error=assertThrows(ResponseStatusException.class,()->tokens.token());
        assertEquals(502,error.getStatusCode().value());assertFalse(error.toString().contains("secret-provider-response"));
        assertEquals(503,assertThrows(ResponseStatusException.class,()->tokens.token()).getStatusCode().value());server.verify();
    }
    @Test void staticFallbackNeedsNoOAuthCall() {
        tokens=new TwitchTokenProvider(builder.build(),"client","","static",clock);
        assertEquals("static",tokens.token());assertFalse(tokens.renewable());server.verify();
    }
}
