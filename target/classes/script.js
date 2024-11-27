// Establish WebSocket connection
let socket = new WebSocket('ws://localhost:8080/logs');

socket.onopen = function(event) {
    console.log("WebSocket connection opened");
};

socket.onmessage = function(event) {
    // Handle loading state
    document.getElementById('loading-message').style.display = 'none';

    const newLogs = JSON.parse(event.data);
    processNewLogs(newLogs);
};

socket.onclose = function(event) {
    console.log("WebSocket connection closed");
    reconnectWebSocket();
};

socket.onerror = function(error) {
    console.error("WebSocket error:", error);
};

// Process new logs for charts and table updates
let logLevels = {};
let methodStatusData = {
    GET: { success: 0, fail: 0 },
    POST: { success: 0, fail: 0 },
    PUT: { success: 0, fail: 0 },
    DELETE: { success: 0, fail: 0 },
};

let timeout;

function processNewLogs(newLogs) {
    clearTimeout(timeout);
    timeout = setTimeout(() => {
        // Update pie chart
        newLogs.forEach(log => {
            logLevels[log.log_level] = (logLevels[log.log_level] || 0) + 1;
        });
        updatePieChart();

        // Update HTTP Methods vs. Status Codes chart
        newLogs.forEach(log => {
            const method = log.http_method;
            const statusCode = log.status_code;
            if (methodStatusData[method]) {
                if (statusCode >= 200 && statusCode < 300) {
                    methodStatusData[method].success += 1;
                } else {
                    methodStatusData[method].fail += 1;
                }
            }
        });
        updateHTTPMethodsChart();

        // Update table
        let tableBody = document.querySelector('.table100 tbody');
        newLogs.forEach(log => {
            const row = document.createElement('tr');
            const cellIP = document.createElement('td');
            cellIP.textContent = log.client_ip || 'N/A';
            row.appendChild(cellIP);
            // Repeat for other properties...
            tableBody.appendChild(row);
        });
        limitTableRows(100);

    }, 100); // Debounce to update every 100ms
}

// Update Pie Chart for log levels
function updatePieChart() {
    const chartData = Object.entries(logLevels).map(([level, count]) => ({
        value: count,
        name: level
    }));
    const chart = echarts.init(document.getElementById('logLevelsPie'));
    chart.setOption({
        series: [{
            data: chartData
        }]
    });
}

// Update Bar Chart for HTTP Methods vs Status Codes
function updateHTTPMethodsChart() {
    const chartData = Object.entries(methodStatusData).map(([method, { success, fail }]) => ({
        method,
        success,
        fail,
    }));
    const chart = echarts.init(document.getElementById('httpMethodStatusChart'));
    chart.setOption({
        series: [
            { data: chartData.map(item => item.success) },
            { data: chartData.map(item => item.fail) }
        ]
    });
}

// Limit table rows to max 100
function limitTableRows(maxRows) {
    let tableBody = document.querySelector('.table100 tbody');
    while (tableBody.children.length > maxRows) {
        tableBody.removeChild(tableBody.firstChild);
    }
}

// Reconnect WebSocket with retry logic
let reconnectAttempts = 0;

function reconnectWebSocket() {
    setTimeout(() => {
        socket = new WebSocket('ws://localhost:8080/logs');
        socket.onopen = function(event) {
            console.log("WebSocket connection reopened");
            reconnectAttempts = 0;
        };
        socket.onmessage = function(event) {
            const newLogs = JSON.parse(event.data);
            processNewLogs(newLogs);
        };
        socket.onclose = function(event) {
            console.log("WebSocket connection closed");
            reconnectAttempts++;
            if (reconnectAttempts < 5) {
                reconnectWebSocket();
            } else {
                console.error("Unable to reconnect after 5 attempts.");
            }
        };
        socket.onerror = function(error) {
            console.error("WebSocket error:", error);
        };
    }, 2000); // Reconnect after 2 seconds
}
