export function loadData(hourlyPlayerCount, dailyPlayerCount) {
    const plugin = {
        id: 'verticalLiner',
        afterInit: (chart, args, opts) => {
            chart.verticalLiner = {}
        },
        afterEvent: (chart, args, options) => {
            const { inChartArea } = args
            chart.verticalLiner = { draw: inChartArea }
        },
        beforeTooltipDraw: (chart, args, options) => {
            const { draw } = chart.verticalLiner
            if (!draw) return

            const { ctx } = chart
            const { top, bottom } = chart.chartArea
            const { tooltip } = args
            const x = tooltip?.caretX
            if (!x) return

            ctx.save()

            ctx.beginPath()
            ctx.moveTo(x, top)
            ctx.lineTo(x, bottom)
            ctx.stroke()

            ctx.restore()
        }
    }

    const gradient = window['chartjs-plugin-gradient'];
    Chart.register(gradient);

    // Tworzenie wykresu za pomocą Chart.js
    createOnlinePlayerChart('online-player-chart', hourlyPlayerCount.slice(-48), plugin, true);
    createOnlinePlayerChart('daily-player-chart-7', hourlyPlayerCount, plugin, true);
    createOnlinePlayerChart('daily-player-chart-30', dailyPlayerCount.slice(-30), plugin, false);
    createOnlinePlayerChart('daily-player-chart-365', dailyPlayerCount.slice(-365), plugin, false);

    createOnlinePlayerChart('online-player-chart-down-panel', hourlyPlayerCount.slice(-48), plugin, true);
}

function createOnlinePlayerChart(id, data, plugin, beginAtZero) {
    var labels = data.map(function (entry) {
        // Konwersja daty na obiekt JavaScript
        var date = new Date(entry.time);
        // Formatowanie czasu do "hh:mm"
        return ('0' + date.getHours()).slice(-2) + ':' + ('0' + date.getMinutes()).slice(-2);
    });

    var playerCounts = data.map(function (entry) {
        return entry.playerCount;
    });

    var ctx = document.getElementById(id).getContext('2d');
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Online',
                data: playerCounts,
                backgroundColor: 'rgba(75, 192, 192, 1)',
                borderColor: 'rgba(0,0,0,0.0)',
                borderWidth: 1,
                fill: true,
                tension: 0.3,
                gradient: {
                    backgroundColor: {
                        axis: 'y',
                        colors: {
                            0: 'rgba(226,162,88,1)',
                            100: 'rgba(236,196,103,1)'
                        }
                    }
                }
            }]
        },
        options: {
            interaction: {
                mode: 'index',
                intersect: false,
            },
            scales: {
                x: {
                    ticks: {
                        color: '#ffffff80'
                    }
                },
                y: {
                    beginAtZero: beginAtZero,

                    ticks: {
                        color: '#ffffff80'
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                }
            },
            radius: 0,
        },
        plugins: [plugin]
    });
}
