package de.extremeenvironment.disasterservice.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by over on 30.06.2016.
 */
@FeignClient("http://messageclient/api")
public interface MessageClient {


   @RequestMapping(method = RequestMethod.POST, value = "/conversations")
    public Conversation addConversation(@RequestBody Conversation conversation);


}