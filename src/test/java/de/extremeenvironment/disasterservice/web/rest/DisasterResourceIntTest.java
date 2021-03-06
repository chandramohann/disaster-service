package de.extremeenvironment.disasterservice.web.rest;

import de.extremeenvironment.disasterservice.DisasterServiceApp;
import de.extremeenvironment.disasterservice.domain.Action;
import de.extremeenvironment.disasterservice.domain.Disaster;
import de.extremeenvironment.disasterservice.domain.DisasterType;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import de.extremeenvironment.disasterservice.repository.ActionRepository;
import de.extremeenvironment.disasterservice.repository.DisasterRepository;
import de.extremeenvironment.disasterservice.repository.DisasterTypeRepository;
import de.extremeenvironment.disasterservice.service.DisasterService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import util.WithMockOAuth2Authentication;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the DisasterResource REST controller.
 *
 * @see DisasterResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DisasterServiceApp.class)
@WebIntegrationTest({
    "spring.profiles.active:test",
    "server.port:0"
})
public class DisasterResourceIntTest {


    private static final Boolean DEFAULT_IS_EXPIRED = false;
    private static final Boolean UPDATED_IS_EXPIRED = true;

    private static final Float DEFAULT_LAT = 1F;
    private static final Float UPDATED_LAT = 2F;

    private static final Float DEFAULT_LON = 1F;
    private static final Float UPDATED_LON = 2F;
    private static final String DEFAULT_TITLE = "AAAAA";
    private static final String UPDATED_TITLE = "BBBBB";
    private static final String DEFAULT_DESCRIPTION = "AAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBB";

    @Inject
    private DisasterRepository disasterRepository;

    @Inject
    private DisasterTypeRepository disasterTypeRepository;

    @Inject
    private ActionRepository actionRepository;

    @Inject
    private DisasterService disasterService;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restDisasterMockMvc;

    private Disaster disaster;

    private DisasterType diTy;

