
function drawLocationsChart(selector, counts, colors) {
    var graph = $(selector)[0];
    var c = graph.getContext('2d');
    var barColors = ["black", "red", "blue", "green"];
    for (var i = 0; i < counts.length; ++i) {
        var height = counts[i] * 3;
        c.strokeStyle = barColors[colors[i]];
        c.strokeRect(1 + i * 3, graph.height - height, 2, height);
    }
}
