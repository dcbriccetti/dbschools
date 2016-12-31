function drawChart(id, xs, ys, attribs, axisAttribs) {
    var graph = $('#pg' + id)[0];
    var c = graph.getContext('2d');

    c.beginPath();
    c.strokeStyle = "gray";
    c.moveTo(0, 0);
    c.lineTo(0, graph.height);
    c.lineTo(graph.width, graph.height);
    c.stroke();

    var yBottom = graph.height - 2;
    var rectWidth = 4;

    for (var i = 0; i < xs.length; ++i) {
        if (attribs[i] == '1')
            c.fillStyle = "brown";
        else
            c.fillStyle = "black";
        c.fillRect(xs[i], yBottom - ys[i], rectWidth, rectWidth);
    }
    var axisColors = ["black", "red", "blue", "green"];
    if (axisAttribs.length > 1) {
        c.moveTo(xs[0], graph.height);
        for (var j = 0; j < axisAttribs.length - 1; ++j) {
            c.beginPath();
            c.strokeStyle = axisColors[axisAttribs[j]];
            c.lineWidth = 3;
            c.moveTo(xs[j], graph.height);
            c.lineTo(xs[j + 1], graph.height);
            c.stroke();
        }
    }
}
