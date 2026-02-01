package com.example.ShortProjects.urlShortener;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.MalformedInputException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Service
public class UrlService {
    private final ConcurrentHashMap<String,String> urlMapper=
            new ConcurrentHashMap<String,String>();

    boolean urlExists(String url){
        return urlMapper.containsKey(url);
    }
    String setUrl(String url) throws MalformedURLException, URISyntaxException {
        new URL(url).toURI();
        if(urlMapper.contains(url)) return urlMapper.get(url);
        else{
            String key= generate();
            urlMapper.put(key,url);
            System.out.println(key+" "+urlMapper.get(key));
            return key;
        }
    }
    String getUrl(String url) {
        if(urlMapper.containsKey(url)) return urlMapper.get(url);
        return "NOT_FOUND";
    }

    String generate(){
        String randomUUID= UUID.randomUUID().toString();
        while(urlMapper.containsKey(randomUUID)) randomUUID= UUID.randomUUID().toString();
        return randomUUID;
    }
}
