import re

path = '/Users/monis/Desktop/MasterFolder/Zorix/CampusConnect/src/main/java/com/cc/service/AdvancedTransactionService.java'

with open(path, 'r') as f:
    content = f.read()

# For Agent: Set outflow to 0.0
# For DocManager: Set inflow to 0.0 (already done)

old_agent_logic = """                            double inflow = pb.getAmountPaidByAgent();
                            double outflow = pb.getRequestedAmountByAccountantToAgent();"""
                            
new_agent_logic = """                            double inflow = pb.getAmountPaidByAgent();
                            double outflow = 0.0; // Enforce only Inflow for Agent as requested"""
                            
if old_agent_logic in content:
    content = content.replace(old_agent_logic, new_agent_logic)

with open(path, 'w') as f:
    f.write(content)
print("Service patched flow successfully.")
