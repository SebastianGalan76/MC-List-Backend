package com.coresaken.mcserverlist.database;

import com.coresaken.mcserverlist.database.model.server.Mode;
import com.coresaken.mcserverlist.database.model.server.Version;
import com.coresaken.mcserverlist.database.model.server.ratings.RatingCategory;
import com.coresaken.mcserverlist.database.repository.RatingCategoryRepository;
import com.coresaken.mcserverlist.database.repository.server.ModeRepository;
import com.coresaken.mcserverlist.database.repository.server.VersionRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@RequiredArgsConstructor
@Order(1)
public class DefaultDataLoader implements CommandLineRunner {
    final VersionRepository versionRepository;
    final ModeRepository modeRepository;
    final RatingCategoryRepository ratingCategoryRepository;

    @Override
    public void run(String... args) throws Exception {
        loadDefaultData();
    }

    private void loadDefaultData(){
        saveVersions();
        saveModes();
        saveRatingCategories();
    }

    private void saveVersions(){
        List<Version> versions = new ArrayList<>();
        versions.add(new Version(1210L, "1.21"));
        versions.add(new Version(1200L, "1.20"));
        versions.add(new Version(1190L, "1.19"));
        versions.add(new Version(1180L, "1.18"));

        versionRepository.saveAll(versions);
    }

    private void saveModes(){
        List<Mode> modes = new ArrayList<>();
        modes.add(new Mode(1L, "Sieć serwerów"));
        modes.add(new Mode(2L, "Survival"));
        modes.add(new Mode(3L, "Hardcore"));
        modes.add(new Mode(4L, "Survival + Gildie"));

        modeRepository.saveAll(modes);
    }

    private void saveRatingCategories(){
        List<RatingCategory> categories = new ArrayList<>();
        categories.add(new RatingCategory(1L, "Atmosfera gry", "Jak oceniasz ogólny klimat na serwerze?"));
        categories.add(new RatingCategory(2L, "Unikalne funkcje", "Jak oceniasz serwer pod względem unikalnych funkcji, trybów i dodatków?"));
        categories.add(new RatingCategory(3L, "Eventy i aktywność", "Jak oceniasz organizowane eventy oraz ich częstotliwość?"));
        categories.add(new RatingCategory(4L, "Ochrona przed oszustami", "Jak oceniasz zabezpieczenia przed cheaterami i oszustami?"));
        categories.add(new RatingCategory(5L, "Administracja", "Jak oceniasz pracę administracji, jej zaangażowanie w pomoc oraz łatwość kontaktu?"));

        ratingCategoryRepository.saveAll(categories);
    }
}