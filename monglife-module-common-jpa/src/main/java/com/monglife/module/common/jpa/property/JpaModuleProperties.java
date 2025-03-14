package com.monglife.module.common.jpa.property;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JpaModuleProperties {

    private String dialect;

    private Hbm2Ddl hbm2Ddl;

    private Boolean showSql;

    private Boolean formatSql;

    @Getter
    @Setter
    public static class Hbm2Ddl {

        private String auto;

        public Hbm2Ddl() {
            this.auto = "none";
        }
    }

    public JpaModuleProperties() {
        this.dialect = "";
        this.hbm2Ddl = new Hbm2Ddl();
        this.showSql = false;
        this.formatSql = false;
    }
}
