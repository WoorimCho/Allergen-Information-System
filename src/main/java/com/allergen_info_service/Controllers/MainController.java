package com.allergen_info_service.Controllers;

import com.allergen_info_service.Services.GoodNightRestClientImpl;
import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

// Landing page + the GoodNightWorld tester routes. Catalogue CRUD moved to the
// catalogue services at the Phase 3 cutover; recipe composition is under /bff/**.
@Controller
public class MainController {
    @Autowired
    GoodNightRestClientImpl night;

    @ResponseBody
    @PostMapping("/response")
    public ResponseEntity<JsonNode> postController(@RequestBody JsonNode rawJson){

        if (rawJson.has("name")) {
            String snackName = rawJson.get("name").asText();
            System.out.println("Snack's name: " + snackName);
        }
        return ResponseEntity.status(200).body(rawJson);
    }

    @GetMapping("/request")
    public ResponseEntity getter(@RequestBody JsonNode rawJson){
        String response = night.getSnack(rawJson);

        return ResponseEntity.ok(response);
    }

    @GetMapping({"/", "/home"})
    public String home(){
        return "home";
    }
}
