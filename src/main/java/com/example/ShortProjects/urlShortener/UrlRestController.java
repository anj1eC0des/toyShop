package com.example.ShortProjects.urlShortener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
public class UrlRestController {
    @Autowired
    UrlService urlService;

    @GetMapping("/url/{url}")
    public ResponseEntity<Void> redirect(@PathVariable("url") String url){
        System.out.println("Endpoint hit");
        System.out.println(url);
        if(urlService.urlExists(url)) {
            String redirectUrl=urlService.getUrl(url);
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(redirectUrl));
            return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
    }

    @PostMapping("/url")
    public ResponseEntity<String> setUrl(@RequestParam("url") String url) throws MalformedURLException, URISyntaxException {
        String setUrl=urlService.setUrl(url);
        return ResponseEntity.ok().body(setUrl);
    }
}
