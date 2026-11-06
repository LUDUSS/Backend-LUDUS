package com.ludus.api.services;
import com.ludus.api.igdb.*;
import com.ludus.api.repositories.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class IgdbServiceTest {
    IgdbClient client;JuegoRepository games;PublicacionRepository reviews;IgdbService service;
    @BeforeEach void setup() {
        client=mock(IgdbClient.class);games=mock(JuegoRepository.class);reviews=mock(PublicacionRepository.class);
        service=new IgdbService(client,games,reviews);
        when(reviews.average(anyLong())).thenReturn(null);
        when(client.games(anyString())).thenReturn(new IgdbGame[0]);
    }
    @Test void unicodeEscapingFiltersAndPaginationAreReal() {
        var page=service.buscarJuegos("龍 \"title\" \\ test",2,3,"relevancia",12L,6L);
        var query=ArgumentCaptor.forClass(String.class);verify(client).games(query.capture());
        assertTrue(query.getValue().contains("genres = (12) & platforms = (6)"));
        assertTrue(query.getValue().contains("limit 4;offset 6;"));
        assertTrue(query.getValue().contains("\\\"title\\\""));
        assertTrue(query.getValue().contains("\\\\ test"));
        assertTrue(page.items().isEmpty());assertFalse(page.hasNext());
        verifyNoInteractions(games,reviews);
    }
    @Test void validationRejectsInjectionAndBadParameters() {
        for(String name:new String[]{"","a".repeat(101),"test\nquery"}) assertThrows(ResponseStatusException.class,()->service.buscarJuegos(name,0,10,"relevancia",null,null));
        assertThrows(ResponseStatusException.class,()->service.buscarJuegos("ok",0,10,"rating",null,null));
        assertThrows(ResponseStatusException.class,()->service.explorar(-1,10,"rating",null,null));
        assertThrows(ResponseStatusException.class,()->service.explorar(0,51,"rating",null,null));
        assertThrows(ResponseStatusException.class,()->service.explorar(0,10,"x;drop",null,null));
        assertThrows(ResponseStatusException.class,()->service.explorar(0,10,"rating",0L,null));
        verifyNoInteractions(client);
    }
    @Test void mappingPreservesLongSummaryMultipleListsAndMissingImages() {
        var raw=new IgdbGame(1L,"name","a".repeat(2000),
            List.of(new IgdbGame.Named(1L,"RPG"),new IgdbGame.Named(2L,"Action")),
            List.of(new IgdbGame.Named(6L,"PC")),null,null,89.5,"name",null);
        var dto=service.map(raw);
        assertNull(dto.localGameId());assertNull(dto.portadaUrl());assertEquals(2000,dto.descripcion().length());
        assertEquals(2,dto.generos().size());assertTrue(dto.screenshots().isEmpty());assertEquals(89.5,dto.ratingIgdb());
        assertNull(dto.ratingLudus().promedio());assertEquals(0,dto.ratingLudus().cantidad());
        verify(games,never()).save(any());
    }
    @Test void detail404AndSafeImages() {
        assertEquals(404,assertThrows(ResponseStatusException.class,()->service.detalle(22L)).getStatusCode().value());
        assertNull(IgdbService.image(new IgdbGame.Image("../bad"),"cover_big"));
        assertEquals("https://images.igdb.com/igdb/image/upload/t_cover_big/abc.jpg",IgdbService.image(new IgdbGame.Image("abc"),"cover_big"));
    }
    @Test void pageFetchesExtraRowWithoutInventingTotal() {
        var g=new IgdbGame(1L,"name",null,null,null,null,null,null,null,null);
        when(client.games(anyString())).thenReturn(new IgdbGame[]{g,g,g});
        var page=service.explorar(0,2,"nombre",null,null);
        assertEquals(2,page.items().size());assertTrue(page.hasNext());
        verify(games,never()).save(any());
    }
}
