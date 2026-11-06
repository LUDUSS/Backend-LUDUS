package com.ludus.api;

import com.ludus.api.models.*;
import com.ludus.api.repositories.*;
import com.ludus.api.security.JwtFilter;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties="spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
class LikesIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository users;
    @Autowired JuegoRepository games;
    @Autowired PublicacionRepository reviews;
    @Autowired InteraccionRepository likes;
    @Autowired ComentarioRepository comments;
    @Autowired SeguidorRepository followers;
    @Autowired EntityManagerFactory emf;
    Usuario author,b,c;
    Juego game;
    Publicacion review;
    String tokenAuthor,tokenB,tokenC;
    @BeforeEach void setup() {
        comments.deleteAll();likes.deleteAll();followers.deleteAll();reviews.deleteAll();games.deleteAll();users.deleteAll();
        author=users.save(new Usuario(null,"Autor","author@fixture.test","fixture-hash","111","",LocalDateTime.now()));
        b=users.save(new Usuario(null,"B","b@fixture.test","fixture-hash","222","",LocalDateTime.now()));
        c=users.save(new Usuario(null,"C","c@fixture.test","fixture-hash","333","",LocalDateTime.now()));
        game=games.save(new Juego(null,119133,"Juego","RPG","PC","Descripcion",""));
        review=reviews.save(new Publicacion(null,author,game,4.0,"Resena",LocalDateTime.now()));
        tokenAuthor=token(author);tokenB=token(b);tokenC=token(c);
    }
    private String token(Usuario user) {
        return "Bearer "+Jwts.builder().subject(user.getCorreo()).expiration(new Date(System.currentTimeMillis()+60000))
            .signWith(JwtFilter.getSignInKey()).compact();
    }
    private String path() {return "/ludus/interaccion/resenia/"+review.getId_resenia();}
    private void like(String token) throws Exception {
        mvc.perform(post(path()).header("Authorization",token)).andExpect(status().isCreated());
    }
    @Test void likeUnlikePersistAndCountersBelongToViewerAndProfile() throws Exception {
        mvc.perform(get(path()).header("Authorization",tokenB)).andExpect(status().isOk())
            .andExpect(jsonPath("$.totalLikes").value(0)).andExpect(jsonPath("$.likedByMe").value(false));
        LocalDateTime before=LocalDateTime.now().minusSeconds(1);
        mvc.perform(post(path()).header("Authorization",tokenB)).andExpect(status().isCreated())
            .andExpect(jsonPath("$.totalLikes").value(1)).andExpect(jsonPath("$.likedByMe").value(true))
            .andExpect(jsonPath("$.idInteraccion").isNumber());
        var saved=likes.findByPublicacionId(review.getId_resenia()).getFirst();
        assertEquals(b.getId_usuario(),saved.getUsuario().getId_usuario());
        assertFalse(saved.getFecha_like().isBefore(before));
        mvc.perform(get(path()).header("Authorization",tokenAuthor)).andExpect(jsonPath("$.totalLikes").value(1))
            .andExpect(jsonPath("$.likedByMe").value(false)).andExpect(jsonPath("$.idInteraccion").doesNotExist());
        mvc.perform(get("/ludus/perfil/"+author.getId_usuario()).header("Authorization",tokenC))
            .andExpect(jsonPath("$.totalLikes").value(1));
        mvc.perform(delete(path()).header("Authorization",tokenB)).andExpect(status().isNoContent());
        mvc.perform(delete(path()).header("Authorization",tokenB)).andExpect(status().isNoContent());
        assertEquals(0,likes.count());
        mvc.perform(get(path()).header("Authorization",tokenB)).andExpect(jsonPath("$.totalLikes").value(0))
            .andExpect(jsonPath("$.likedByMe").value(false));
        mvc.perform(get("/ludus/perfil/"+author.getId_usuario()).header("Authorization",tokenC))
            .andExpect(jsonPath("$.totalLikes").value(0));
    }
    @Test void legacyIdentityDateAndIdCannotBeForgedAndBothRoutesRejectDuplicates() throws Exception {
        mvc.perform(post("/ludus/interaccion/").header("Authorization",tokenB).contentType("application/json")
            .content("{\"id_interaccion\":999999,\"usuario\":{\"id_usuario\":"+author.getId_usuario()+"},\"fecha_like\":\"1900-01-01T00:00:00\",\"publicacion\":{\"id_resenia\":"+review.getId_resenia()+"}}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.usuario.id_usuario").value(b.getId_usuario()));
        var saved=likes.findAll().getFirst();
        assertNotEquals(999999,saved.getId_interaccion());assertTrue(saved.getFecha_like().getYear()>1900);
        mvc.perform(post(path()).header("Authorization",tokenB)).andExpect(status().isConflict());
        mvc.perform(post("/ludus/interaccion/").header("Authorization",tokenB).contentType("application/json")
            .content("{\"publicacion\":{\"id_resenia\":"+review.getId_resenia()+"}}")).andExpect(status().isConflict());
        assertEquals(1,likes.count());assertEquals(saved.getId_interaccion(),likes.findAll().getFirst().getId_interaccion());
    }
    @Test void distinctUsersAndOwnReviewLikesAreAllowed() throws Exception {
        like(tokenAuthor);like(tokenB);
        for(String token:List.of(tokenAuthor,tokenB))
            mvc.perform(get(path()).header("Authorization",token)).andExpect(jsonPath("$.totalLikes").value(2))
                .andExpect(jsonPath("$.likedByMe").value(true));
        mvc.perform(get(path()).header("Authorization",tokenC)).andExpect(jsonPath("$.totalLikes").value(2))
            .andExpect(jsonPath("$.likedByMe").value(false));
        mvc.perform(get("/ludus/perfil/me").header("Authorization",tokenAuthor)).andExpect(jsonPath("$.totalLikes").value(2));
        assertEquals(2,likes.count());
    }
    @Test void foreignLikeCannotBeDeletedThroughEitherRoute() throws Exception {
        like(tokenB);
        int id=likes.findAll().getFirst().getId_interaccion();
        mvc.perform(delete("/ludus/interaccion/"+id).header("Authorization",tokenAuthor)).andExpect(status().isForbidden());
        mvc.perform(delete(path()).header("Authorization",tokenAuthor)).andExpect(status().isNoContent());
        assertEquals(1,likes.count());
        mvc.perform(delete("/ludus/interaccion/"+id).header("Authorization",tokenB)).andExpect(status().isNoContent());
        assertEquals(0,likes.count());
    }
    @Test void simultaneousCreatesHaveOneWinnerAndUnlikeIsIdempotent() throws Exception {
        var start=new CountDownLatch(1);var codes=new ArrayList<Integer>();
        try(var pool=Executors.newFixedThreadPool(6)) {
            var jobs=new ArrayList<Future<Integer>>();
            for(int i=0;i<6;i++)jobs.add(pool.submit(()->{
                start.await();
                return mvc.perform(post(path()).header("Authorization",tokenB)).andReturn().getResponse().getStatus();
            }));
            start.countDown();
            for(var job:jobs)codes.add(job.get(15,TimeUnit.SECONDS));
        }
        assertEquals(1,Collections.frequency(codes,201));assertEquals(5,Collections.frequency(codes,409));assertEquals(1,likes.count());
        try(var pool=Executors.newFixedThreadPool(6)) {
            var jobs=new ArrayList<Future<Integer>>();
            for(int i=0;i<6;i++)jobs.add(pool.submit(()->mvc.perform(delete(path()).header("Authorization",tokenB)).andReturn().getResponse().getStatus()));
            for(var job:jobs)assertEquals(204,job.get(15,TimeUnit.SECONDS));
        }
        assertEquals(0,likes.count());
    }
    @Test void actualDatabaseUniqueRejectsDirectDuplicate() throws Exception {
        like(tokenB);
        var duplicate=new Interaccion();duplicate.setUsuario(b);duplicate.setPublicacion(review);duplicate.setFecha_like(LocalDateTime.now());
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->likes.saveAndFlush(duplicate));
        assertEquals(1,likes.count());
    }
    @Test void deletingReviewRemovesItsLikesAndKeepsUserAndGame() throws Exception {
        like(tokenAuthor);like(tokenB);
        mvc.perform(delete("/ludus/publicacion/"+review.getId_resenia()).header("Authorization",tokenAuthor)).andExpect(status().isNoContent());
        assertEquals(0,likes.count());assertEquals(0,reviews.count());assertEquals(1,games.count());assertEquals(3,users.count());
        mvc.perform(get("/ludus/perfil/me").header("Authorization",tokenAuthor)).andExpect(jsonPath("$.totalLikes").value(0));
        mvc.perform(get(path()).header("Authorization",tokenB)).andExpect(status().isNotFound());
        mvc.perform(post(path()).header("Authorization",tokenB)).andExpect(status().isNotFound());
    }
    @Test void batchReturnsCountAndPersonalStateInTwoSqlQueriesIndependentOfPageSize() throws Exception {
        like(tokenB);
        var ids=new ArrayList<Integer>();ids.add(review.getId_resenia());
        for(int n=0;n<19;n++) {
            var extra=games.save(new Juego(null,200000+n,"Game "+n,"","","",""));
            ids.add(reviews.save(new Publicacion(null,author,extra,4.0,"Review "+n,LocalDateTime.now())).getId_resenia());
        }
        var statistics=emf.unwrap(org.hibernate.SessionFactory.class).getStatistics();
        for(int size:new int[]{1,20}) {
            statistics.clear();
            String query=ids.subList(0,size).stream().map(Object::toString).collect(java.util.stream.Collectors.joining(","));
            mvc.perform(get("/ludus/interaccion/resenias").param("ids",query).header("Authorization",tokenB))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(size))
                .andExpect(jsonPath("$[0].idResenia").value(review.getId_resenia()))
                .andExpect(jsonPath("$[0].totalLikes").value(1)).andExpect(jsonPath("$[0].likedByMe").value(true));
            assertEquals(2,statistics.getPrepareStatementCount(),"Una consulta de usuario y una agregacion para toda la pagina");
        }
        mvc.perform(get("/ludus/interaccion/resenias").param("ids",ids.get(1)+","+ids.getFirst()).header("Authorization",tokenC))
            .andExpect(jsonPath("$[0].totalLikes").value(0)).andExpect(jsonPath("$[0].likedByMe").value(false))
            .andExpect(jsonPath("$[1].totalLikes").value(1)).andExpect(jsonPath("$[1].likedByMe").value(false));
    }
    @Test void validatesIdsUnknownReviewsAndBatchLimits() throws Exception {
        for(String id:List.of("0","-1","abc"))
            mvc.perform(post("/ludus/interaccion/resenia/"+id).header("Authorization",tokenB)).andExpect(status().isBadRequest());
        mvc.perform(post("/ludus/interaccion/").header("Authorization",tokenB).contentType("application/json").content("{}")).andExpect(status().isBadRequest());
        mvc.perform(get("/ludus/interaccion/resenias").header("Authorization",tokenB)).andExpect(status().isBadRequest());
        mvc.perform(get("/ludus/interaccion/resenias").param("ids","").header("Authorization",tokenB)).andExpect(status().isBadRequest());
        mvc.perform(get("/ludus/interaccion/resenias").param("ids",String.join(",",Collections.nCopies(51,"1"))).header("Authorization",tokenB)).andExpect(status().isBadRequest());
        mvc.perform(get("/ludus/interaccion/resenias").param("ids",review.getId_resenia()+",2147483647").header("Authorization",tokenB)).andExpect(status().isNotFound());
        mvc.perform(delete("/ludus/interaccion/resenia/2147483647").header("Authorization",tokenB)).andExpect(status().isNotFound());
        assertEquals(0,likes.count());
    }
    @Test void allLikeOperationsAndCatalogRequireJwt() throws Exception {
        mvc.perform(post(path())).andExpect(status().isUnauthorized());
        mvc.perform(delete(path())).andExpect(status().isUnauthorized());
        mvc.perform(get(path())).andExpect(status().isUnauthorized());
        mvc.perform(get("/ludus/interaccion/resenias").param("ids",review.getId_resenia().toString())).andExpect(status().isUnauthorized());
        mvc.perform(post("/ludus/interaccion/").contentType("application/json").content("{}")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/ludus/interaccion/1")).andExpect(status().isUnauthorized());
        mvc.perform(get("/ludus/igdb/buscar?nombre=Elden")).andExpect(status().isUnauthorized());
        assertEquals(0,likes.count());
    }
}
