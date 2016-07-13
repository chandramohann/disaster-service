package de.extremeenvironment.disasterservice.web.rest;

import de.extremeenvironment.disasterservice.DisasterServiceApp;
import de.extremeenvironment.disasterservice.client.MessageClient;
import de.extremeenvironment.disasterservice.client.UserService;
import de.extremeenvironment.disasterservice.domain.Action;
import de.extremeenvironment.disasterservice.domain.ActionObject;
import de.extremeenvironment.disasterservice.domain.Disaster;
import de.extremeenvironment.disasterservice.domain.User;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import de.extremeenvironment.disasterservice.repository.ActionObjectRepository;
import de.extremeenvironment.disasterservice.repository.ActionRepository;
import de.extremeenvironment.disasterservice.repository.DisasterRepository;
import de.extremeenvironment.disasterservice.repository.UserRepository;
import de.extremeenvironment.disasterservice.service.DisasterService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import util.WithMockOAuth2Authentication;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the Matching algorithm.
 *
 * @see de.extremeenvironment.disasterservice.service.ActionService
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DisasterServiceApp.class)
@WebIntegrationTest({
    "spring.profiles.active:test",
    "server.port:0"
})
public class MatchingIntTest {

    private static User user;

    @Inject
    private WebApplicationContext context;

    @Inject
    private ActionRepository actionRepository;

    @Inject
    private ActionObjectRepository actionObjectRepository;

    @Inject
    private MessageClient messageClient;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private UserRepository userRepository;

    @Inject
    private DisasterRepository disasterRepository;

    @Inject
    private DisasterService disasterService;

    @Inject
    private UserService userService;



    private MockMvc restActionMockMvc;

    private ActionObject actObj1, actObj2;

