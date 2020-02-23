package com.instacar.urlshortener.controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.hash.Hashing;
import com.instacar.urlshortener.dto.ShortenUrl;


@Controller
public class UrlShortenerController {
	
	 @Autowired
	 StringRedisTemplate redisTemplate;
	 
	 @Autowired
	 StringRedisTemplate rateLimiter;
	 
	private Map<String, ShortenUrl> shortenUrlList = new HashMap<>();

	@RequestMapping(value="/", method=RequestMethod.GET)
	public String loadIndex() {
		return "index";
	}
	
	@RequestMapping(value="/shortenurl", method=RequestMethod.POST)
	public ResponseEntity<Object> getShortenUrl(@RequestBody ShortenUrl shortenUrl) throws MalformedURLException, UnknownHostException {
		 UrlValidator urlValidator = new UrlValidator(
	                new String[]{"http", "https"}
	        );
		 String url=shortenUrl.getfullUrl();
		 String id="";
	     if (urlValidator.isValid(url)) {
	    	  id= Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
	         System.out.println("URL Id generated: "+ id);
	         redisTemplate.opsForValue().set(id, url, 5, TimeUnit.MINUTES);
	     }
	     shortenUrl.setshortUrl("http://" + InetAddress.getLocalHost().getHostName() + ":8080/s/"+id);
		 return new ResponseEntity<Object>(shortenUrl, HttpStatus.OK);
	}
	
	@RequestMapping(value="/s/{randomstring}", method=RequestMethod.GET)
	public void getFullUrl(HttpServletRequest request, HttpServletResponse response, @PathVariable("randomstring") String randomString) throws IOException {
		String ipAddress = request.getRemoteAddr();
		Long currentTime= System.currentTimeMillis();
		boolean flag=true;
		String key=ipAddress +":"+currentTime/60000;
		if(rateLimiter.opsForValue().get(key)==null) {
			rateLimiter.opsForValue().set(key, "1", 1, TimeUnit.MINUTES);
			flag=false;
		}
			
		Long count=(long) 1;
		if(flag) {
			count=rateLimiter.opsForValue().increment(key);
		}
		System.out.println(key+"  "+count);
		if(count<=2) {
			String url = redisTemplate.opsForValue().get(randomString);
		    System.out.println("URL Retrieved: " + url);
		    System.out.println(ipAddress);
			response.sendRedirect(url);
		}
	}
	
	@RequestMapping(value="/customUrl", method=RequestMethod.POST)
	public  ResponseEntity<Object> getCustomUrl(@RequestBody ShortenUrl customUrl) throws MalformedURLException, UnknownHostException {
		 UrlValidator urlValidator = new UrlValidator(
	                new String[]{"http", "https"}
	        );
		 String fullUrl=customUrl.getfullUrl();
		 String shortUrl=customUrl.getshortUrl();
	     if (urlValidator.isValid(fullUrl) && redisTemplate.opsForValue().get(shortUrl)==null) {
	         redisTemplate.opsForValue().set(shortUrl, fullUrl, 5, TimeUnit.MINUTES);
	         customUrl.setshortUrl("http://" + InetAddress.getLocalHost().getHostName() + ":8080/s/"+shortUrl);
	     }
	     else {
	    	 customUrl.setshortUrl(""); 
	     }
		 return new ResponseEntity<Object>(customUrl, HttpStatus.OK);
	}

}