    @Inject
    private WebApplicationContext context;


    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);

        this.restDisasterMockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Before
    public void initTest() {
        diTy = new DisasterType();
        diTy.setName("A");
        disasterTypeRepository.saveAndFlush(diTy);
        disaster = new Disaster();
        disaster.setIsExpired(DEFAULT_IS_EXPIRED);
        disaster.setLat(DEFAULT_LAT);
        disaster.setLon(DEFAULT_LON);
        disaster.setTitle(DEFAULT_TITLE);
        disaster.setDescription(DEFAULT_DESCRIPTION);
        disaster.setDisasterType(diTy);
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(username = "admin", scope = "web-app")
    public void createDisaster() throws Exception {
        int databaseSizeBeforeCreate = disasterRepository.findAll().size();

        // Create the Disaster

        restDisasterMockMvc.perform(post("/api/disasters")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(disaster)))
                .andExpect(status().isCreated());

        // Validate the Disaster in the database
        List<Disaster> disasters = disasterRepository.findAll();
        assertThat(disasters).hasSize(databaseSizeBeforeCreate + 1);
        Disaster testDisaster = disasters.get(disasters.size() - 1);
        assertThat(testDisaster.isIsExpired()).isEqualTo(DEFAULT_IS_EXPIRED);
        assertThat(testDisaster.getLat()).isEqualTo(DEFAULT_LAT);
        assertThat(testDisaster.getLon()).isEqualTo(DEFAULT_LON);
        assertThat(testDisaster.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testDisaster.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(username = "admin", scope = "web-app")
    public void getAllDisasters() throws Exception {
        // Initialize the database
        disasterRepository.saveAndFlush(disaster);

        // Get all the disasters
        restDisasterMockMvc.perform(get("/api/disasters?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(disaster.getId().intValue())))
                .andExpect(jsonPath("$.[*].isExpired").value(hasItem(DEFAULT_IS_EXPIRED.booleanValue())))
                .andExpect(jsonPath("$.[*].lat").value(hasItem(DEFAULT_LAT.doubleValue())))
                .andExpect(jsonPath("$.[*].lon").value(hasItem(DEFAULT_LON.doubleValue())))
                .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
                .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())));
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(username = "admin", scope = "web-app")
    public void getDisaster() throws Exception {
        // Initialize the database
        disasterRepository.saveAndFlush(disaster);

        // Get the disaster
        restDisasterMockMvc.perform(get("/api/disasters/{id}", disaster.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(disaster.getId().intValue()))
            .andExpect(jsonPath("$.isExpired").value(DEFAULT_IS_EXPIRED.booleanValue()))
            .andExpect(jsonPath("$.lat").value(DEFAULT_LAT.doubleValue()))
            .andExpect(jsonPath("$.lon").value(DEFAULT_LON.doubleValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()));
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(username = "admin", scope = "web-app")
    public void getNonExistingDisaster() throws Exception {
        // Get the disaster
        restDisasterMockMvc.perform(get("/api/disasters/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(username = "admin", scope = "web-app")
    public void updateDisaster() throws Exception {
        // Initialize the database
        disasterRepository.saveAndFlush(disaster);
        int databaseSizeBeforeUpdate = disasterRepository.findAll().size();

        // Update the disaster
        Disaster updatedDisaster = new Disaster();
        updatedDisaster.setId(disaster.getId());
        updatedDisaster.setIsExpired(UPDATED_IS_EXPIRED);
        updatedDisaster.setLat(UPDATED_LAT);
        updatedDisaster.setLon(UPDATED_LON);
        updatedDisaster.setTitle(UPDATED_TITLE);
        updatedDisaster.setDescription(UPDATED_DESCRIPTION);

        restDisasterMockMvc.perform(put("/api/disasters")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedDisaster)))
                .andExpect(status().isOk());

        // Validate the Disaster in the database
        List<Disaster> disasters = disasterRepository.findAll();
        assertThat(disasters).hasSize(databaseSizeBeforeUpdate);
        Disaster testDisaster = disasters.get(disasters.size() - 1);
        assertThat(testDisaster.isIsExpired()).isEqualTo(UPDATED_IS_EXPIRED);
        assertThat(testDisaster.getLat()).isEqualTo(UPDATED_LAT);
        assertThat(testDisaster.getLon()).isEqualTo(UPDATED_LON);
        assertThat(testDisaster.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testDisaster.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(username = "admin", scope = "web-app")
    public void deleteDisaster() throws Exception {
        // Initialize the database
        disasterRepository.saveAndFlush(disaster);
        int databaseSizeBeforeDelete = disasterRepository.findAll().size();

        // Get the disaster
        restDisasterMockMvc.perform(delete("/api/disasters/{id}", disaster.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Disaster> disasters = disasterRepository.findAll();
        assertThat(disasters).hasSize(databaseSizeBeforeDelete - 1);
    }

    /**
     * Test
     * If a Disaster with the same Type in a 15km radius exist which is not expired, the create method will create a Action which the type knowlege
     * @throws Exception
     */
    @Test
    @Transactional
    @WithMockOAuth2Authentication(username = "user", scope = "web-app")
    public void noDoubleDisaster() throws  Exception {
        disasterRepository.saveAndFlush(disaster);
        int sizeD = disasterRepository.findAll().size();
        int sizeA = actionRepository.findAll().size();

        Disaster di = new Disaster();
        di.setDisasterType(diTy);
        di.setIsExpired(false);

        di.setLat(1F);
        di.setLon(1F);

        restDisasterMockMvc.perform(post("/api/disasters")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(di)));


        assertEquals(sizeD, disasterRepository.findAll().size());
        assertEquals(sizeA, (actionRepository.findAll().size() - 1));
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(username = "admin", scope = "web-app")
    public void getActionsForHeatmap() throws Exception {
        List<Action> actions = actionRepository.findByDisasterId(3L)
            .stream().filter(a -> (a.getActionType() == ActionType.KNOWLEDGE || a.getActionType() == ActionType.SEEK))
            .collect(Collectors.toList());

        restDisasterMockMvc.perform(get("/api/disasters/{id}/heatmap", 3))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(actions.size())));

    }
}

