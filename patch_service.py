import re

path = '/Users/monis/Desktop/MasterFolder/Zorix/CampusConnect/src/main/java/com/cc/service/AdvancedTransactionService.java'

with open(path, 'r') as f:
    content = f.read()

# Add imports for AgentRepository, DocPersonRepository, VisaManRepository
# Add them at the beginning of the class attributes

imports_addition = """
import com.cc.repository.AgentRepository;
import com.cc.repository.DocPersonRepository;
import com.cc.repository.VisaManRepository;
import com.cc.modals.Agent;
import com.cc.modals.DocPerson;
import com.cc.modals.VisaMan;
"""

if "AgentRepository" not in content:
    content = content.replace("import org.springframework.stereotype.Service;", imports_addition + "import org.springframework.stereotype.Service;")

fields_addition = """
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private DocPersonRepository docPersonRepository;
    
    @Autowired
    private VisaManRepository visaManRepository;
"""

if "agentRepository" not in content:
    content = content.replace("public class AdvancedTransactionService {", "public class AdvancedTransactionService {" + fields_addition)


# Replace getFilterOptions
new_method = """    public Map<String, List<String>> getFilterOptions() {
        Map<String, List<String>> options = new HashMap<>();

        // Get Agents
        Set<String> agents = new HashSet<>();
        List<Agent> allAgents = agentRepository.findAll();
        for (Agent a : allAgents) {
            if (a.getFullName() != null && !a.getFullName().isEmpty()) {
                agents.add(a.getFullName());
            } else if (a.getUsername() != null && !a.getUsername().isEmpty()) {
                agents.add(a.getUsername());
            }
        }

        // Fallback: also add from passbook in case some transactions don't have matching agent records
        List<PassbookForAccountantAgent> agentsPb = agentPassbookRepository.findAll();
        for (PassbookForAccountantAgent pb : agentsPb) {
            if (pb.getPaidByAgentFullName() != null && !pb.getPaidByAgentFullName().isEmpty()) {
                agents.add(pb.getPaidByAgentFullName());
            } else if (pb.getPaidByAgent() != null && !pb.getPaidByAgent().isEmpty()) {
                agents.add(pb.getPaidByAgent());
            }
        }

        // Get Doc Managers
        Set<String> docManagers = new HashSet<>();
        List<DocPerson> allDocPersons = docPersonRepository.findAll();
        for (DocPerson dp : allDocPersons) {
            if (dp.getFullName() != null && !dp.getFullName().isEmpty()) {
                docManagers.add(dp.getFullName());
            } else if (dp.getUsername() != null && !dp.getUsername().isEmpty()) {
                docManagers.add(dp.getUsername());
            }
        }
        List<VisaMan> allVisaMen = visaManRepository.findAll();
        for (VisaMan vm : allVisaMen) {
            if (vm.getFullName() != null && !vm.getFullName().isEmpty()) {
                docManagers.add(vm.getFullName());
            } else if (vm.getUsername() != null && !vm.getUsername().isEmpty()) {
                docManagers.add(vm.getUsername());
            }
        }

        // Fallback: from requests
        List<RequestMoney> requests = requestMoneyRepository.findAll();
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
"""

content = re.sub(r'public Map<String, List<String>> getFilterOptions\(\) \{.*\}', new_method, content, flags=re.DOTALL)

with open(path, 'w') as f:
    f.write(content)
print("Service patched successfully.")
