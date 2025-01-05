package com.coresaken.mcserverlist.service;

import com.coresaken.mcserverlist.data.response.ObjectResponse;
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

    public ResponseEntity<ObjectResponse<Banner>> createBanner(MultipartFile file, String link, String size) {
        User user = userService.getLoggedUser();
        if(user == null){
            return ObjectResponse.badRequest(1, "Musisz się zalogować, aby stworzyć swój baner!");
        }

        ResponseEntity<Response> uploadResponse = BannerFileService.upload(file);
        if(uploadResponse.getStatusCode() != HttpStatus.OK){
            Response body = uploadResponse.getBody();
            if(body != null){
                return ObjectResponse.badRequest(body.getErrorCode(), body.getMessage());
            }
        }

        Banner banner = new Banner();
        banner.setSize(Banner.Size.valueOf(size));
        banner.setLink(link);
        banner.setOwner(user);
        banner.changeStatus(Banner.Status.NOT_VERIFIED, null);
        banner.setFilePath(uploadResponse.getBody().getMessage());
        bannerRepository.save(banner);

        return ObjectResponse.ok("Banner został przesłany do weryfikacji. Przejdź do zakładki \"Moje Banery\", aby wyświetlić status.", banner);
    }

    public ResponseEntity<Response> acceptBanner(Long bannerId){
        Banner banner = bannerRepository.findById(bannerId).orElse(null);
        if(banner == null){
            return Response.badRequest(1, "Wystąpił nieoczekiwany błąd. Brak banera o podanym ID");
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
            return Response.badRequest(1, "Wystąpił nieoczekiwany błąd. Brak banera o podanym ID");
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

    public ResponseEntity<ObjectResponse<Banner>> editBanner(Long id, MultipartFile file, String link) {
        Banner banner = bannerRepository.findById(id).orElse(null);
        if(banner == null){
            return ObjectResponse.badRequest(1, "Wystąpił nieoczekiwany błąd. Brak banera o podanym ID");
        }

        User user = userService.getLoggedUser();
        if(user==null || (user.getRole() != User.Role.ADMIN && !banner.getOwner().equals(user))){
            System.out.println(user.getLogin());
            return ObjectResponse.badRequest(2, "Nie posiadasz wymaganych uprawnień, aby to zrobić");
        }

        if(file != null){
            ResponseEntity<Response> uploadResponse = BannerFileService.upload(file);
            if(uploadResponse.getStatusCode() != HttpStatus.OK){
                Response body = uploadResponse.getBody();
                if(body != null){
                    return ObjectResponse.badRequest(body.getErrorCode(), body.getMessage());
                }
            }

            BannerFileService.remove(banner.getFilePath());
            banner.setFilePath(uploadResponse.getBody().getMessage());
        }

        if(banner.getStatus() == Banner.Status.REJECTED){
            banner.setStatus(Banner.Status.NOT_VERIFIED);
            banner.setRejectedReason(null);
        }

        banner.setLink(link);
        bannerRepository.save(banner);

        if(banner.getSize() == Banner.Size.BIG){
            if(bigBanners.contains(banner)){
                bigBanners.remove(banner);
                bigBanners.add(banner);
            }
        }
        else if(banner.getSize() == Banner.Size.SMALL){
            if(smallBanners.contains(banner)){
                smallBanners.remove(banner);
                smallBanners.add(banner);
            }
        }

        return ObjectResponse.ok("Zmiany zostały zapisane.", banner);
    }

    public ResponseEntity<Response> deleteBanner(Long id) {
        Banner banner = bannerRepository.findById(id).orElse(null);
        if(banner == null){
            return Response.badRequest(1, "Wystąpił nieoczekiwany błąd. Brak banera o podanym ID");
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
        return Response.ok("Baner został prawidłowo usunięty");
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

    public ResponseEntity<ObjectResponse<List<Banner>>> getBanners() {
        List<Banner> banners = new ArrayList<>();

        Banner bigBanner = getBigBanner();
        if(bigBanner != null){
            banners.add(bigBanner);
        }

        List<Banner> smallBanners = getSmallBanners();
        if (smallBanners != null && !smallBanners.isEmpty()) {
            banners.addAll(smallBanners);
        }

        return ObjectResponse.ok(null, banners);
    }
}
