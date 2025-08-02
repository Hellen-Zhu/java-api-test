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

insert into auto_baseurl(service_name,profile_name,base_url)

insert into auto_endpoint(component,path,method,class_name,service_name,qa_owner)