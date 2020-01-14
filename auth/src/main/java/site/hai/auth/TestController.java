package site.hai.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/index")
    public Map<String,Object> index(){
        Map<String,Object> map = new HashMap<>();
        map.put("key","kk");
        return map;
    }
}
