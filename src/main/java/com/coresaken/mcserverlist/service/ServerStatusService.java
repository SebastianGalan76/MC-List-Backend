package com.coresaken.mcserverlist.service;

import com.coresaken.mcserverlist.data.response.ServerStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ServerStatusService {

    public ServerStatus getServerStatus(String address) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.mcstatus.io/v2/status/java/" + address;
        try{
            return restTemplate.getForObject(url, ServerStatus.class);
        }catch (HttpClientErrorException e){
            return null;
        }
    }

    public ServerStatus getServerStatus(String ip, int port) {
        StringBuilder finalAddress = new StringBuilder();
        finalAddress.append(ip);
        if(port>0){
            finalAddress.append(":").append(port);
        }

        return getServerStatus(finalAddress.toString());
    }
}
