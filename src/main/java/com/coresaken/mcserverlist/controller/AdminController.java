package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.dto.ReportServerDto;
import com.coresaken.mcserverlist.database.model.Banner;
import com.coresaken.mcserverlist.service.BannerService;
import com.coresaken.mcserverlist.service.server.ReportServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminController {
    final BannerService bannerService;
    final ReportServerService reportServerService;

    @GetMapping("/admin/banners")
    public List<Banner> getBanners(){
        return bannerService.getBannersByStatus(new Banner.Status[]{Banner.Status.PUBLISHED, Banner.Status.NOT_VERIFIED});
    }

    @GetMapping("/admin/reports")
    public List<ReportServerDto> getReports(){
        return reportServerService.getReports();
    }
}
