import re

path = '/Users/monis/Desktop/MasterFolder/Zorix/CampusConnect/src/main/java/com/cc/service/AdvancedTransactionService.java'

with open(path, 'r') as f:
    content = f.read()

imports = """import org.springframework.beans.factory.annotation.Autowired;
import com.cc.repository.AgentRepository;
import com.cc.repository.DocPersonRepository;
import com.cc.repository.VisaManRepository;
import com.cc.modals.Agent;
import com.cc.modals.DocPerson;
import com.cc.modals.VisaMan;
"""

if "import com.cc.repository.AgentRepository;" not in content:
    content = content.replace("@Service\npublic class AdvancedTransactionService {", imports + "@Service\npublic class AdvancedTransactionService {")

with open(path, 'w') as f:
    f.write(content)
print("Java imports fixed.")
