
nv.addGraph(function() {
    var chart = nv.models.discreteBarChart()
        .x(function(d) { return d.label })
        .y(function(d) { return d.value })
        .staggerLabels(true)
        .duration(100)
        ;

    d3.select('#passesPerWeek svg')
        .datum(chartData)
        .call(chart);

    nv.utils.windowResize(chart.update);
    return chart;
});

