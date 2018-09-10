package ivan.spring.controller;


import akka.actor.ActorRef;
import ivan.spring.data.ChatStateJson;
import ivan.spring.service.ActorsConditions2;
import ivan.spring.service.ActorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ActorController {

    @Autowired
    ActorsService actorsService;

    @GetMapping(value = "/main")
    public String openStatusView() {
        return "main";
    }


    @RequestMapping(value = "/actorStatuses", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public ChatStateJson actorStatuses() {
        return ActorsConditions2.getJson();
    }

    @RequestMapping(value = "/newPerson/{personName}")
    @ResponseBody
    public ChatStateJson addNewPersonToChat(HttpServletRequest request, @PathVariable String personName) {
        ActorRef person = actorsService.addPerson(personName);
        request.getSession().setAttribute("person_"+personName, person);
        return actorStatuses();
    }

    @RequestMapping(value = "/newMessage/{personName}")
    @ResponseBody
    public ChatStateJson newMessage(HttpServletRequest request, @PathVariable String personName, @RequestParam String message) {
        ActorRef person = (ActorRef) request.getSession().getAttribute("person_"+personName);
        actorsService.sendMessageToChat(person, message);
        return actorStatuses();
    }

    @RequestMapping(value = "/logout/{personName}")
    @ResponseBody
    public ChatStateJson removePersonFromChat(HttpServletRequest request, @PathVariable String personName) {
        ActorRef person = (ActorRef) request.getSession().getAttribute("person_"+personName);
        actorsService.removePerson(person);
        return actorStatuses();
    }

}
