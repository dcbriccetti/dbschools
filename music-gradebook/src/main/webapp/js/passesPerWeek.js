function addGraph(musicianId) {
    var key = 'musician' + musicianId;
    if (key in chartData) {
        var musicianData = chartData[key];
        nv.addGraph(function () {
            var chart = nv.models.multiBarChart()
                    .x(function (d) {
                        return d.label
                    })
                    .y(function (d) {
                        return d.value
                    })
                    .color(["#5cb85c", "#c9302c"])
                    .reduceXTicks(false)
                    .showControls(false)
                    .stacked(true)
                    .showLegend(false)
                ;
            chart.margin().left = 25;
            chart.margin().bottom = 20;
            chart.yAxis.tickFormat(d3.format(',.0d'));

            d3.select('#passesPerWeek' + musicianId)
                .datum(musicianData)
                .call(chart);

            nv.utils.windowResize(chart.update);
            return chart;
        });
    }
}
