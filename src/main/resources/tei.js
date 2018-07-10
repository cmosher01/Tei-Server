window.onload = () => {
    var i;
    var rot;
    const sds = document.querySelectorAll("img.tei-graphic");
    for (i = 0; i < sds.length; ++i) {
        const img = sds[i];
        if (img.offsetParent !== null) {
            const urlattr = img.getAttribute("url");
            if (urlattr) {
                const urlmat = img.getAttribute("url").match(/^(.*\.ptif)\/(.*)$/);
                const url = urlmat[1]+"/info.json";
                const pmat = urlmat[2].split("/");
                const mirrot = pmat[2];
                if (mirrot.startsWith("!")) {
                    rot = parseInt(mirrot.substring(1), 10);
                } else {
                    rot = parseInt(mirrot, 10);
                }

                const div = document.createElement("div");
                div.setAttribute("class", "tei-graphic");
                div.setAttribute("url", url);
                img.parentNode.replaceChild(div, img);

                var viewer = OpenSeadragon({
                    prefixUrl: "https://cdnjs.cloudflare.com/ajax/libs/openseadragon/2.3.1/images/",
                    element: div,
                    degrees: rot,
                    tileSources: url,
                    maxZoomPixelRatio: 10
                });
            }
        }
    }
}
