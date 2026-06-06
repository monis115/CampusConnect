const http = require('http');

http.get('http://localhost:8080/api/agents/user/2', (resp) => {
  let data = '';
  resp.on('data', (chunk) => { data += chunk; });
  resp.on('end', () => {
    const agent = JSON.parse(data);
    const telexStudents = agent.student.filter(s => s.action === 'INITIATE_TELEX' || s.applicationStatus.includes('TELEX'));
    console.log(JSON.stringify(telexStudents.map(s => ({
      id: s.id, 
      fullName: s.fullName, 
      status: s.applicationStatus, 
      telexMan: !!s.telexMan,
      action: s.action
    })), null, 2));
  });
}).on("error", (err) => {
  console.log("Error: " + err.message);
});
