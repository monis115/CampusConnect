package com.cc.service.report.impl;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class FixReports {
    public static void main(String[] args) throws Exception {
        File dir = new File("/Users/monis/Desktop/MasterFolder/Zorix/CampusConnect/src/main/java/com/cc/service/report/impl");
        File[] files = dir.listFiles((d, name) -> name.endsWith("Generator.java"));
        
        for (File file : files) {
            String content = new String(Files.readAllBytes(file.toPath()));
            
            if (content.contains("DateFilterUtil")) continue;
            
            String originalContent = content;
            
            content = content.replaceAll(
                "(List<SubAccountTransaction>\\s+(\\w+)\\s*=\\s*transactionRepository\\.findAll\\(\\);)",
                "$1\n        $2 = DateFilterUtil.filter($2, request, SubAccountTransaction::getTransactionDate);"
            );
            
            content = content.replaceAll(
                "(List<CollectionRequest>\\s+(\\w+)\\s*=\\s*collectionRequestRepository\\.findAll\\(\\);)",
                "$1\n        $2 = DateFilterUtil.filter($2, request, CollectionRequest::getCreatedAt);"
            );
            
            content = content.replaceAll(
                "(List<BeneficiaryAccount>\\s+(\\w+)\\s*=\\s*beneficiaryRepository\\.findAll\\(\\);)",
                "$1\n        $2 = DateFilterUtil.filter($2, request, BeneficiaryAccount::getCreatedAt);"
            );
            
            content = content.replaceAll(
                "(List<AccountantCredit>\\s+(\\w+)\\s*=\\s*creditRepository\\.findAll\\(\\);)",
                "$1\n        $2 = DateFilterUtil.filter($2, request, AccountantCredit::getCreatedAt);"
            );
            
            content = content.replaceAll(
                "(List<RequestMoney>\\s+(\\w+)\\s*=\\s*requestMoneyRepository\\.findAll\\(\\);)",
                "$1\n        $2 = DateFilterUtil.filter($2, request, RequestMoney::getCreatedAt);"
            );

            if (!content.equals(originalContent)) {
                if (!content.contains("import com.cc.service.report.impl.DateFilterUtil;")) {
                    content = content.replace("import com.cc.DTO.report.ReportRequest;", 
                        "import com.cc.DTO.report.ReportRequest;\nimport com.cc.service.report.impl.DateFilterUtil;");
                }
                Files.write(file.toPath(), content.getBytes());
                System.out.println("Updated: " + file.getName());
            }
        }
    }
}
