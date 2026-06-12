const http = require('http');

http.get('http://localhost:8080/api/accountant/ACC_001', (resp) => {
  let data = '';
  resp.on('data', (chunk) => { data += chunk; });
  resp.on('end', () => {
    console.log(data.substring(0, 500)); // Print just a snippet to see structure
  });
}).on("error", (err) => {
  console.log("Error: " + err.message);
});
