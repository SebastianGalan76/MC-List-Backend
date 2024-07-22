function copyToClipboard(element) {
    const text = element.getAttribute('data-copy');

    const tempInput = document.createElement('input');
    tempInput.style.position = 'absolute';
    tempInput.style.left = '-9999px';
    tempInput.value = text;
    document.body.appendChild(tempInput);

    tempInput.select();
    document.execCommand('copy');

    document.body.removeChild(tempInput);

    alert('Skopiowano wartość: ' + text);
}