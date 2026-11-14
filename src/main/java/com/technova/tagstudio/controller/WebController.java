package com.technova.tagstudio.controller;

import com.technova.tagstudio.model.TagCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    @GetMapping("/")
    public String index(Model model) {
        logger.info("Serving TechNova Tag Studio home page");
        model.addAttribute("appName", "TechNova Tag Studio");
        model.addAttribute("defaultChannel", "TechNova IQ");
        model.addAttribute("defaultTopic", "SQL Server Performance Tuning");
        model.addAttribute("categories", Arrays.asList(TagCategory.values()));
        return "index";
    }
}
