package com.ludus.api.igdb;
import org.junit.jupiter.api.*;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import java.net.SocketTimeoutException;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
class IgdbClientTest {
    MockRestServiceServer server;IgdbClient client;TwitchTokenProvider tokens;TwitchTokenProviderTest.MutableClock clock;
    @BeforeEach void setup() {
        var builder=RestClient.builder().baseUrl("https://api.igdb.com/v4");
        server=MockRestServiceServer.bindTo(builder).build();tokens=mock(TwitchTokenProvider.class);
        when(tokens.token()).thenReturn("test-token");when(tokens.clientId()).thenReturn("test-client");
        clock=new TwitchTokenProviderTest.MutableClock();client=new IgdbClient(builder.build(),tokens,clock);
    }
    @Test void requestHeadersUnicodeAndCache() {
        server.expect(requestTo("https://api.igdb.com/v4/games")).andExpect(method(HttpMethod.POST))
            .andExpect(header("Client-ID","test-client")).andExpect(header("Authorization","Bearer test-token"))
            .andExpect(content().string("search \"龍\";"))
            .andRespond(withSuccess("[{\"id\":1,\"name\":\"龍\",\"unknown\":1}]",MediaType.APPLICATION_JSON));
        assertEquals("龍",client.games("search \"龍\";")[0].name());
        assertEquals(1,client.games("search \"龍\";").length);server.verify();
    }
    @Test void providerErrorsAreSanitized() {
        for(var status:new HttpStatus[]{HttpStatus.BAD_REQUEST,HttpStatus.UNAUTHORIZED,HttpStatus.FORBIDDEN,HttpStatus.INTERNAL_SERVER_ERROR}) {
            setup();server.expect(anything()).andRespond(withStatus(status).body("sensitive"));
            var error=assertThrows(ResponseStatusException.class,()->client.games("query"));
            assertEquals(502,error.getStatusCode().value());assertFalse(error.toString().contains("sensitive"));server.verify();
        }
    }
    @Test void retriesOnceOn401WithRenewedToken() {
        when(tokens.renewable()).thenReturn(true);when(tokens.token()).thenReturn("old","new");
        server.expect(anything()).andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        server.expect(anything()).andExpect(header("Authorization","Bearer new")).andRespond(withSuccess("[]",MediaType.APPLICATION_JSON));
        assertEquals(0,client.games("query").length);verify(tokens).invalidate("old");server.verify();
    }
    @Test void repeated401DoesNotLoop() {
        when(tokens.renewable()).thenReturn(true);
        server.expect(anything()).andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        server.expect(anything()).andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        assertEquals(502,assertThrows(ResponseStatusException.class,()->client.games("query")).getStatusCode().value());
        verify(tokens,times(1)).invalidate(anyString());server.verify();
    }
    @Test void quotaAndRetryAfterDoNotBlockThreads() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After","5"));
        assertEquals(503,assertThrows(ResponseStatusException.class,()->client.games("first")).getStatusCode().value());
        clock.millis=2000;
        assertEquals(503,assertThrows(ResponseStatusException.class,()->client.games("second")).getStatusCode().value());server.verify();
    }
    @Test void localLimitFourPerSecond() {
        for(int i=0;i<4;i++)server.expect(anything()).andRespond(withSuccess("[]",MediaType.APPLICATION_JSON));
        for(int i=0;i<4;i++)client.games("query"+i);
        assertEquals(503,assertThrows(ResponseStatusException.class,()->client.games("fifth")).getStatusCode().value());server.verify();
    }
    @Test void timeoutIs504AndMalformedBody502() {
        server.expect(anything()).andRespond(withException(new SocketTimeoutException()));
        assertEquals(504,assertThrows(ResponseStatusException.class,()->client.games("a")).getStatusCode().value());server.verify();
        setup();server.expect(anything()).andRespond(withSuccess("not-json",MediaType.APPLICATION_JSON));
        assertEquals(502,assertThrows(ResponseStatusException.class,()->client.games("a")).getStatusCode().value());server.verify();
    }
    @Test void cacheExpiresAndLookupsAreTyped() {
        server.expect(anything()).andRespond(withSuccess("[]",MediaType.APPLICATION_JSON));
        server.expect(anything()).andRespond(withSuccess("[]",MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.igdb.com/v4/genres"))
            .andRespond(withSuccess("[{\"id\":12,\"name\":\"RPG\"}]",MediaType.APPLICATION_JSON));
        client.games("same");clock.millis=61000;client.games("same");
        assertEquals(12L,client.lookup("/genres","fields id,name;")[0].id());server.verify();
    }
}
