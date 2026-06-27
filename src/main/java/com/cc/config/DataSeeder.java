package com.cc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("====== Running Database Seeder ======");
        try {
            ClassPathResource resource = new ClassPathResource("task_rules_seed.sql");
            if (resource.exists()) {
                String sql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                
                // Only run if the table is empty to prevent duplicates, or clear it first
                // Wait, if it's a seed, we should clear existing task_rules to apply the new seed cleanly.
                jdbcTemplate.execute("DELETE FROM task_rules");
                
                String[] commands = sql.split(";");
                for (String command : commands) {
                    if (command.trim().length() > 0) {
                        jdbcTemplate.execute(command);
                    }
                }
                System.out.println("====== task_rules_seed.sql Executed Successfully ======");
            }
        } catch (Exception e) {
            System.err.println("====== Failed to run task_rules_seed.sql ======");
            e.printStackTrace();
        }
    }
}
