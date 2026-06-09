
package com.vbforge.org.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import javax.sql.DataSource;

@Configuration
public class FlywayConfig {
    
    private final DataSource dataSource;
    
    public FlywayConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void runFlywayMigration() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>> RUNNING FLYWAY MIGRATION >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load();
        
        flyway.migrate();
        System.out.println("=========================== FLYWAY MIGRATION COMPLETE ==============================");
    }
}