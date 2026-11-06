package com.ludus.api;
import com.ludus.api.models.*;
import com.ludus.api.repositories.*;
import com.ludus.api.igdb.*;
import com.ludus.api.services.GameResolver;
import com.ludus.api.security.JwtFilter;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class ReviewsIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository users;@Autowired JuegoRepository games;@Autowired PublicacionRepository reviews;
    @Autowired ComentarioRepository comments;@Autowired InteraccionRepository likes;
    @Autowired SeguidorRepository followers;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder encoder;
    @Autowired GameResolver resolver;@Autowired PlatformTransactionManager manager;
    @MockitoBean IgdbClient igdb;
    Usuario a,b;String tokenA,tokenB;
    @BeforeEach void setup() {
        comments.deleteAll();likes.deleteAll();followers.deleteAll();reviews.deleteAll();games.deleteAll();users.deleteAll();
        a=users.save(new Usuario(null,"Ana","ana@test",encoder.encode("fixture-password"),"111","bio",LocalDateTime.now()));
        b=users.save(new Usuario(null,"Bob","bob@test",encoder.encode("fixture-password"),"222","bio",LocalDateTime.now()));
        tokenA=jwt(a.getCorreo());tokenB=jwt(b.getCorreo());
        when(igdb.games(anyString())).thenReturn(new IgdbGame[]{new IgdbGame(119133L,"Elden Ring","long ".repeat(400),
            List.of(new IgdbGame.Named(12L,"RPG")),List.of(new IgdbGame.Named(6L,"PC")),null,null,92.0,"elden-ring",null)});
        when(igdb.lookup(anyString(),anyString())).thenReturn(new IgdbGame.Named[]{new IgdbGame.Named(12L,"RPG")});
    }
    private String jwt(String email) {return "Bearer "+Jwts.builder().subject(email).expiration(new Date(System.currentTimeMillis()+60000)).signWith(JwtFilter.getSignInKey()).compact();}
    private int publish() throws Exception {
        String json=mvc.perform(post("/ludus/publicacion/igdb/119133").header("Authorization",tokenA)
            .contentType("application/json").content("{\"texto\":\"Excelente\",\"calificacion\":5,\"idUsuario\":"+b.getId_usuario()+",\"usuario\":{\"id_usuario\":"+b.getId_usuario()+"}}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.autor.idUsuario").value(a.getId_usuario()))
            .andExpect(jsonPath("$.juego.igdbId").value(119133)).andReturn().getResponse().getContentAsString();
        var match=Pattern.compile("\"idResenia\"\\s*:\\s*(\\d+)").matcher(json);assertTrue(match.find());return Integer.parseInt(match.group(1));
    }
    @Test void duplicateAcrossBothCreationRoutesIs409ButOwnEditDeleteAndRepublishWork() throws Exception {
        int id=publish();
        var game=games.findByIgdbId(119133L).orElseThrow();
        mvc.perform(post("/ludus/publicacion/igdb/119133").header("Authorization",tokenA).contentType("application/json")
            .content("{\"texto\":\"Duplicada\",\"calificacion\":1}")).andExpect(status().isConflict());
        mvc.perform(post("/ludus/publicacion/").header("Authorization",tokenA).contentType("application/json")
            .content("{\"juego\":{\"id_juego\":"+game.getId_juego()+"},\"texto_resenia\":\"Duplicada local\",\"calificacion\":2}"))
            .andExpect(status().isConflict());
        assertEquals(1,reviews.count());assertEquals("Excelente",reviews.findById(id).orElseThrow().getTexto_resenia());
        mvc.perform(put("/ludus/publicacion/"+id).header("Authorization",tokenA).contentType("application/json")
            .content("{\"texto\":\"Editada\",\"calificacion\":4}")).andExpect(status().isOk())
            .andExpect(jsonPath("$.idResenia").value(id));
        mvc.perform(delete("/ludus/publicacion/"+id).header("Authorization",tokenA)).andExpect(status().isNoContent());
        assertNotEquals(id,publish());assertEquals(1,reviews.count());assertEquals(1,games.count());
    }
    @Test void differentUsersCanReviewSameGameAndSameUserCanReviewAnotherGame() throws Exception {
        publish();
        mvc.perform(post("/ludus/publicacion/igdb/119133").header("Authorization",tokenB).contentType("application/json")
            .content("{\"texto\":\"Opinion de Bob\",\"calificacion\":3}")).andExpect(status().isCreated())
            .andExpect(jsonPath("$.autor.idUsuario").value(b.getId_usuario()));
        var another=new Juego();another.setId_api(999L);another.setNombre_juego("Otro juego");games.saveAndFlush(another);
        mvc.perform(post("/ludus/publicacion/").header("Authorization",tokenA).contentType("application/json")
            .content("{\"juego\":{\"id_juego\":"+another.getId_juego()+"},\"texto_resenia\":\"Otro juego\",\"calificacion\":4}"))
            .andExpect(status().isCreated());
        assertEquals(3,reviews.count());
        mvc.perform(get("/ludus/publicacion/juego/igdb/119133/rating").header("Authorization",tokenA))
            .andExpect(jsonPath("$.cantidad").value(2)).andExpect(jsonPath("$.promedio").value(4.0));
    }
    @Test void concurrentReviewPostsCreateOneAndReturn409ForTheRest() throws Exception {
        resolver.resolve(119133L);
        var start=new CountDownLatch(1);var codes=new ArrayList<Integer>();
        try(var pool=Executors.newFixedThreadPool(6)) {
            var jobs=new ArrayList<Future<Integer>>();
            for(int i=0;i<6;i++) jobs.add(pool.submit(()->{
                start.await();
                return mvc.perform(post("/ludus/publicacion/igdb/119133").header("Authorization",tokenA)
                    .contentType("application/json").content("{\"texto\":\"Resena concurrente\",\"calificacion\":4}"))
                    .andReturn().getResponse().getStatus();
            }));
            start.countDown();
            for(var job:jobs) codes.add(job.get(15,TimeUnit.SECONDS));
        }
        assertEquals(1,Collections.frequency(codes,201));assertEquals(5,Collections.frequency(codes,409));
        assertEquals(1,reviews.count());assertEquals(1,games.count());
    }
    @Test void databaseUniqueConstraintRejectsDuplicatesWithoutServicePrecheck() throws Exception {
        publish();var game=games.findByIgdbId(119133L).orElseThrow();
        var transaction=new TransactionTemplate(manager);
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->transaction.execute(status->
            reviews.saveAndFlush(new Publicacion(null,a,game,3.0,"Duplicada directa",LocalDateTime.now()))));
        assertEquals(1,reviews.count());
    }
    @Test void sameReviewAppearsInGameAndBothProfileViewsAndUpdatesDeletePropagate() throws Exception {
        int id=publish();assertEquals(1,games.count());assertEquals(1,reviews.count());
        for(String path:new String[]{"/ludus/publicacion/juego/igdb/119133","/ludus/publicacion/usuario/"+a.getId_usuario(),"/ludus/publicacion/me"})
            mvc.perform(get(path).header("Authorization",tokenA)).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].idResenia").value(id)).andExpect(jsonPath("$.items[0].autor.idUsuario").value(a.getId_usuario()))
                .andExpect(jsonPath("$.items[0].autor.correo").doesNotExist()).andExpect(jsonPath("$.items[0].autor.contrasenia").doesNotExist());
        mvc.perform(get("/ludus/publicacion/juego/igdb/119133").header("Authorization",tokenB))
            .andExpect(jsonPath("$.items[0].idResenia").value(id));
        mvc.perform(get("/ludus/perfil/me").header("Authorization",tokenA)).andExpect(jsonPath("$.cantidadResenias").value(1))
            .andExpect(jsonPath("$.juegosResenados").value(1)).andExpect(jsonPath("$.promedioCalificacion").value(5.0));
        mvc.perform(put("/ludus/publicacion/"+id).header("Authorization",tokenB).contentType("application/json").content("{\"texto\":\"hacked\",\"calificacion\":1}")).andExpect(status().isForbidden());
        mvc.perform(delete("/ludus/publicacion/"+id).header("Authorization",tokenB)).andExpect(status().isForbidden());
        mvc.perform(put("/ludus/publicacion/"+id).header("Authorization",tokenA).contentType("application/json")
            .content("{\"texto\":\"Actualizada\",\"calificacion\":4}")).andExpect(status().isOk());
        for(String path:new String[]{"/ludus/publicacion/juego/igdb/119133","/ludus/publicacion/usuario/"+a.getId_usuario()})
            mvc.perform(get(path).header("Authorization",tokenB)).andExpect(jsonPath("$.items[0].texto").value("Actualizada"));
        mvc.perform(get("/ludus/publicacion/juego/igdb/119133/rating").header("Authorization",tokenA))
            .andExpect(jsonPath("$.promedio").value(4.0)).andExpect(jsonPath("$.cantidad").value(1));
        // Comprobar dependientes reales sin cambiar constraints ni usar cascadas JPA.
        var review=reviews.findById(id).orElseThrow();
        comments.save(new Comentario(review,b,"Comentario",LocalDateTime.now()));
        var like=new Interaccion();like.setPublicacion(review);like.setUsuario(b);like.setFecha_like(LocalDateTime.now());likes.save(like);
        mvc.perform(delete("/ludus/publicacion/"+id).header("Authorization",tokenA)).andExpect(status().isNoContent());
        assertEquals(0,comments.count());assertEquals(0,likes.count());
        for(String path:new String[]{"/ludus/publicacion/juego/igdb/119133","/ludus/publicacion/usuario/"+a.getId_usuario()})
            mvc.perform(get(path).header("Authorization",tokenB)).andExpect(jsonPath("$.items").isEmpty());
        mvc.perform(get("/ludus/publicacion/juego/igdb/119133/rating").header("Authorization",tokenA))
            .andExpect(jsonPath("$.promedio").doesNotExist()).andExpect(jsonPath("$.cantidad").value(0));
        mvc.perform(get("/ludus/publicacion/"+id).header("Authorization",tokenA)).andExpect(status().isNotFound());
    }
    @Test void validationEmptyPagesAndLegacyAuthorSpoofing() throws Exception {
        mvc.perform(post("/ludus/publicacion/igdb/119133").header("Authorization",tokenA).contentType("application/json")
            .content("{\"texto\":\"bad\",\"calificacion\":6}")).andExpect(status().isBadRequest());
        assertEquals(0,games.count());assertEquals(0,reviews.count());
        mvc.perform(get("/ludus/publicacion/juego/igdb/9999").header("Authorization",tokenA)).andExpect(jsonPath("$.items").isEmpty());
        assertEquals(0,games.count());verifyNoInteractions(igdb);
        var local=resolver.resolve(119133L);
        mvc.perform(post("/ludus/publicacion/").header("Authorization",tokenA).contentType("application/json")
            .content("{\"id_resenia\":999,\"usuario\":{\"id_usuario\":"+b.getId_usuario()+"},\"juego\":{\"id_juego\":"+local.getId_juego()+"},\"texto_resenia\":\"legacy\",\"calificacion\":3}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.autor.idUsuario").value(a.getId_usuario()));
        assertEquals(1,reviews.count());
        mvc.perform(get("/ludus/publicacion/me?page=1&size=1").header("Authorization",tokenA)).andExpect(jsonPath("$.items").isEmpty());
        mvc.perform(get("/ludus/publicacion/me?size=51").header("Authorization",tokenA)).andExpect(status().isBadRequest());
        mvc.perform(get("/ludus/publicacion/usuario/99999").header("Authorization",tokenA)).andExpect(status().isNotFound());
    }
    @Test void searchDetailAndLookupNeverPersist() throws Exception {
        mvc.perform(get("/ludus/igdb/buscar?nombre=Elden").header("Authorization",tokenA))
            .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].localGameId").doesNotExist());
        mvc.perform(get("/ludus/igdb/juegos/119133").header("Authorization",tokenA)).andExpect(status().isOk());
        mvc.perform(get("/ludus/igdb/generos").header("Authorization",tokenA)).andExpect(status().isOk()).andExpect(jsonPath("$.items[0].id").value(12));
        assertEquals(0,games.count());assertEquals(0,reviews.count());
    }
    @Test void upstreamFailureAndUnknownGameDoNotCreateAnyRows() throws Exception {
        when(igdb.games(anyString())).thenReturn(new IgdbGame[0]);
        mvc.perform(post("/ludus/publicacion/igdb/888").header("Authorization",tokenA).contentType("application/json")
            .content("{\"texto\":\"Resena\",\"calificacion\":4}")).andExpect(status().isNotFound());
        when(igdb.games(anyString())).thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.GATEWAY_TIMEOUT,"IGDB no respondio"));
        mvc.perform(post("/ludus/igdb/juegos/888/resolver").header("Authorization",tokenA)).andExpect(status().isGatewayTimeout());
        assertEquals(0,games.count());assertEquals(0,reviews.count());
    }
    @Test void resolverConcurrentAndRepeatedCreatesExactlyOneLocalGame() throws Exception {
        var start=new CountDownLatch(1);var ids=new HashSet<Integer>();
        try(var pool=Executors.newFixedThreadPool(6)) {
            var jobs=new ArrayList<Future<Integer>>();
            for(int i=0;i<6;i++)jobs.add(pool.submit(()->{start.await();return resolver.resolve(119133L).getId_juego();}));
            start.countDown();
            for(var job:jobs)ids.add(job.get(15,TimeUnit.SECONDS));
        }
        assertEquals(1,ids.size());assertEquals(1,games.count());
        assertTrue(ids.contains(resolver.resolve(119133L).getId_juego()));
        assertTrue(games.findAll().getFirst().getDescripcion().length()>255);
    }
    @Test void profileIsPublicDataOnlyAndOtherUserCannotEdit() throws Exception {
        mvc.perform(get("/ludus/perfil/"+a.getId_usuario()).header("Authorization",tokenB))
            .andExpect(status().isOk()).andExpect(jsonPath("$.idUsuario").value(a.getId_usuario()))
            .andExpect(jsonPath("$.correo").doesNotExist()).andExpect(jsonPath("$.contrasenia").doesNotExist());
        mvc.perform(put("/ludus/usuario/"+a.getId_usuario()).header("Authorization",tokenB)
            .contentType("application/json").content("{\"nombre\":\"hacked\"}")).andExpect(status().isForbidden());
        mvc.perform(put("/ludus/perfil/me").header("Authorization",tokenA).contentType("application/json")
            .content("{\"nombre\":\"Ana editada\",\"sobreMi\":\"Nueva bio\"}")).andExpect(jsonPath("$.nombre").value("Ana editada"));
        assertEquals("Ana editada",users.findById(a.getId_usuario()).orElseThrow().getNombre());
    }
    @Test void foreignKeysAndTransactionRollbackAreReal() {
        var game=resolver.resolve(119133L);
        var transaction=new TransactionTemplate(manager);
        assertThrows(IllegalStateException.class,()->transaction.execute(status-> {
            reviews.saveAndFlush(new Publicacion(null,a,game,4.0,"will roll back",LocalDateTime.now()));
            throw new IllegalStateException("rollback");
        }));
        assertEquals(0,reviews.count());assertEquals(1,games.count());
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->transaction.execute(status-> {
            reviews.saveAndFlush(new Publicacion(null,a,new Juego(999999,1,"x","","","",""),4.0,"bad fk",LocalDateTime.now()));
            return null;
        }));
        assertEquals(0,reviews.count());
    }
    @Test void legacyGameWritesCannotForgeOrDeleteCatalog() throws Exception {
        mvc.perform(post("/ludus/juego/").header("Authorization",tokenA).contentType("application/json")
            .content("{\"id_api\":119133,\"nombre_juego\":\"forged\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nombre_juego").value("Elden Ring"));
        var game=games.findAll().getFirst();
        mvc.perform(delete("/ludus/juego/"+game.getId_juego()).header("Authorization",tokenA)).andExpect(status().isForbidden());
        mvc.perform(put("/ludus/juego/"+game.getId_juego()).header("Authorization",tokenA).contentType("application/json").content("{}")).andExpect(status().isForbidden());
        assertEquals(1,games.count());
    }
    @Test void auxiliaryFeaturesKeepWorkingWithoutAuthorSpoofing() throws Exception {
        int id=publish();
        mvc.perform(post("/ludus/comentario/").header("Authorization",tokenB).contentType("application/json")
            .content("{\"usuario\":{\"id_usuario\":"+a.getId_usuario()+"},\"publicacion\":{\"id_resenia\":"+id+"},\"texto_comentario\":\"Comentario\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.usuario.id_usuario").value(b.getId_usuario()))
            .andExpect(jsonPath("$.usuario.contrasenia").doesNotExist());
        mvc.perform(post("/ludus/interaccion/").header("Authorization",tokenB).contentType("application/json")
            .content("{\"usuario\":{\"id_usuario\":"+a.getId_usuario()+"},\"publicacion\":{\"id_resenia\":"+id+"}}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.usuario.id_usuario").value(b.getId_usuario()));
        int like=likes.findAll().getFirst().getId_interaccion();
        mvc.perform(delete("/ludus/interaccion/"+like).header("Authorization",tokenA)).andExpect(status().isForbidden());
        mvc.perform(delete("/ludus/interaccion/"+like).header("Authorization",tokenB)).andExpect(status().isNoContent());
        mvc.perform(post("/ludus/seguidor/seguir").header("Authorization",tokenA).param("idSeguidor",b.getId_usuario().toString()).param("idSeguido",a.getId_usuario().toString()))
            .andExpect(status().isForbidden());
        mvc.perform(post("/ludus/seguidor/seguir").header("Authorization",tokenB).param("idSeguidor",b.getId_usuario().toString()).param("idSeguido",a.getId_usuario().toString()))
            .andExpect(status().isCreated());
        int follow=followers.findAll().getFirst().getIdSeguimiento();
        mvc.perform(delete("/ludus/seguidor/"+follow).header("Authorization",tokenA)).andExpect(status().isForbidden());
        mvc.perform(delete("/ludus/seguidor/"+follow).header("Authorization",tokenB)).andExpect(status().isNoContent());
    }
    @Test void realLoginAndRegistrationKeepWorkingWithoutPasswordDisclosure() throws Exception {
        mvc.perform(post("/ludus/login").contentType("application/json").content("{\"correo\":\"ana@test\",\"contrasenia\":\"fixture-password\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
        mvc.perform(post("/ludus/login").contentType("application/json").content("{\"correo\":\"ana@test\",\"contrasenia\":\"incorrect\"}"))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/ludus/usuario/").contentType("application/json")
            .content("{\"id_usuario\":"+a.getId_usuario()+",\"nombre\":\"Nueva\",\"correo\":\"new@test\",\"contrasenia\":\"fixture-password\",\"telefono\":\"333\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.contrasenia").doesNotExist());
        assertEquals(3,users.count());assertEquals("Ana",users.findById(a.getId_usuario()).orElseThrow().getNombre());
    }
}
