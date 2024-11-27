// This script handles the dynamic updating of the table when new logs are received via WebSocket.

let tableBody = document.querySelector('.table100 tbody');

function processNewLogs(newLogs) {
    // Iterate over the new logs and add rows to the table
    newLogs.forEach(log => {
        const row = document.createElement('tr');
        
        // Create and append table cells for each log property
        const cellIP = document.createElement('td');
        cellIP.textContent = log.client_ip || 'N/A';
        row.appendChild(cellIP);

        const cellTimestamp = document.createElement('td');
        cellTimestamp.textContent = log.timestamp || 'N/A';
        row.appendChild(cellTimestamp);

        const cellRequestType = document.createElement('td');
        cellRequestType.textContent = log.request_type || 'N/A';
        row.appendChild(cellRequestType);

        const cellEndpoint = document.createElement('td');
        cellEndpoint.textContent = log.endpoint || 'N/A';
        row.appendChild(cellEndpoint);

        const cellStatusCode = document.createElement('td');
        cellStatusCode.textContent = log.status_code || 'N/A';
        row.appendChild(cellStatusCode);

        const cellResponseTime = document.createElement('td');
        cellResponseTime.textContent = log.response_time || 'N/A';
        row.appendChild(cellResponseTime);

        const cellReferrer = document.createElement('td');
        cellReferrer.textContent = log.referrer || 'N/A';
        row.appendChild(cellReferrer);

        const cellUserAgent = document.createElement('td');
        cellUserAgent.textContent = log.user_agent || 'N/A';
        row.appendChild(cellUserAgent);

        const cellAction = document.createElement('td');
        cellAction.textContent = log.action || 'N/A';
        row.appendChild(cellAction);

        const cellUserId = document.createElement('td');
        cellUserId.textContent = log.user_id || 'N/A';
        row.appendChild(cellUserId);

        const cellLogType = document.createElement('td');
        cellLogType.textContent = log.log_type || 'N/A';
        row.appendChild(cellLogType);

        tableBody.appendChild(row);
    });

    // Limit the table rows to the most recent 100 logs
    limitTableRows(100);
}

function limitTableRows(maxRows) {
    // Keep only the most recent logs in the table (limit to 100 rows)
    while (tableBody.children.length > maxRows) {
        tableBody.removeChild(tableBody.firstChild);
    }
}
