const addressInput = document.getElementById('address-input');
const portInput = document.getElementById('port-input');

import { getSelectedVersions } from './versionManager.js';
import { getSelectedModes } from './modeManager.js';
import { isAddressValid, isPortValid } from './utils.js';

addressInput.addEventListener('input', function (event) {
    const currentValue = addressInput.value;
    const filteredValue = currentValue.replace(/:/g, '');
    if (currentValue !== filteredValue) {
        addressInput.value = filteredValue;
    }
});

const serverStatusResponseError = document.getElementById('server-status-response-error');
const loadingStatusAnimation = document.getElementById("loading-animation");

const errorMessage = document.getElementById('error-message');

var isFunctionLocked = false;

document.getElementById('add-server-button').addEventListener('click', addServer);
async function addServer() {
    if (!isAddressValid(addressInput.value)) {
        errorMessage.innerHTML = "Wpisz poprawny adres serwera!";
        return;
    }
    if (!isPortValid(portInput.value)) {
        errorMessage.innerHTML = "Wpisz poprawny port serwera!";
        return;
    }
    errorMessage.innerHTML = null;

    serverStatusResponseError.style.display = "none";
    loadingStatusAnimation.style.display = "block";

    if (isFunctionLocked) {
        setTimeout(() => {
            addServer();
        }, 5000);

        return;
    }

    isFunctionLocked = true;
    setTimeout(() => {
        isFunctionLocked = false;
    }, 5000);

    const serverDto = {
        ip: addressInput.value,
        port: portInput.value,

        versions: getSelectedVersions(),
        modes: getSelectedModes()
    }

    try {
        const response = await fetch('/add-new-server/post', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(serverDto)
        });


        if (!response.ok) {
            const errorResponse = await response.json();
            throw new Error(`Error: ${errorResponse.message}`);
        }
        else {
            const responseJson = await response.json();
            
            serverStatusResponseError.style.display = "block";
            loadingStatusAnimation.style.display = "none";

            if (responseJson.status == "BAD_REQUEST") {
                errorMessage.innerHTML = responseJson.message;
            }
        }
    } catch (error) {
        errorMessage.innerHTML = error.message;
    }
}