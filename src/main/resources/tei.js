function onClickImage(evt) {
    const img = evt.target;
    const src = img.getAttribute("src");

    const closex = document.createElement("a");
    closex.setAttribute("href", "#");
    closex.innerText = "{close}";
    closex.style.float = "right";

    const urlmat = src.match(/^(.*\.ptif)\/(.*)$/);
    const url = urlmat[1]+"/info.json";
    const pmat = urlmat[2].split("/"); /* 150,320,4800,3000/full/!270/default.jpg */
    // TODO: make home be specified region (pmat[0]), if OSD allows it (see below)
    const mirrot = pmat[2];
    if (mirrot.startsWith("!")) {
        rot = parseInt(mirrot.substring(1), 10);
    } else {
        rot = parseInt(mirrot, 10);
    }

    const osd = document.createElement("div");
    osd.style.width = "80%";
    osd.style.height = "80%";
    osd.style.top = "50%";
    osd.style.left = "50%";
    osd.style.transform = "translate(-50%, -50%)";

    const dialog = document.createElement("dialog");
    dialog.addEventListener("close", function(evt) {
        evt.target.parentNode.removeChild(evt.target);
    });
    closex.addEventListener("click", function(evt) {
        evt.target.parentNode.close();
    });
    dialog.style.width = "80vw";
    dialog.style.height = "80vh";
    dialog.style.top = "50%";
    dialog.style.left = "50%";
    dialog.style.transform = "translate(-50%, -50%)";

    dialog.appendChild(closex);
    const title = document.createElement("div");
    title.innerText = src;
    title.style["overflow-wrap"] = "break-word";
    dialog.appendChild(title);

    dialog.appendChild(osd);

    const body = document.getElementsByTagName('body')[0];
    body.appendChild(dialog);

    var viewer = OpenSeadragon({
        prefixUrl: "https://cdnjs.cloudflare.com/ajax/libs/openseadragon/2.3.1/images/",
        element: osd,
        degrees: rot,
        tileSources: url,
        maxZoomPixelRatio: 10
    });

    dialog.showModal();
}

window.onload = function() {
    var i;
    const sds = document.querySelectorAll("img.tei-graphic");
    for (i = 0; i < sds.length; ++i) {
        const img = sds[i];
        img.addEventListener("click", onClickImage);
    }
};














// TODO: make home be specified region (pmat[0]), if OSD allows it
// viewer.addHandler("open", function(x) {
//     const strRectImg = pmat[0];
//     if (strRectImg !== "full") {
//         const xf = x.eventSource.viewport._contentSizeNoRotate.x;
//         const yf = x.eventSource.viewport._contentSizeNoRotate.y;
//         const strOrds = strRectImg.split(",");
//         const rectFit = new OpenSeadragon.Rect(
//             parseInt(strOrds[0])/xf,
//             parseInt(strOrds[1])/yf,
//             parseInt(strOrds[2])/xf,
//             parseInt(strOrds[3])/yf);
//         x.eventSource.viewport.setHomeBounds(rectFit, xf);
//     }
// });