    private Disaster disaster;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);


        this.restActionMockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Before
    public void initTest() {
        Disaster d1 = new Disaster();
        d1.setLat(23F);
        d1.setLon(23F);
        disasterRepository.saveAndFlush(d1);
        Disaster d2 = new Disaster();
        d2.setLat(24F);
        d2.setLon(24F);
        disasterRepository.saveAndFlush(d2);


        ActionObject actObj1 = new ActionObject();
        actObj1.setName("AAAA");
        actionObjectRepository.saveAndFlush(actObj1);

        ActionObject actObj2 = new ActionObject();
        actObj2.setName("BBBB");
        actionObjectRepository.saveAndFlush(actObj2);

        user = new User();
        userRepository.saveAndFlush(user);


    }


    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void correctMatch() throws Exception {
        User u1 = new User();
        u1.setUserId(1L);

        userRepository.saveAndFlush(u1);

        List<User> allUsers = userRepository.findAll();


        Action action1Seek = new Action();
        action1Seek.setLat(1F);
        action1Seek.setLon(1F);
        action1Seek.setIsExpired(false);
        action1Seek.setActionType(ActionType.SEEK);
        List<Disaster> disasters = disasterRepository.findAll();
        action1Seek.setDisaster(disasters.get(disasters.size() - 1));
        action1Seek.setUser(allUsers.get(0));
        List<ActionObject> actionObjects = actionObjectRepository.findAll();
        action1Seek.addActionObject(actionObjects.get(actionObjects.size() - 1));

        actionRepository.saveAndFlush(action1Seek);


        Action action2Offer = new Action();
        action2Offer.setLat(1.005F);
        action2Offer.setLon(1.005F);
        action2Offer.setIsExpired(false);
        action2Offer.setActionType(ActionType.OFFER);
        action2Offer.addActionObject(actionObjects.get(actionObjects.size() - 1));


        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action2Offer)))
            .andExpect(status().isCreated());



        List<Action> results = actionRepository.findAll();

        assertTrue(results.get(results.size() - 2).getMatch().equals(results.get(results.size() - 1)));
        assertTrue(results.get(results.size() - 1).getMatch().equals(results.get(results.size() - 2)));


    }


    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void actionsDifferentActionObjectTypes() throws Exception {
        User u1 = new User();
        u1.setUserId(1L);
        User u2 = new User();
        u2.setUserId(2L);
        User u3 = new User();
        u3.setUserId(3L);

        userRepository.saveAndFlush(u1);
        userRepository.saveAndFlush(u2);
        userRepository.saveAndFlush(u3);

        List<User> allUsers = userRepository.findAll();


        Action action1Seek = new Action();
        action1Seek.setLat(1F);
        action1Seek.setLon(1F);
        action1Seek.setIsExpired(false);
        action1Seek.setActionType(ActionType.SEEK);
        List<Disaster> disasters = disasterRepository.findAll();
        action1Seek.setDisaster(disasters.get(disasters.size() - 1));
        List<ActionObject> actionObjects = actionObjectRepository.findAll();
        action1Seek.addActionObject(actionObjects.get(actionObjects.size() - 2));
        action1Seek.setUser(allUsers.get(0));


        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action1Seek)));


        Action action2Offer = new Action();
        action2Offer.setLat(1.005F);
        action2Offer.setLon(1.005F);
        action2Offer.setIsExpired(false);
        action2Offer.setActionType(ActionType.OFFER);
        action2Offer.setDisaster(disasters.get(disasters.size() - 1));
        action2Offer.addActionObject(actionObjects.get(actionObjects.size() - 1));
        action2Offer.setUser(allUsers.get(1));


        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action2Offer)));


        List<Action> results = actionRepository.findAll();

        assertTrue(results.get(results.size() - 2).getMatch() == null);
        assertTrue(results.get(results.size() - 1).getMatch() == null);

    }


    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void actionsTooMuchDistance() throws Exception {
        User u1 = new User();
        u1.setUserId(1L);
        User u2 = new User();
        u2.setUserId(2L);
        User u3 = new User();
        u3.setUserId(3L);

        userRepository.saveAndFlush(u1);
        userRepository.saveAndFlush(u2);
        userRepository.saveAndFlush(u3);

        List<User> allUsers = userRepository.findAll();


        Action action1Seek = new Action();
        action1Seek.setLat(0F);
        action1Seek.setLon(0F);
        action1Seek.setIsExpired(false);
        action1Seek.setActionType(ActionType.SEEK);
        List<Disaster> disasters = disasterRepository.findAll();
        action1Seek.setDisaster(disasters.get(disasters.size() - 1));
        List<ActionObject> actionObjects = actionObjectRepository.findAll();
        action1Seek.addActionObject(actionObjects.get(actionObjects.size() - 1));
        action1Seek.setUser(allUsers.get(0));


        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action1Seek)));


        Action action2Offer = new Action();
        action2Offer.setLat(0F);
        action2Offer.setLon(1F);
        action2Offer.setIsExpired(false);
        action2Offer.setActionType(ActionType.OFFER);
        action2Offer.setDisaster(disasters.get(disasters.size() - 1));
        action2Offer.addActionObject(actionObjects.get(actionObjects.size() - 1));
        action2Offer.setUser(allUsers.get(1));


        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action2Offer)));


        List<Action> results = actionRepository.findAll();

        assertTrue(results.get(results.size() - 2).getMatch() == null);
        assertTrue(results.get(results.size() - 1).getMatch() == null);


    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void matchAlreadySet() throws Exception {
        User u1 = new User();
        u1.setUserId(1L);
        User u2 = new User();
        u2.setUserId(2L);
        User u3 = new User();
        u3.setUserId(3L);

        userRepository.saveAndFlush(u1);
        userRepository.saveAndFlush(u2);
        userRepository.saveAndFlush(u3);

        List<User> allUsers = userRepository.findAll();


        Action action1Seek = new Action();
        action1Seek.setLat(1F);
        action1Seek.setLon(1F);
        action1Seek.setIsExpired(false);
        action1Seek.setActionType(ActionType.SEEK);
        List<Disaster> disasters = disasterRepository.findAll();
        action1Seek.setDisaster(disasters.get(disasters.size() - 1));
        List<ActionObject> actionObjects = actionObjectRepository.findAll();
        action1Seek.addActionObject(actionObjects.get(actionObjects.size() - 1));
        action1Seek.setUser(allUsers.get(0));


        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action1Seek)));


        Action action2Offer = new Action();
        action2Offer.setLat(1.005F);
        action2Offer.setLon(1.005F);
        action2Offer.setIsExpired(false);
        action2Offer.setActionType(ActionType.OFFER);
        action2Offer.setDisaster(disasters.get(disasters.size() - 1));
        action2Offer.addActionObject(actionObjects.get(actionObjects.size() - 1));
        action2Offer.setUser(allUsers.get(1));


        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action2Offer)));


        Action action3Seek = new Action();
        action3Seek.setLat(1.005F);
        action3Seek.setLon(1.005F);
        action3Seek.setIsExpired(false);
        action3Seek.setActionType(ActionType.OFFER);
        action3Seek.setDisaster(disasters.get(disasters.size() - 1));
        action3Seek.addActionObject(actionObjects.get(actionObjects.size() - 1));
        action3Seek.setUser(allUsers.get(2));


        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action3Seek)));


        List<Action> results = actionRepository.findAll();

        assertTrue(results.get(results.size() - 3).getMatch().equals(results.get(results.size() - 2)));
        assertTrue(results.get(results.size() - 2).getMatch().equals(results.get(results.size() - 3)));
        assertTrue(results.get(results.size() - 1).getMatch() == null);

    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void testRejectingMatch() throws Exception {
        User u1 = new User();
        u1.setUserId(1L);
        User u2 = new User();
        u2.setUserId(2L);
//        User u3 = new User();
//        u3.setUserId(3L);

        userRepository.saveAndFlush(u1);
        userRepository.saveAndFlush(u2);
//        userRepository.saveAndFlush(u3);


        List<User> allUsers = userRepository.findAll();
//        System.out.println(allUsers);


        List<ActionObject> actionObjects = actionObjectRepository.findAll();

        Action action1Seek = new Action();
        action1Seek.setIsExpired(false);
        action1Seek.setLat(2F);
        action1Seek.setLon(2F);
        action1Seek.setIsExpired(false);
        action1Seek.setActionType(ActionType.SEEK);
        List<Disaster> disasters = disasterRepository.findAll();
        action1Seek.setDisaster(disasters.get(disasters.size() - 1));
        action1Seek.addActionObject(actionObjects.get(actionObjects.size() - 1));
        action1Seek.setUser(allUsers.get(0));

        actionRepository.saveAndFlush(action1Seek);


        Action action2Offer = new Action();
        action2Offer.setIsExpired(false);
        action2Offer.setLat(2.005F);
        action2Offer.setLon(2.005F);
        action2Offer.setIsExpired(false);
        action2Offer.setActionType(ActionType.OFFER);
        action2Offer.addActionObject(actionObjects.get(actionObjects.size() - 1));
        action2Offer.setUser(allUsers.get(1));

        actionRepository.saveAndFlush(action2Offer);


        Action action3Seek = new Action();
        action3Seek.setIsExpired(false);
        action3Seek.setLat(2.005F);
        action3Seek.setLon(2.005F);
        action3Seek.setIsExpired(false);
        action3Seek.setActionType(ActionType.SEEK);
        action3Seek.setDisaster(disasters.get(disasters.size() - 1));
        action3Seek.addActionObject(actionObjects.get(actionObjects.size() - 1));

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action3Seek)))
            .andExpect(status().isCreated());

