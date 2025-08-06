CREATE TABLE auto_case(
                          id serial primary key,      -- all
                          enable boolean,             -- all
                          is_e2e boolean,             -- all
                          is_sanity varchar,          -- all
    --suite
                          component varchar,          -- all (1)
                          component_like varchar,     -- all (1...*)
    --scenario
                          scenario varchar,           -- all
    --test
                          issue_key varchar,          -- all
                          description varchar,        -- all

                          label varchar,              -- single
                          path varchar,               -- single
                          method varchar,             -- single
                          profile_name varchar,       -- single

                          parameter varchar,          -- jsonb all (different format)
                          update_at timestamp(6) default current_timestamp -- all
);

CREATE TABLE auto_progress (
                               id SERIAL PRIMARY KEY,
                               runid CHARACTER VARYING(50) NOT NULL,
                               version_id CHARACTER VARYING(35),
                               component CHARACTER VARYING(50),
                               total_cases INTEGER,
                               passes INTEGER,
                               failures INTEGER,
                               skips INTEGER,
                               begin_time TIMESTAMP(3) WITHOUT TIME ZONE,
                               end_time TIMESTAMP(6) WITHOUT TIME ZONE,
                               releaseversion CHARACTER VARYING(200),
                               task_status CHARACTER VARYING(25),
                               run_by CHARACTER VARYING(50),
                               label CHARACTER VARYING(1000),
                               runmode CHARACTER VARYING(255),  -- 将枚举类型改为通用的VARYING，以兼容性更好
                               profile CHARACTER VARYING(200),
                               update_time TIMESTAMP(6) WITHOUT TIME ZONE
);
CREATE INDEX idx_component_auto_progress ON auto_progress (component);
CREATE INDEX idx_run_id_auto_progress ON auto_progress (runid);


CREATE TRIGGER trigger_update_at
    BEFORE UPDATE ON auto_case
    FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

-- 创建索引
CREATE INDEX idx_component_auto_progress ON auto_progress (component);
CREATE INDEX idx_run_id_auto_progress ON auto_progress (runid);

CREATE TABLE auto_endpoint(
                              id serial primary key,
                              component varchar,
                              path varchar,
                              method varchar,
                              class_name varchar,
                              service_name varchar,
                              qa_owner varchar
);

CREATE TABLE auto_baseurl(
                             id serial primary key,
                             service_name varchar,
                             profile_name varchar,
                             base_url varchar
);

CREATE TABLE auto_endpoint_all(
                                  id serial primary key,
                                  path varchar,
                                  method varchar,
                                  service_name varchar
);

CREATE TABLE auto_system_variable(
                                     id serial primary key,
                                     component varchar,
                                     component_like varchar,
                                     profile varchar,
                                     config_key varchar,
                                     value varchar
);


CREATE TABLE auto_configuration (
                                    id serial primary key,
                                    name VARCHAR(255) NOT NULL,
                                    component VARCHAR(255),
                                    daily_run_Version VARCHAR(255),
                                    qa VARCHAR(255),
                                    module VARCHAR(50),
                                    labels VARCHAR(255),
                                    sanity BOOLEAN,
                                    regions VARCHAR(255),
                                    delete_dependency JSONB,
                                    qaList JSONB,
                                    enable BOOLEAN,
                                    service_name VARCHAR(255)
);

GRANT ALL PRIVILEGES ON TABLE auto_baseurl,auto_case,auto_endpoint,auto_system_variable TO lifqa;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE auto_baseurl_id_seq,auto_endpoint_id_seq,auto_case_id_seq TO lifqa;


CREATE TABLE report_progress (
                                 group_id VARCHAR(50),
                                 run_id VARCHAR(50),
                                 component VARCHAR(100),
                                 report VARCHAR(25),
                                 "date" DATE,
                                 processed VARCHAR(10),
                                 module VARCHAR(25),
                                 label VARCHAR(100)
);

CREATE TABLE auto_testngresult (
                                   id SERIAL PRIMARY KEY,
                                   runid CHARACTER VARYING(60),
                                   component CHARACTER VARYING(50),
                                   suite_result CHARACTER VARYING,
                                   insertdatetime TIMESTAMP(6) WITHOUT TIME ZONE,
                                   config CHARACTER VARYING(255)
);
