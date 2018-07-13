function onClickImage(event) {
    const src = event.target.getAttribute("src");
    /*
    Assume src is a IIIF Image API 2.0 format URI:
        {scheme}://{server}{/prefix}/{identifier}/{region}/{size}/{rotation}/{quality}.{format}
    For example:
        https://www.example.com/images/path/to/some_image.ptif/150,320,4800,3000/full/90/default.jpg
    match to get:
    [1]: "https://www.example.com/images/path/to/some_image.ptif"
    [2]: "150,320,4800,3000" {region}
    [3]: "full" {size}
    [4]: "90" {rotation} (and mirror)
    [5]: "default.jpg"
    */

    /* match last 4 fields non-greedy, and beginning field greedy: */
    const srcMatch = src.match(/^(.+)\/([^/]+?)\/([^/]+?)\/([^/]+?)\/([^/]+?)$/);

    const tiles = srcMatch[1]+"/info.json";
    const rot = parseInt(srcMatch[4].startsWith("!") ? srcMatch[4].substring(1) : srcMatch[4], 10);

    const div = document.createElement("div");
    const scrpos = document.scrollingElement.scrollTop;
    document.body.appendChild(div);

    const viewer = OpenSeadragon({
        prefixUrl: "https://cdnjs.cloudflare.com/ajax/libs/openseadragon/2.3.1/images/",
        element: div,
        degrees: rot,
        tileSources: tiles,
        maxZoomPixelRatio: 10
    });
    viewer.setFullScreen(true);

    viewer.addHandler("close", function(evt) {
        const d = evt.userData.div;
        d.parentNode.removeChild(d);
    }, {div: div});

    viewer.addHandler("open", function(x) {
        const strOrd = x.userData.strOrds;
        if (strOrd !== "full") {
            const strOrds = strOrd.split(",");
            const rectFit = new OpenSeadragon.Rect(
                parseInt(strOrds[0]),
                parseInt(strOrds[1]),
                parseInt(strOrds[2]),
                parseInt(strOrds[3]));
            const rectView = x.eventSource.viewport.imageToViewportRectangle(rectFit);
            // doesn't seem to fill the whole viewport, but at least it centers it:
            x.eventSource.viewport.fitBounds(rectView, true);
        }
    }, {strOrds: srcMatch[2]});

    viewer.addHandler("full-screen", function(x) {
        if (!x.fullScreen) {
            x.eventSource.close();
        }
    });

    document.scrollingElement.scrollTop = scrpos;

    const osd = document.querySelectorAll(".openseadragon-container")[0];
    osd.style.backgroundColor = "var(--sol-base3)";
}

window.onload = function() {
    var i;
    const sds = document.querySelectorAll("img.tei-graphic");
    for (i = 0; i < sds.length; ++i) {
        const img = sds[i];
        img.addEventListener("click", onClickImage);
    }
};
