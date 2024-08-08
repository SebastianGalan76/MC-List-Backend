package com.coresaken.mcserverlist.service;

import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.Banner;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.repository.BannerRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@Data
@RequiredArgsConstructor
public class BannerService {
    final UserService userService;
    final BannerRepository bannerRepository;

    public Response createBanner(MultipartFile file, String link, String size) {
        User user = userService.getLoggedUser();
        if(user == null){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Twoja sesja wygasła. Zaloguj się ponownie").build();
        }

        Response uploadResponse = BannerFileService.upload(file);
        if(uploadResponse.getStatus() != HttpStatus.OK){
            return uploadResponse;
        }

        Banner banner = new Banner();
        banner.setBannerSize(Banner.BannerSize.valueOf(size));
        banner.setLink(link);
        banner.setOwner(user);
        banner.changeStatus(Banner.Status.NOT_VERIFIED, null);
        banner.setFilePath(uploadResponse.getMessage());
        bannerRepository.save(banner);

        return Response.builder().status(HttpStatus.OK).message("Banner został przesłany do weryfikacji. Przejdź do zakładki \"Moje Banery\", aby wyświetlić status").build();
    }

    public Response acceptBanner(Long bannerId){
        Banner banner = bannerRepository.findById(bannerId).orElse(null);
        if(banner == null){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Wystąpił błąd #8725. Brak baneru o podanym ID").build();
        }

        User user = userService.getLoggedUser();
        if(user==null || user.getRole() != User.Role.ADMIN){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Nie posiadasz wymaganych uprawnień, aby to zrobić").build();
        }

        if(banner.isPaid()){
            banner.changeStatus(Banner.Status.PUBLISHED, null);
        }
        else{
            banner.changeStatus(Banner.Status.ACCEPTED, null);
        }

        bannerRepository.save(banner);
        return Response.builder().status(HttpStatus.OK).build();
    }

    public Response rejectBanner(Long bannerId, String reason){
        Banner banner = bannerRepository.findById(bannerId).orElse(null);
        if(banner == null){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Wystąpił błąd #8725. Brak baneru o podanym ID").build();
        }

        User user = userService.getLoggedUser();
        if(user==null || user.getRole() != User.Role.ADMIN){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Nie posiadasz wymaganych uprawnień, aby to zrobić").build();
        }

        banner.changeStatus(Banner.Status.REJECTED, reason);
        bannerRepository.save(banner);
        return Response.builder().status(HttpStatus.OK).build();
    }

    public Response publishBanner(Long bannerId){
        Banner banner = bannerRepository.findById(bannerId).orElse(null);
        if(banner == null){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Wystąpił błąd #8725. Brak baneru o podanym ID").build();
        }

        banner.changeStatus(Banner.Status.PUBLISHED, null);

        LocalDateTime now = LocalDateTime.now();
        banner.setPublishedAt(now);
        banner.setFinishedAt(now.plusDays(31));
        banner.setPaid(true);
        bannerRepository.save(banner);
        return Response.builder().status(HttpStatus.OK).build();
    }

    public Response editBanner(Long id, MultipartFile file, String link) {
        Banner banner = bannerRepository.findById(id).orElse(null);
        if(banner == null){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Wystąpił błąd #8725. Brak baneru o podanym ID").build();
        }

        User user = userService.getLoggedUser();
        if(user==null || user.getRole() != User.Role.ADMIN || !banner.getOwner().equals(user)){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Nie posiadasz wymaganych uprawnień, aby to zrobić").build();
        }

        if(file != null){
            Response uploadResponse = BannerFileService.upload(file);
            if(uploadResponse.getStatus() != HttpStatus.OK){
                return uploadResponse;
            }

            BannerFileService.remove(banner.getFilePath());
            banner.setFilePath(uploadResponse.getMessage());
        }
        banner.setLink(link);
        banner.changeStatus(Banner.Status.NOT_VERIFIED, null);
        bannerRepository.save(banner);
        if(banner.isPaid()){
            return Response.builder().status(HttpStatus.OK).message("Zmiany zostały wysłane do weryfikacji. Po weryfikacji baner zostanie automatycznie opublikowany").build();
        }
        return Response.builder().status(HttpStatus.OK).message("Zmiany zostały wysłane do weryfikacji").build();
    }

    public Response deleteBanner(Long id) {
        Banner banner = bannerRepository.findById(id).orElse(null);
        if(banner == null){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Wystąpił błąd #8725. Brak baneru o podanym ID").build();
        }

        User user = userService.getLoggedUser();
        if(user==null || user.getRole() != User.Role.ADMIN || !banner.getOwner().equals(user)){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Nie posiadasz wymaganych uprawnień, aby to zrobić").build();
        }

        BannerFileService.remove(banner.getFilePath());
        bannerRepository.delete(banner);
        return Response.builder().status(HttpStatus.OK).build();
    }
}
