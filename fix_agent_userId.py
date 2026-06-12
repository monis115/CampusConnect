import re

path = '/Users/monis/Desktop/MasterFolder/Zorix/CampusConnect/src/main/java/com/cc/service/AdvancedTransactionService.java'

with open(path, 'r') as f:
    content = f.read()

content = content.replace("a.getUsername()", "a.getUserId()")

with open(path, 'w') as f:
    f.write(content)
print("Fixed a.getUsername() to a.getUserId()")
