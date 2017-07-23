package net.zhome.home.service;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping("/svc")
public class SampleAppService {
    public SampleAppService() {

    }

    @RequestMapping(value = "/hello", method = RequestMethod.GET, produces = "application/json")
    public String getGreeting() {

        return "{'hello': 'there'}";
    }

}
