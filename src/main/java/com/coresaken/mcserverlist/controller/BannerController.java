package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.response.ObjectResponse;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.Banner;
import com.coresaken.mcserverlist.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class BannerController {
    final BannerService bannerService;

    @PostMapping("/banner")
    public ResponseEntity<ObjectResponse<Banner>> createBanner(@RequestParam("file") MultipartFile file, @RequestParam("url") String link, @RequestParam("size") String size){
        return bannerService.createBanner(file, link, size);
    }

    @PutMapping("/banner/{id}")
    public ResponseEntity<ObjectResponse<Banner>> editBanner(@PathVariable("id") Long id, @RequestParam(value = "file", required = false) MultipartFile file, @RequestParam(value = "url", required = false) String link){
        return bannerService.editBanner(id, file, link);
    }

    @DeleteMapping("/banner/{id}")
    public ResponseEntity<Response> deleteBanner(@PathVariable("id") Long id){
        return bannerService.deleteBanner(id);
    }

    @PostMapping("/banner/{id}/accept")
    public ResponseEntity<Response> acceptBanner(@PathVariable("id") Long id){
        return bannerService.acceptBanner(id);
    }

    @PostMapping("/banner/{id}/reject")
    public ResponseEntity<Response> rejectBanner(@PathVariable("id") Long id, @Param("reason") String reason){
        return bannerService.rejectBanner(id, reason);
    }
}
