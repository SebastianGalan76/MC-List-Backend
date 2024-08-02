const serverTemplate = document.getElementById("server-template");

const serverListContainer = document.getElementById("server-container");

import { getCurrentPage, refreshPageContainer } from "./pageManager.js";

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

function populateList(listArray) {
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

        const online = template.querySelector('.online-value');
        online.innerHTML = serverJson.onlinePlayers;

        const vote = template.querySelector('.vote-value');
        vote.innerHTML = serverJson.votes.length;

        serverListContainer.appendChild(template);
    });
}