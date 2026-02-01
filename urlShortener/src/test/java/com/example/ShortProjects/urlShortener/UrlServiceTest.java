package com.example.ShortProjects.urlShortener;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class UrlServiceTest {

    @Test
    void testSetSomeUrl() throws Exception {
        UrlService urlService=new UrlService();
        String response= urlService.setUrl("http://google.com");
        assertEquals("http://google.com", urlService.getUrl(response));
    }

    @Test
    void testSettingInvalidUrl(){
        UrlService urlService=new UrlService();
        assertThrows(Exception.class,()->{
            urlService.setUrl("    ");
        });
    }

    @Test
    void testGettingNotEnteredUrl(){
        UrlService urlService=new UrlService();
        assertEquals("NOT_FOUND",urlService.getUrl("http://totally-invalid-url.com/"));
    }
}