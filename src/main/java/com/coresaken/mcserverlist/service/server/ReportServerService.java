package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.model.server.Report;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.repository.server.ReportRepository;
import com.coresaken.mcserverlist.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportServerService {
    final ServerService serverService;
    final UserService userService;

    final ReportRepository reportRepository;

    public ResponseEntity<Response> reportServer(Long id, String reason) {
        Server server = serverService.getServerById(id);

        if(server==null){
            return Response.badRequest(1, "Serwer o podanym ID nie istnieje");
        }

        User user = userService.getLoggedUser();
        if(user == null){
            return Response.badRequest(2, "Twoja sesja wygasła. Musisz się zalogować ponownie");
        }

        Report report = new Report();
        report.setReason(reason);
        report.setUser(user);
        report.setServer(server);
        reportRepository.save(report);
        return Response.ok("Zgłoszenie zostało wysłane do administracji");
    }
}
