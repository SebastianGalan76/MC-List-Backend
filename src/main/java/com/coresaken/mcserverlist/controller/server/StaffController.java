package com.coresaken.mcserverlist.controller.server;

import com.coresaken.mcserverlist.data.response.ObjectResponse;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.server.staff.Rank;
import com.coresaken.mcserverlist.service.server.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StaffController {
    final StaffService staffService;

    @PostMapping("/server/{id}/manage/staff/all")
    public ResponseEntity<Response> saveAllStaff(@PathVariable("id") Long serverId, @RequestBody List<Rank> staff){
        return staffService.saveAllStaff(serverId, staff);
    }

    @PostMapping("/server/{id}/manage/staff")
    public ResponseEntity<ObjectResponse<Rank>> createRank(@PathVariable("id") Long serverId, @RequestBody Rank rank){
        return staffService.createRank(serverId, rank);
    }

    @PutMapping("/server/{id}/manage/staff")
    public ResponseEntity<Response> editRank(@PathVariable("id") Long serverId, @RequestBody Rank rank){
        return staffService.editRank(serverId, rank);
    }

    @PostMapping("/server/{id}/manage/staff/delete")
    public ResponseEntity<Response> deleteRank(@PathVariable("id") Long serverId, @RequestBody Rank rank){
        return staffService.deleteRank(serverId, rank);
    }
}
