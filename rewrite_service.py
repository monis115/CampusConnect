import re

with open('/Users/monis/Desktop/MasterFolder/Zorix/CampusConnect/src/main/java/com/cc/service/AdvancedTransactionService.java', 'r') as f:
    content = f.read()

# remove trailing backslashes at the end of lines
content = re.sub(r'\\\n', '\n', content)

with open('/Users/monis/Desktop/MasterFolder/Zorix/CampusConnect/src/main/java/com/cc/service/AdvancedTransactionService.java', 'w') as f:
    f.write(content)