//        actionRepository.saveAndFlush(action3Seek);


        List<Action> allActions = actionRepository.findAll();
        allActions.get(allActions.size() - 3).setMatch(allActions.get(allActions.size() - 2));
        allActions.get(allActions.size() - 2).setMatch(allActions.get(allActions.size() - 3));
        actionRepository.saveAndFlush(allActions.get(allActions.size() - 2));
        actionRepository.saveAndFlush(allActions.get(allActions.size() - 1));


        List<Action> results = actionRepository.findAll();
        results.forEach(r -> System.out.println(r + " : " + r.getUser() + " : " + r.getMatch()));
        System.out.println();


//        assertTrue(results.get(results.size() - 3).getMatch().equals(results.get(results.size() - 2)));
//        assertTrue(results.get(results.size() - 2).getMatch().equals(results.get(results.size() - 3)));
//        assertTrue(results.get(results.size() - 1).getMatch() == null);


        restActionMockMvc.perform(put("/api/actions/{id}/rejectMatch", results.get(results.size() - 2).getId())
            .contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk());

        results = actionRepository.findAll();
        results.forEach(r -> System.out.println(r + " : " + r.getMatch()));
        System.out.println();

        assertTrue(results.get(results.size() - 3).getMatch() == null);
        assertTrue(results.get(results.size() - 2).getMatch().equals(results.get(results.size() - 1)));
        assertTrue(results.get(results.size() - 1).getMatch().equals(results.get(results.size() - 2)));

        assertTrue(results.get(results.size() - 3).getRejectedMatches().contains(results.get(results.size() - 2)));
        assertTrue(results.get(results.size() - 2).getRejectedMatches().contains(results.get(results.size() - 3)));


    }
}
