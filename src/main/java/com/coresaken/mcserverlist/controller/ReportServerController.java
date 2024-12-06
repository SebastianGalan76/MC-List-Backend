package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.service.server.ReportServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReportServerController {
    final ReportServerService reportServerService;

    @PostMapping("/server/{id}/report")
    public ResponseEntity<Response> reportServer(@PathVariable("id") Long id, @RequestBody String reason){
        return reportServerService.reportServer(id, reason);
    }
}
