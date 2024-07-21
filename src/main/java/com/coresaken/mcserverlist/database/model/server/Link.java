package com.coresaken.mcserverlist.database.model.server;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "link")
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    int index;

    @Column(length = 30)
    String type;

    String url;

    public LinkTypeData getType(){
        try{
            for(LinkTypeEnum e:LinkTypeEnum.values()){
                if(e.friendlyName.equalsIgnoreCase(type)){
                    return new LinkTypeData(false, e.friendlyName, e.iconUrl);
                }
            }
            LinkTypeEnum linkTypeEnum = LinkTypeEnum.valueOf(type.toUpperCase());
            return new LinkTypeData(false, linkTypeEnum.friendlyName, linkTypeEnum.iconUrl);
        }catch (IllegalArgumentException e){
            return new LinkTypeData(true, type, LinkTypeEnum.CUSTOM.iconUrl);
        }
    }

    @Data
    @AllArgsConstructor
    public static class LinkTypeData{
        boolean custom;

        String name;
        String iconUrl;
    }

    public enum LinkTypeEnum{
        CUSTOM("CUSTOM","fa-solid fa-link"),
        MAIN_PAGE("Strona główna", "fa-solid fa-house"),
        ITEM_SHOP("Item Shop", "fa-solid fa-cart-shopping"),
        SHOP("Sklep", "fa-solid fa-cart-shopping"),
        DISCORD("Discord", "fa-brands fa-discord"),
        TIKTOK("TikTok", "fa-brands fa-tiktok"),
        FACEBOOK("Facebook", "fa-brands fa-square-facebook"),
        INSTAGRAM("Instagram", "fa-brands fa-square-instagram"),
        YOUTUBE("YouTube", "fa-brands fa-youtube"),
        ;

        final String friendlyName;
        final String iconUrl;

        LinkTypeEnum(String friendlyName, String iconUrl){
            this.friendlyName = friendlyName;
            this.iconUrl = iconUrl;
        }

        @JsonValue
        public LinkTypeInfo convert() {
            return new LinkTypeInfo(friendlyName, iconUrl);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LinkTypeInfo {
        String friendlyName;
        String iconUrl;
    }
}
