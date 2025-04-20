const sensorData = [
    { id: "S001", leakage: 0.5 },
    { id: "S002", leakage: 1.2 },
    { id: "S003", leakage: 0.8 },
    { id: "S004", leakage: 0.3 },
    { id: "S005", leakage: 1.0 },
    { id: "S006", leakage: 0.6 },
    { id: "S007", leakage: 0.4 },
  ];

 /* function loadTable() {
    const tbody = document.getElementById("sensorTable");
    tbody.innerHTML = "";
  
    sensorData.forEach(sensor => {
      const row = document.createElement("tr");
  
      row.innerHTML = `
        <td>${sensor.id}</td>
        <td>${sensor.leakage}</td>
        <td>
          <div class="progress-bar">
            <div class="progress-fill" style="width: ${sensor.leakage * 100 / 1.5}%;"></div>
          </div>
        </td>
        <td>
          <select>
            <option>1 min</option>
            <option>5 min</option>
            <option>10 min</option>
          </select>
        </td>
        <td><button onclick="alert('Filtering ${sensor.id}')">Filter</button></td>
      `;
      tbody.appendChild(row);
    });
  }
  
  function filterTable() {
    const input = document.getElementById("searchInput").value.toLowerCase();
    const rows = document.querySelectorAll("#sensorTable tr");
  
    rows.forEach(row => {
      const id = row.cells[0].textContent.toLowerCase();
      row.style.display = id.includes(input) ? "" : "none";
    });
  }
  
  window.onload = loadTable;
  */
  function loadTable() {
    fetch('https://eay4fqoqq5.execute-api.eu-north-1.amazonaws.com/default/spillnetapi')
    .then(response => response.json())
    .then(data => {
        const tbody = document.getElementById("sensorTable");
        tbody.innerHTML = "";
        
        data.forEach(sensor => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${sensor.id}</td>
                <td>${sensor.leakage}</td>
                <td>
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: ${sensor.leakage * 100 / 1.5}%;"></div>
                    </div>
                </td>
                <td>
                    <select>
                        <option>1 min</option>
                        <option>5 min</option>
                        <option>10 min</option>
                    </select>
                </td>
                <td><button onclick="alert('Filtering ${sensor.id}')">Filter</button></td>
            `;
            tbody.appendChild(row);
        });
    })
    .catch(error => console.error('Error fetching data:', error));
}

//window.onload = loadTable;