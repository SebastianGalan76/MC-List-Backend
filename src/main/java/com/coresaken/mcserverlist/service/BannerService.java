package com.coresaken.mcserverlist.service;

import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.Banner;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.repository.BannerRepository;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Data
@RequiredArgsConstructor
public class BannerService {
    final UserService userService;
    final BannerRepository bannerRepository;

    private List<Banner> bigBanners;
    private int bigBannerIndex;

    private List<Banner> smallBanners;
    private int smallBannerIndex;

    @PostConstruct
    public void initialize(){
        bigBanners = bannerRepository.findByStatusAndSize(Banner.Status.PUBLISHED, Banner.Size.BIG);
        smallBanners = bannerRepository.findByStatusAndSize(Banner.Status.PUBLISHED, Banner.Size.SMALL);

        bigBannerIndex = 0;
        smallBannerIndex = 0;
    }

    public Banner getBigBanner(){
        if(bigBanners.isEmpty()){
            return null;
        }
        return bigBanners.get(bigBannerIndex++%bigBanners.size());
    }

    public List<Banner> getSmallBanners(){
        if(smallBanners.isEmpty()){
            return null;
        }

        List<Banner> banners = new ArrayList<>();
        for(int i=0;i<3;i++){
            banners.add(smallBanners.get(smallBannerIndex++%smallBanners.size()));
        }

        return banners;
    }

    public ResponseEntity<Response> createBanner(MultipartFile file, String link, String size) {
        User user = userService.getLoggedUser();
        if(user == null){
            return Response.badRequest(1, "Twoja sesja wygasła. Zaloguj się ponownie");
        }

        ResponseEntity<Response> uploadResponse = BannerFileService.upload(file);
        if(uploadResponse.getStatusCode() != HttpStatus.OK){
            return uploadResponse;
        }

        Banner banner = new Banner();
        banner.setSize(Banner.Size.valueOf(size));
        banner.setLink(link);
        banner.setOwner(user);
        banner.changeStatus(Banner.Status.NOT_VERIFIED, null);
        banner.setFilePath(uploadResponse.getBody().getMessage());
        bannerRepository.save(banner);

        return Response.ok("Banner został przesłany do weryfikacji. Przejdź do zakładki \"Moje Banery\", aby wyświetlić status");
    }

    public ResponseEntity<Response> acceptBanner(Long bannerId){
        Banner banner = bannerRepository.findById(bannerId).orElse(null);
        if(banner == null){
            return Response.badRequest(1, "Wystąpił nieoczekiwany błąd. Brak baneru o podanym ID");
        }

        User user = userService.getLoggedUser();
        if(user==null || user.getRole() != User.Role.ADMIN){
            return Response.badRequest(2, "Nie posiadasz wymaganych uprawnień, aby to zrobić");
        }

        if(banner.isPaid()){
            banner.changeStatus(Banner.Status.PUBLISHED, null);

            if(banner.getSize() == Banner.Size.BIG){
                bigBanners.add(banner);
            }
            else if(banner.getSize() == Banner.Size.SMALL){
                smallBanners.add(banner);
            }
        }
        else{
            banner.changeStatus(Banner.Status.ACCEPTED, null);
        }

        bannerRepository.save(banner);
        return Response.ok("Baner został zaakceptowany");
    }

    public ResponseEntity<Response> rejectBanner(Long bannerId, String reason){
        Banner banner = bannerRepository.findById(bannerId).orElse(null);
        if(banner == null){
            return Response.badRequest(1, "Wystąpił nieoczekiwany błąd. Brak baneru o podanym ID");
        }

        User user = userService.getLoggedUser();
        if(user==null || user.getRole() != User.Role.ADMIN){
            return Response.badRequest(2, "Nie posiadasz wymaganych uprawnień, aby to zrobić");
        }

        banner.changeStatus(Banner.Status.REJECTED, reason);
        bannerRepository.save(banner);
        return Response.ok("Baner został odrzucony");
    }

    public void publishBanner(Banner banner){
        banner.changeStatus(Banner.Status.PUBLISHED, null);

        LocalDateTime now = LocalDateTime.now();
        banner.setPublishedAt(now);
        banner.setFinishedAt(now.plusDays(31));
        banner.setPaid(true);

        if(banner.getSize() == Banner.Size.BIG){
            bigBanners.add(banner);
        }
        else if(banner.getSize() == Banner.Size.SMALL){
            smallBanners.add(banner);
        }

        bannerRepository.save(banner);
    }

    public ResponseEntity<Response> editBanner(Long id, MultipartFile file, String link) {
        Banner banner = bannerRepository.findById(id).orElse(null);
        if(banner == null){
            return Response.badRequest(1, "Wystąpił błąd #8725. Brak baneru o podanym ID");
        }

        User user = userService.getLoggedUser();
        if(user==null || user.getRole() != User.Role.ADMIN || !banner.getOwner().equals(user)){
            return Response.badRequest(2, "Nie posiadasz wymaganych uprawnień, aby to zrobić");
        }

        if(banner.getSize() == Banner.Size.BIG){
            bigBanners.remove(banner);
        }
        else if(banner.getSize() == Banner.Size.SMALL){
            smallBanners.remove(banner);
        }

        if(file != null){
            ResponseEntity<Response> uploadResponse = BannerFileService.upload(file);
            if(uploadResponse.getStatusCode() != HttpStatus.OK){
                return uploadResponse;
            }

            BannerFileService.remove(banner.getFilePath());
            banner.setFilePath(uploadResponse.getBody().getMessage());
        }
        banner.setLink(link);
        banner.changeStatus(Banner.Status.NOT_VERIFIED, null);
        bannerRepository.save(banner);
        if(banner.isPaid()){
            return Response.ok("Zmiany zostały wysłane do weryfikacji. Po weryfikacji baner zostanie automatycznie opublikowany");
        }
        return Response.ok("Zmiany zostały wysłane do weryfikacji");
    }

    public ResponseEntity<Response> deleteBanner(Long id) {
        Banner banner = bannerRepository.findById(id).orElse(null);
        if(banner == null){
            return Response.badRequest(1, "Wystąpił nieoczekiwany błąd. Brak baneru o podanym ID");
        }

        if(banner.getSize() == Banner.Size.BIG){
            bigBanners.remove(banner);
        }
        else if(banner.getSize() == Banner.Size.SMALL){
            smallBanners.remove(banner);
        }

        User user = userService.getLoggedUser();
        if(user==null || user.getRole() != User.Role.ADMIN || !banner.getOwner().equals(user)){
            return Response.badRequest(2, "Nie posiadasz wymaganych uprawnień, aby to zrobić");
        }

        BannerFileService.remove(banner.getFilePath());
        bannerRepository.delete(banner);
        return Response.ok("Baner został usunięty");
    }

    public List<Banner> getBannersByStatus(Banner.Status[] statuses){
        List<Banner> banners = new ArrayList<>();
        for(Banner.Status status:statuses){
            banners.addAll(bannerRepository.findByStatus(status));
        }
        return banners;
    }

    public void active(com.coresaken.mcserverlist.data.payment.Banner bannerDto) {
        Banner banner = bannerRepository.findById(bannerDto.getBannerId()).orElse(null);
        if(banner==null){
            return;
        }

        publishBanner(banner);
    }

    @Nullable
    public Banner getById(Long id) {
        return bannerRepository.findById(id).orElse(null);
    }
}
