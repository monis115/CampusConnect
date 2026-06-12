#!/bin/bash
SERVICE_FILE="/Users/monis/Desktop/MasterFolder/Zorix/CampusConnect/src/main/java/com/cc/service/AdvancedTransactionService.java"
CONTROLLER_FILE="/Users/monis/Desktop/MasterFolder/Zorix/CampusConnect/src/main/java/com/cc/controller/AccountantController.java"

# Add the getFilterOptions method to AdvancedTransactionService
cat << 'INNER_EOF' > temp_service.txt
    public Map<String, List<String>> getFilterOptions() {
        Map<String, List<String>> options = new HashMap<>();

        // Get Agents
        List<PassbookForAccountantAgent> agentsPb = agentPassbookRepository.findAll();
        Set<String> agents = new HashSet<>();
        for (PassbookForAccountantAgent pb : agentsPb) {
            if (pb.getPaidByAgentFullName() != null && !pb.getPaidByAgentFullName().isEmpty()) {
                agents.add(pb.getPaidByAgentFullName());
            } else if (pb.getPaidByAgent() != null && !pb.getPaidByAgent().isEmpty()) {
                agents.add(pb.getPaidByAgent());
            }
        }

        // Get Doc Managers
        List<RequestMoney> requests = requestMoneyRepository.findAll();
        Set<String> docManagers = new HashSet<>();
        for (RequestMoney rm : requests) {
            if (rm.getRequesterFullName() != null && !rm.getRequesterFullName().isEmpty()) {
                docManagers.add(rm.getRequesterFullName());
            } else if (rm.getRequesterUsername() != null && !rm.getRequesterUsername().isEmpty()) {
                docManagers.add(rm.getRequesterUsername());
            }
        }

        // Get Colleges
        List<Student> students = studentRepository.findAll();
        Set<String> colleges = new HashSet<>();
        for (Student s : students) {
            if (s.getCollegeName() != null && !s.getCollegeName().isEmpty()) {
                colleges.add(s.getCollegeName());
            }
        }

        List<String> agentList = new ArrayList<>(agents);
        Collections.sort(agentList);
        List<String> docList = new ArrayList<>(docManagers);
        Collections.sort(docList);
        List<String> collegeList = new ArrayList<>(colleges);
        Collections.sort(collegeList);

        options.put("agents", agentList);
        options.put("docManagers", docList);
        options.put("colleges", collegeList);

        return options;
    }
INNER_EOF

# Insert into AdvancedTransactionService before the last brace
sed -i '' -e '/^}$/i\
'"$(cat temp_service.txt | sed 's/$/\\/')"'\
' "$SERVICE_FILE"

# Add the import Collections if not present
grep -q "java.util.Collections" "$SERVICE_FILE" || sed -i '' '/import java.util.List;/a\
import java.util.Collections;\
' "$SERVICE_FILE"

# Add the endpoint to AccountantController
cat << 'INNER_EOF' > temp_controller.txt
    @GetMapping("/transactions/advanced-filter-options")
    public ResponseEntity<Map<String, List<String>>> getAdvancedFilterOptions() {
        return ResponseEntity.ok(advancedTransactionService.getFilterOptions());
    }
INNER_EOF

# Insert into AccountantController before the last brace
sed -i '' -e '/^}$/i\
'"$(cat temp_controller.txt | sed 's/$/\\/')"'\
' "$CONTROLLER_FILE"

rm temp_service.txt temp_controller.txt
