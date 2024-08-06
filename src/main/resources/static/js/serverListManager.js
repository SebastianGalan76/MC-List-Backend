const serverTemplate = document.getElementById("server-template");

const serverListContainer = document.getElementById("server-container");

import { getCurrentPage, refreshPageContainer } from "./pageManager.js";
var promotedServers = false;

export async function loadServers() {
    const response = await fetch('/server/list/' + getCurrentPage(), {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
    });


    if (!response.ok) {
        const errorResponse = await response.json();
        throw new Error(`Error: ${errorResponse.message}`);
    }
    else {
        var responseJson = await response.json();

        populateList(responseJson.content);
        refreshPageContainer(responseJson.page.totalPages);
    }
}

export function populateList(listArray) {
    serverListContainer.innerHTML = null;

    listArray.forEach(serverJson => {
        const template = serverTemplate.content.cloneNode(true);

        const link = template.querySelector('.link');
        link.href = "/server/" + serverJson.ip;

        const addressValue = template.querySelector('.address-value');
        addressValue.innerHTML = serverJson.name.name;

        const motd = template.querySelector('.motd');
        const formattedMotd = serverJson.detail.motdHtml.replace(/\n/g, "<br>");
        motd.innerHTML = formattedMotd;

        const logo = template.querySelector('.logo-src');
        if (serverJson.detail.icon != null) {
            logo.src = serverJson.detail.icon;
        }
        else {
            logo.style.display = "none";
        }

        if (serverJson.mode != null) {
            const modeTag = template.querySelector('.mode-tag ');
            modeTag.style.display = "flex";

            modeTag.querySelector('.content').innerHTML = serverJson.mode.name;
        }
        if (serverJson.versions != null && serverJson.versions.length > 0) {
            const versionTag = template.querySelector('.version-tag');
            versionTag.style.display = "flex";

            const sortedArray = serverJson.versions.sort((a, b) => parseFloat(a.id) - parseFloat(b.id));

            const minValue = sortedArray[0];
            const maxValue = sortedArray[sortedArray.length - 1];

            if (minValue.id == maxValue.id) {
                versionTag.querySelector('.content').innerHTML = minValue.name;
            }
            else {
                versionTag.querySelector('.content').innerHTML = minValue.name + " - " + maxValue.name;
            }
        }
        if (serverJson.mods) {
            template.querySelector('.mods-tag ').style.display = "flex";
        }
        if (serverJson.premium) {
            template.querySelector('.premium-tag ').style.display = "flex";
        }

        if (serverJson.promotionPoints > 0) {
            const promotionPoints = template.querySelector('.promotion-value');
            promotionPoints.innerHTML = serverJson.promotionPoints;

            template.querySelector('.server').classList.add('promoted');

            if(!promotedServers){
                promotedServers = true;
                serverListContainer.appendChild(createServerHeader('promoted', '👑', 'Promowane serwery'));
            }
        }
        else {
            template.querySelector('.promotion').style.display = "none";

            if(promotedServers){
                promotedServers = false;
                serverListContainer.appendChild(createServerHeader('', '', 'Serwery'));
            }
        }

        const online = template.querySelector('.online-value');
        online.innerHTML = serverJson.onlinePlayers;

        const vote = template.querySelector('.vote-value');
        vote.innerHTML = serverJson.votes.length;

        serverListContainer.appendChild(template);
    });
}

function createServerHeader(className, icon, text) {
    const header = document.createElement('div');
    header.className = 'servers-header';
    if (className) header.classList.add(className);

    const title = document.createElement('div');
    title.className = 'title';
    
    if (icon) {
        const iconElement = document.createElement('div');
        iconElement.className = 'icon';
        iconElement.textContent = icon;
        title.appendChild(iconElement);
    }

    title.appendChild(document.createTextNode(text));
    header.appendChild(title);

    const divider = document.createElement('div');
    divider.className = className ? 'divider-line-horizontal' : 'divider-line';
    header.appendChild(divider);

    return header;
}